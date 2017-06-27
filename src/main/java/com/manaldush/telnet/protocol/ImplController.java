package com.manaldush.telnet.protocol;

import com.google.common.base.Preconditions;
import com.manaldush.telnet.*;
import com.manaldush.telnet.exceptions.ConfigurationException;
import com.manaldush.telnet.exceptions.GeneralTelnetException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Created by Maxim.Melnikov on 22.06.2017.
 */
public class ImplController implements IController<Configuration>, Runnable {
    private enum STATUS {
        STARTED, STOPPED, CONFIGURED, INITIALIZE,
    }
    private enum SOCKET_TYPE {
        CLIENT, SERVER
    }
    private final static SocketOption SO_REUSEADDR_OPT    = new ImplSocketOption<>("SO_REUSEADDR" , Boolean.TYPE);
    private final static SocketOption SO_RCVBUF_OPT       = new ImplSocketOption<>("SO_RCVBUF" , Integer.TYPE);
    private final static SocketOption SO_SNDBUF_OPT       = new ImplSocketOption<>("SO_SNDBUF" , Integer.TYPE);
    private final static SocketOption TCP_NODELAY_OPT     = new ImplSocketOption<>("TCP_NODELAY" , Boolean.TYPE);
    private final static SocketOption SO_KEEPALIVE_OPT    = new ImplSocketOption<>("SO_KEEPALIVE" , Boolean.TYPE);
    private final static byte[] LOG_SESSIONS_OVER_LIMIT = "sessions limit is over".getBytes();
    private final static String LOG_UNKNOWN_COMMAND = "unknown command";
    private final static byte[] LOG_UNKNOWN_COMMAND_AS_BYTES = LOG_UNKNOWN_COMMAND.getBytes();
    private final static ByteBuffer BUFFER_OVER_LIMIT;
    private final static ByteBuffer BUFFER_UNKNOWN_COMMAND;
    private static final int DATA_PORTION = 10;
    private volatile ServerSocketChannel ss;
    private volatile Configuration conf;
    private final Map<String, CommandTemplate> commandTemplates = new HashMap<>();
    private final Map<SocketChannel, ISession> sessions = new HashMap<>();
    private volatile STATUS status = STATUS.INITIALIZE;
    private volatile Selector selector;
    private int sessionsNumber = 0;
    private final Semaphore state = new Semaphore(1);
    private Thread executor = null;
    static {
        BUFFER_OVER_LIMIT = ByteBuffer.allocate(LOG_SESSIONS_OVER_LIMIT.length);
        BUFFER_OVER_LIMIT.put(LOG_SESSIONS_OVER_LIMIT);
        BUFFER_UNKNOWN_COMMAND = ByteBuffer.allocate(LOG_UNKNOWN_COMMAND_AS_BYTES.length);
        BUFFER_UNKNOWN_COMMAND.put(LOG_UNKNOWN_COMMAND_AS_BYTES);
    }

    public ImplController() {}

    private void innerConfigure(final Configuration _conf) throws IOException {
        Preconditions.checkNotNull(_conf);
        Configuration conf = (Configuration) _conf.clone();
        ServerSocketChannel ss = ServerSocketChannel.open();
        ss.setOption(SO_REUSEADDR_OPT, conf.getSO_REUSEADDR());
        ss.setOption(SO_RCVBUF_OPT, conf.getSO_RCVBUF());
        ss.bind(new InetSocketAddress(conf.getAddress(), conf.getPort()));
        ss.configureBlocking(false);
        selector = Selector.open();
        ss.register(selector, SelectionKey.OP_ACCEPT, SOCKET_TYPE.SERVER);
        this.conf = _conf;
        this.ss = ss;
    }

    /**
     * Registration of command in controller.
     * @param _template - command template
     */
    @Override
    public void register(CommandTemplate _template) {
        Preconditions.checkNotNull(_template);
        commandTemplates.put(_template.getCommand(), _template);
    }

    /**
     * Unregister command in controller.
     * @param _template - command template as CommandTemplate object
     */
    @Override
    public void unregister(CommandTemplate _template) {
        Preconditions.checkNotNull(_template);
        commandTemplates.remove(_template.getCommand());
    }

    /**
     * Unregister command in controller.
     *
     * @param _template - command template as string
     */
    @Override
    public void unregister(String _template) {
        Preconditions.checkNotNull(_template);
        commandTemplates.remove(_template);
    }

    @Override
    public synchronized void start() {
        if (STATUS.INITIALIZE == status) throw new IllegalStateException("System has not been configured yet");
        if (STATUS.STARTED == status) throw new IllegalStateException("System has been already started");
        if (STATUS.STOPPED == status) throw new IllegalStateException("System has been already stopped");
        executor = new Thread(this);
        status = STATUS.STARTED;
        executor.start();
    }

