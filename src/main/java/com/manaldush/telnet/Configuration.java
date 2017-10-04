package com.manaldush.telnet;

import com.google.common.base.Preconditions;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Base configuration of service.
 * Created by Maxim.Melnikov on 20.06.2017.
 */
public final class Configuration implements Cloneable {
    private static final String DEFAULT_GREETING = "Alice Server Greeting!!!";
    private static final String DEFAULT_PROMPT = "->";
    /**Maximum Number of simultaneous user sessions, default value = 10, 0 is not limited.*/
    private int maxSessions = 0;
    /**Server address*/
    private final InetAddress address;
    private final int port;
    private ICommandParserFactory parser = new DefaultCommandParserFactory();
    private String greeting = DEFAULT_GREETING;

    private Integer SO_SNDBUF = 1024;

    private Integer SO_RCVBUF = 1024;

    private Boolean SO_REUSEADDR = Boolean.FALSE;

    private Boolean TCP_NODELAY = Boolean.FALSE;

    private String prompt = DEFAULT_PROMPT;

    private Configuration(InetAddress _addr, int _port) {
        address = _addr;
        port = _port;
    }

    /**
     * Build configuration object.
     * @param _addr - address
     * @param _port - port
     * @return Configuration object
     * @throws UnknownHostException - unknown address
     */
    public static Configuration build(final String _addr, final int _port) throws UnknownHostException {
        Preconditions.checkNotNull(_addr);
        Preconditions.checkArgument(0 < _port);
        Preconditions.checkArgument(_port <= 65535);
        return new Configuration(InetAddress.getByName(_addr), _port);
    }

    public Configuration setSNDBUF(final Integer SO_SNDBUF) {
        Preconditions.checkNotNull(SO_SNDBUF);
        Preconditions.checkArgument(SO_SNDBUF > 0);
        this.SO_SNDBUF = SO_SNDBUF;
        return this;
    }

    public Configuration setRCVBUF(final Integer SO_RCVBUF) {
        Preconditions.checkNotNull(SO_RCVBUF);
        Preconditions.checkArgument(SO_RCVBUF > 0);
        this.SO_RCVBUF = SO_RCVBUF;
        return this;
    }

    public Configuration setREUSEADDR(final Boolean SO_REUSEADDR) {
        Preconditions.checkNotNull(SO_REUSEADDR);
        this.SO_REUSEADDR = SO_REUSEADDR;
        return this;
    }

    public Configuration setTCPNODELAY(final Boolean TCP_NODELAY) {
        Preconditions.checkNotNull(TCP_NODELAY);
        this.TCP_NODELAY = TCP_NODELAY;
        return this;
    }

    public Configuration setMaxSessions(int _maxSessions) {
        Preconditions.checkArgument(_maxSessions >= 0);
        maxSessions = _maxSessions;
        return this;
    }

    public Configuration setParser(final ICommandParserFactory _parser) {
        Preconditions.checkNotNull(_parser);
        parser = _parser;
        return this;
    }

    @Override
    public Object clone() {
        Configuration conf = new Configuration(this.address, this.port);
        return conf.setRCVBUF(SO_RCVBUF).setSNDBUF(SO_SNDBUF).setREUSEADDR(SO_REUSEADDR).setTCPNODELAY(TCP_NODELAY).
                setMaxSessions(maxSessions).setParser(parser);
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public Integer getSO_SNDBUF() {
        return SO_SNDBUF;
    }

    public Integer getSO_RCVBUF() {
        return SO_RCVBUF;
    }

    public Boolean getSO_REUSEADDR() {
        return SO_REUSEADDR;
    }

    public Boolean getTCP_NODELAY() {
        return TCP_NODELAY;
    }

    public int getMaxSessions() {
        return maxSessions;
    }

    public ICommandParserFactory getParser() {
        return parser;
    }

    public String getGreeting() {
        return greeting;
    }

    public void setGreeting(String greeting) {
        Preconditions.checkNotNull(greeting);
        this.greeting = greeting;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
