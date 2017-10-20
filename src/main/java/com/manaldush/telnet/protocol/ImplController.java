package com.manaldush.telnet.protocol;

import com.google.common.base.Preconditions;
import com.manaldush.telnet.*;
import com.manaldush.telnet.commands.HasNoAccessCommand;
import com.manaldush.telnet.commands.HelpCommand;
import com.manaldush.telnet.commands.QuitCommand;
import com.manaldush.telnet.commands.UnknownCommand;
import com.manaldush.telnet.exceptions.AuthTelnetException;
import com.manaldush.telnet.exceptions.ConfigurationException;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.security.AuthTelnetClientSession;
import com.manaldush.telnet.security.Role;

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

import static com.manaldush.telnet.protocol.Constants.CRLF;

/**
 * Implementation of IController object.
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
    private static final int DATA_PORTION = 10;
    private volatile ServerSocketChannel ss;
    private volatile ConfigurationWrapper conf;
    private final Map<String, CommandTemplate> commandTemplates = new HashMap<>();
    private final Map<SocketChannel, AuthTelnetClientSession> sessions = new HashMap<>();
    private volatile STATUS status = STATUS.INITIALIZE;
    private volatile Selector selector;
    private int sessionsNumber = 0;
    private final Semaphore state = new Semaphore(1);
    private Thread executor = null;
    private static final Command UNKNOWN_COMMAND = createUnknownCommand();
    private static final Command HAS_NO_ACCESS_COMMAND = createHasNoAccessCommand();

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

    /**
     * Start controller.
     */
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

    /**
     * Stop controller.
     */
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

    /**
     * Return command object appropriate to given String.
     * @param _command - string representation of command with options
     * @return - command
     * @throws ParseException - parse Exception of incoming String, illegal options
     */
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
                for (Map.Entry<SocketChannel, AuthTelnetClientSession> pair : sessions.entrySet()) {
                    AuthTelnetClientSession session = pair.getValue();
                    session.getSession().close();
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
                IClientSession session = new ImplTelnetClientSession(client, this, DATA_PORTION, clientKey, conf.getConf().getPrompt());
                AuthTelnetClientSession authSession = new AuthTelnetClientSession(session);
                boolean denySess = true;
                synchronized (this) {
                    if (acceptSession()) {
                        if (sessions.size() == 0) state.acquire();
                        sessions.put(client, authSession);
                        denySess = false;
                    }
                }
                if (denySess) {
                    session.write(LOG_SESSIONS_OVER_LIMIT);
                    session.close();
                } else {
                    getUserName(session);
                }
            } else if (key.isReadable()) {
                SocketChannel client = (SocketChannel) key.channel();
                if (client == null) continue;
                AuthTelnetClientSession authSession;
                IClientSession session;
                synchronized (this) {
                    authSession = sessions.get(client);
                }
                // Check Session was reset
                if (authSession == null) continue;
                session = authSession.getSession();
                try {
                    if (!authSession.hasUserName()) {
                        List<String> lines = this.readData(client, authSession.getSession());
                        if (lines != null && !lines.isEmpty()) {
                            authSession.setUserName(lines.get(0));
                            session.resetBuffer();
                            this.getPassword(authSession.getSession());
                            continue;
                        }
                    } else if (!authSession.hasPasswd()) {
                        List<String> lines = this.readData(client, authSession.getSession());
                        if (lines != null && !lines.isEmpty()) {
                            authSession.setPasswd(lines.get(0));
                            session.resetBuffer();
                            authSession.checkUser();
                            if (authSession.isAuthFailed()) {
                                session.write(Constants.RED);
                                session.write("Illegal credentials");
                                session.write(Constants.RESET_COLOR);
                                session.write(CRLF);
                                throw new AuthTelnetException("Authentification error");
                            }
                            session.write(Constants.GREEN);
                            session.write(conf.getConf().getGreeting());
                            session.write(Constants.RESET_COLOR);
                            session.write(CRLF);
                            session.prompt();
                            continue;
                        }
                    }
                    addTasks(this.readData(client, authSession.getSession()), authSession);
                } catch (GeneralTelnetException | IOException e) {
                    e.printStackTrace();
                    //status = STATUS.STOPPED;
                    authSession.getSession().close();
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

    private void addTasks(List<String> _lines, AuthTelnetClientSession _session) throws IOException {
        if (_lines == null || _lines.size() == 0) return;
        Iterator<String> iterator = _lines.iterator();
        while(iterator.hasNext()) {
            String line = iterator.next();
            if (line== null || line.isEmpty()) {
                _session.getSession().prompt();
                continue;
            }
            Command cmd = null;
            try {
                cmd = this.search(line);
            } catch (ParseException e) {
                e.printStackTrace();
                cmd = null;
            }
            if (cmd == null) {
                cmd = UNKNOWN_COMMAND;
            } else if (!_session.checkRoles(cmd.getTemplate())) {
                cmd = HAS_NO_ACCESS_COMMAND;
            }
            _session.getSession().addTask(cmd);
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
        quit.addRole(Role.SYSTEM_ROLE);
    }

    private void registerHelpCommand() {
        CommandTemplate help = CommandTemplate.build("help", "help command, describe all commands", new ICommandProcessorFactory() {
            @Override
            public ICommandProcessor build(Command _cmd, IClientSession _session) {
                return HelpCommand.build(_session, new HashMap<>(commandTemplates));
            }
        });
        this.register(help);
        help.addRole(Role.SYSTEM_ROLE);
    }

    private static Command createUnknownCommand() {
        CommandTemplate unknownCommand = CommandTemplate.build("unknown command", "unknown command", new ICommandProcessorFactory() {
            @Override
            public ICommandProcessor build(Command _cmd, IClientSession _session) {
                return UnknownCommand.build(_session);
            }
        });
        return Command.build(unknownCommand);
    }

    private static Command createHasNoAccessCommand() {
        final CommandTemplate hasNoAccessCommand = CommandTemplate.build("has no access", "has no access", new ICommandProcessorFactory() {
            @Override
            public ICommandProcessor build(Command _cmd, IClientSession _session) {
                return HasNoAccessCommand.build(_session);
            };
        });
        return Command.build(hasNoAccessCommand);
    }

    private void getUserName(IClientSession _session) throws IOException {
        _session.write("username:");
    }

    private void getPassword(IClientSession _session) throws IOException {
        _session.write("password:");
    }
}