    @Override
    public void stop() {
        synchronized (this) {
            if (status == STATUS.STARTED) status = STATUS.STOPPED;
            else return;
        }
        if (executor.isAlive()) try {
            executor.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Command search(String _command) throws ParseException {
        ICommandParser parser = conf.getParser().build(_command);
        String cmdPart = parser.parseCommand();
        CommandTemplate template = commandTemplates.get(cmdPart);
        if (template == null) return null;
        return Command.build(template, parser.parseOptions());
    }

    /**
     * Configure Controller object.
     * @param _conf - configuration object
     */
    @Override
    public void configure(Configuration _conf) throws ConfigurationException {
        synchronized (this) {
            if (status == STATUS.INITIALIZE) {
                status = STATUS.CONFIGURED;
            } else {
                throw new ConfigurationException("Controller has been already configured");
            }
        }
        try {
            innerConfigure(_conf);
        } catch (IOException reason) {
            throw new ConfigurationException("Controller configuration error occured", reason);
        }
    }

    @Override
    public void run() {
        try {
            while (status == STATUS.STARTED) {
                if (selector.select(1000) == 0) continue;
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    Object type = key.attachment();
                    if (type.equals(SOCKET_TYPE.SERVER)) {
                        SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
                        if (client == null) continue;
                        if (acceptSession()) {
                            configureClientSocket(client);
                            synchronized (this) {
                                if (sessions.size() == 0) state.acquire();
                                sessions.put(client, new ImplTelnetSession(client, this, DATA_PORTION));
                            }
                        } else {
                            BUFFER_OVER_LIMIT.flip();
                            client.write(BUFFER_OVER_LIMIT);
                            client.close();
                        }
                    } else if (type.equals(SOCKET_TYPE.CLIENT)) {
                        SocketChannel client = (SocketChannel) key.channel();
                        ISession session;
                        synchronized (this) {
                            session = sessions.get(client);
                        }
                        try {
                            String line = this.readData(client, session);
                            if (line == null) continue;
                            Command cmd = this.search(line);
                            if (cmd == null) throw new ParseException(LOG_UNKNOWN_COMMAND, 0);
                            session.addTask(cmd.getTemplate().getCommandProcessorFactory().build(cmd));
                        } catch (GeneralTelnetException | IOException e) {
                            e.printStackTrace();
                            status = STATUS.STOPPED;
                            session.close();
                        } catch (ParseException ex) {
                            client.write(BUFFER_UNKNOWN_COMMAND);
                        }
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            status = STATUS.STOPPED;
        } finally {
            // close all sessions and wait closing all sessions
            synchronized (this) {
                for (Map.Entry<SocketChannel, ISession> pair : sessions.entrySet()) {
                    ISession session = pair.getValue();
                    session.close();
                }
            }
            try {
                state.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
                status = STATUS.STOPPED;
            }
        }
    }

    private void configureClientSocket(SocketChannel _channel) throws IOException {
        _channel.configureBlocking(false);
        _channel.setOption(SO_REUSEADDR_OPT, conf.getSO_REUSEADDR());
        _channel.setOption(SO_RCVBUF_OPT, conf.getSO_RCVBUF());
        _channel.setOption(SO_SNDBUF_OPT, conf.getSO_SNDBUF());
        _channel.setOption(TCP_NODELAY_OPT, conf.getTCP_NODELAY());
        _channel.setOption(SO_KEEPALIVE_OPT, true);
        _channel.register(selector, SelectionKey.OP_READ, SOCKET_TYPE.CLIENT);
    }

    private boolean acceptSession() {
        final int maxSessNum = conf.getMaxSessions();
        if (maxSessNum == 0 || maxSessNum > sessionsNumber) {
            sessionsNumber++;
            return true;
        }
        return false;
    }

    synchronized void resetSession(SocketChannel _channel) throws IOException {
        sessions.remove(_channel);
        _channel.close();
        if (sessions.size() == 0) state.release();
        sessionsNumber--;
    }

    private String readData(SocketChannel _channel, ISession _session) throws IOException, GeneralTelnetException {
        ByteBuffer buffer = ByteBuffer.allocate(DATA_PORTION);
        String line = null;
        for(;;) {
            int numberBytes = _channel.read(buffer);
            if (numberBytes == DATA_PORTION) {
                line = _session.decode(buffer, DATA_PORTION);
                continue;
            } else if (numberBytes < DATA_PORTION && numberBytes > 0) {
                line = _session.decode(buffer, numberBytes);
            } else if (DATA_PORTION < -1) {
                // connection was closed
                ISession session = sessions.get(_channel);
                session.close();
            }
            break;
        }
        return line;
    }


}
