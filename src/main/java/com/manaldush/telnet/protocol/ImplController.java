package com.manaldush.telnet.protocol;

import com.google.common.base.Preconditions;
import com.manaldush.telnet.*;
import com.manaldush.telnet.exceptions.ConfigurationException;
import com.manaldush.telnet.exceptions.GeneralTelnetException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
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
public class ImplController implements IController<ConfigurationWrapper>, Runnable {
    enum STATUS {
        STARTED, STOPPED, CONFIGURED, INITIALIZE,
    }
    enum SOCKET_TYPE {
        CLIENT, SERVER
    }
    private final static SocketOption<Boolean> SO_REUSEADDR_OPT    = StandardSocketOptions.SO_REUSEADDR;
    private final static SocketOption<Integer> SO_RCVBUF_OPT       = StandardSocketOptions.SO_RCVBUF;
    private final static SocketOption<Integer> SO_SNDBUF_OPT       = StandardSocketOptions.SO_SNDBUF;
    private final static SocketOption<Boolean> TCP_NODELAY_OPT     = StandardSocketOptions.TCP_NODELAY;
    private final static SocketOption<Boolean> SO_KEEPALIVE_OPT    = StandardSocketOptions.SO_KEEPALIVE;
    private final static byte[] LOG_SESSIONS_OVER_LIMIT = "sessions limit is over".getBytes();
    private final static String LOG_UNKNOWN_COMMAND = "unknown command";
    private final static byte[] LOG_UNKNOWN_COMMAND_AS_BYTES = LOG_UNKNOWN_COMMAND.getBytes();
    private final static ByteBuffer BUFFER_OVER_LIMIT;
    private final static ByteBuffer BUFFER_UNKNOWN_COMMAND;
    private static final int DATA_PORTION = 10;
    private volatile ServerSocketChannel ss;
    private volatile ConfigurationWrapper conf;
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

    public ImplController() {
    }

    private void innerConfigure(final ConfigurationWrapper _conf) throws IOException {
        Preconditions.checkNotNull(_conf);
        ConfigurationWrapper conf = (ConfigurationWrapper) _conf.clone();
        ServerSocketChannel ss = _conf.getSsChannelFactory().build();
        ss.setOption(SO_REUSEADDR_OPT, conf.getConf().getSO_REUSEADDR());
        ss.setOption(SO_RCVBUF_OPT, conf.getConf().getSO_RCVBUF());
        ss.bind(new InetSocketAddress(conf.getConf().getAddress(), conf.getConf().getPort()));
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
        ICommandParser parser = conf.getConf().getParser().build(_command);
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
    public void configure(ConfigurationWrapper _conf) throws ConfigurationException {
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
                processKeys();
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

    private void processKeys() throws IOException, InterruptedException {
        if (selector.select(1000) == 0) return;
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = selectedKeys.iterator();
        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            if (key.isAcceptable()) {
                SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
                if (client == null) return;
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
            } else if (key.isReadable()) {
                SocketChannel client = (SocketChannel) key.channel();
                ISession session;
                synchronized (this) {
                    session = sessions.get(client);
                }
                // Check Session was reset
                if (session == null) return;
                try {
                    String line = this.readData(client, session);
                    if (line == null) return;
                    Command cmd = this.search(line);
                    if (cmd == null) throw new ParseException(LOG_UNKNOWN_COMMAND, 0);
                    session.addTask(cmd.getTemplate().getCommandProcessorFactory().build(cmd,
                            ImplWriterAdapter.build(session, client)));
                } catch (GeneralTelnetException | IOException e) {
                    e.printStackTrace();
                    status = STATUS.STOPPED;
                    session.close();
                } catch (ParseException ex) {
                    BUFFER_UNKNOWN_COMMAND.flip();
                    client.write(BUFFER_UNKNOWN_COMMAND);
                }
            }
        }
    }

    private void configureClientSocket(SocketChannel _channel) throws IOException {
        _channel.configureBlocking(false);
        _channel.setOption(SO_REUSEADDR_OPT, conf.getConf().getSO_REUSEADDR());
        _channel.setOption(SO_RCVBUF_OPT, conf.getConf().getSO_RCVBUF());
        _channel.setOption(SO_SNDBUF_OPT, conf.getConf().getSO_SNDBUF());
        _channel.setOption(TCP_NODELAY_OPT, conf.getConf().getTCP_NODELAY());
        _channel.setOption(SO_KEEPALIVE_OPT, true);
        _channel.register(selector, SelectionKey.OP_READ, SOCKET_TYPE.CLIENT);
    }

    private boolean acceptSession() {
        final int maxSessNum = conf.getConf().getMaxSessions();
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
            } else if (numberBytes < 0) {
                // connection was closed
                _session.close();
                return null;
            }
            break;
        }
        return line;
    }


}
