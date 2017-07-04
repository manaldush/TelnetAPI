package com.manaldush.telnet.protocol;

import com.google.common.base.Preconditions;
import com.manaldush.telnet.*;
import com.manaldush.telnet.commands.HelpCommand;
import com.manaldush.telnet.commands.QuitCommand;
import com.manaldush.telnet.exceptions.ConfigurationException;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.exceptions.OperationException;

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
    private final static SocketOption<Boolean> SO_REUSEADDR_OPT    = StandardSocketOptions.SO_REUSEADDR;
    private final static SocketOption<Integer> SO_RCVBUF_OPT       = StandardSocketOptions.SO_RCVBUF;
    private final static SocketOption<Integer> SO_SNDBUF_OPT       = StandardSocketOptions.SO_SNDBUF;
    private final static SocketOption<Boolean> TCP_NODELAY_OPT     = StandardSocketOptions.TCP_NODELAY;
    private final static SocketOption<Boolean> SO_KEEPALIVE_OPT    = StandardSocketOptions.SO_KEEPALIVE;
    private final static byte[] LOG_SESSIONS_OVER_LIMIT = "sessions limit is over".getBytes();
    private final static String LOG_UNKNOWN_COMMAND = "unknown command";
    private static final int DATA_PORTION = 10;
    private volatile ServerSocketChannel ss;
    private volatile ConfigurationWrapper conf;
    private final Map<String, CommandTemplate> commandTemplates = new HashMap<>();
    private final Map<SocketChannel, IClientSession> sessions = new HashMap<>();
    private volatile STATUS status = STATUS.INITIALIZE;
    private volatile Selector selector;
    private int sessionsNumber = 0;
    private final Semaphore state = new Semaphore(1);
    private Thread executor = null;

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
        ss.register(selector, SelectionKey.OP_ACCEPT);
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
        registerDefaultCommands();
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
        selector.wakeup();
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
                for (Map.Entry<SocketChannel, IClientSession> pair : sessions.entrySet()) {
                    IClientSession session = pair.getValue();
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
        selector.select();
        if (status != STATUS.STARTED) return;
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = selectedKeys.iterator();
        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            if (key.isAcceptable()) {
                SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
                if (client == null) continue;
                SelectionKey clientKey = configureClientSocket(client);
                IClientSession session = new ImplTelnetClientSession(client, this, DATA_PORTION, clientKey);
                boolean denySess = true;
                synchronized (this) {
                    if (acceptSession()) {
                        if (sessions.size() == 0) state.acquire();
                        sessions.put(client, session);
                        denySess = false;
                    }
                }
                if (denySess) {
                    session.write(LOG_SESSIONS_OVER_LIMIT);
                    session.close();
                }
            } else if (key.isReadable()) {
                SocketChannel client = (SocketChannel) key.channel();
                if (client == null) continue;
                IClientSession session;
                synchronized (this) {
                    session = sessions.get(client);
                }
                // Check Session was reset
                if (session == null) continue;
                try {
                    addTasks(this.readData(client, session), session);
                } catch (GeneralTelnetException | IOException e) {
                    e.printStackTrace();
                    status = STATUS.STOPPED;
                    session.close();
                }
            }
        }
    }

    private SelectionKey configureClientSocket(SocketChannel _channel) throws IOException {
        _channel.configureBlocking(false);
        _channel.setOption(SO_REUSEADDR_OPT, conf.getConf().getSO_REUSEADDR());
        _channel.setOption(SO_RCVBUF_OPT, conf.getConf().getSO_RCVBUF());
        _channel.setOption(SO_SNDBUF_OPT, conf.getConf().getSO_SNDBUF());
        _channel.setOption(TCP_NODELAY_OPT, conf.getConf().getTCP_NODELAY());
        _channel.setOption(SO_KEEPALIVE_OPT, true);
        return _channel.register(selector, SelectionKey.OP_READ);
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

    private List<String> readData(SocketChannel _channel, IClientSession _session) throws IOException, GeneralTelnetException {
        ByteBuffer buffer = ByteBuffer.allocate(DATA_PORTION);
        List<String> lines = new ArrayList<>();
        for(;;) {
            int numberBytes = _channel.read(buffer);
            if (numberBytes == DATA_PORTION) {
                lines.addAll(_session.decode(buffer, DATA_PORTION));
                continue;
            } else if (numberBytes < DATA_PORTION && numberBytes > 0) {
                lines.addAll(_session.decode(buffer, numberBytes));
            } else if (numberBytes < 0) {
                // connection was closed
                _session.close();
                return null;
            }
            break;
        }
        return lines;
    }

    private void addTasks(List<String> _lines, IClientSession _session) throws IOException {
        if (_lines == null || _lines.size() == 0) return;
        Iterator<String> iterator = _lines.iterator();
        while(iterator.hasNext()) {
            try {
                String line = iterator.next();
                Command cmd = this.search(line);
                if (cmd == null) throw new ParseException(LOG_UNKNOWN_COMMAND, 0);
                _session.addTask(cmd);
            } catch (ParseException ex) {
                _session.write(LOG_UNKNOWN_COMMAND);
            }
        }
    }

    private void registerDefaultCommands() {
        registerQuitCommand();
        registerHelpCommand();
    }

    private void registerQuitCommand() {
        CommandTemplate quit = CommandTemplate.build("quit", "close session", new ICommandProcessorFactory() {
            @Override
            public ICommandProcessor build(Command _cmd, final IClientSession _session) {
                return QuitCommand.build(_session);
            }
        });
        this.register(quit);
    }

    private void registerHelpCommand() {
        CommandTemplate help = CommandTemplate.build("help", "help command, describe all commands", new ICommandProcessorFactory() {
            @Override
            public ICommandProcessor build(Command _cmd, IClientSession _session) {
                return HelpCommand.build(_session, new HashMap<>(commandTemplates));
            }
        });
        this.register(help);
    }

}
