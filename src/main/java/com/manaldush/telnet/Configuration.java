package com.manaldush.telnet;

import com.google.common.base.Preconditions;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Base configuration of service.
 * Created by Maxim.Melnikov on 20.06.2017.
 */
public final class Configuration implements Cloneable {
    /**Default greeting string.*/
    private static final String DEFAULT_GREETING = "Alice Server Greeting!!!";
    /**Default prompt char.*/
    private static final String DEFAULT_PROMPT = "->";
    /**Default send buffer.*/
    private static final int DEFAULT_SND_BUF = 1024;
    /**Default rcv buffer.*/
    private static final int DEFAULT_RCV_BUF = 1024;
    /**Max port value.*/
    private static final int MAX_PORT_VALUE = 65535;
    /**Maximum Number of simultaneous user sessions, default value = 10, 0 is not limited.*/
    private int maxSessions = 0;
    /**Server address.*/
    private final InetAddress address;
    /**Port value.*/
    private final int port;
    /**Command parser.*/
    private ICommandParserFactory parser = new DefaultCommandParserFactory();
    /**Greeting value.*/
    private String greeting = DEFAULT_GREETING;
    /**Socket send buffer size.*/
    private Integer soSndBuf = DEFAULT_SND_BUF;
    /**Socket receive buffer size.*/
    private Integer soRcvBuf = DEFAULT_RCV_BUF;
    /**Socket reuse address flag.*/
    private Boolean soReuseAddress = Boolean.FALSE;
    /**Socket TCP no delay flag.*/
    private Boolean tcpNoDelay = Boolean.FALSE;
    /**Prompt string.*/
    private String prompt = DEFAULT_PROMPT;

    /**
     * Constructor of configuration object.
     * @param _addr - address object
     * @param _port - port value
     */
    private Configuration(final InetAddress _addr, final int _port) {
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
        Preconditions.checkArgument(_port <= MAX_PORT_VALUE);
        return new Configuration(InetAddress.getByName(_addr), _port);
    }

    /**
     * Set SND_BUF size socket parameter.
     * @param _soSndBuf - SND_BUF size
     * @return configuration object
     */
    public Configuration setSoSndBuf(final Integer _soSndBuf) {
        Preconditions.checkNotNull(_soSndBuf);
        Preconditions.checkArgument(_soSndBuf > 0);
        this.soSndBuf = _soSndBuf;
        return this;
    }

    /**
     * Set RCV_BUF size socket parameter.
     * @param _soRcvBuf - RCV_BUF size
     * @return configuration object
     */
    public Configuration setRCVBUF(final Integer _soRcvBuf) {
        Preconditions.checkNotNull(_soRcvBuf);
        Preconditions.checkArgument(_soRcvBuf > 0);
        this.soRcvBuf = _soRcvBuf;
        return this;
    }

    /**
     * Set socket Reuse address flag.
     * @param _soReuseAddress - reuse address flag
     * @return configuration object
     */
    public Configuration setREUSEADDR(final Boolean _soReuseAddress) {
        Preconditions.checkNotNull(_soReuseAddress);
        this.soReuseAddress = _soReuseAddress;
        return this;
    }

    /**
     * Set socket TCP_NO_DELAY flag.
     * @param _tcpNoDelay - flag value
     * @return configuration object
     */
    public Configuration setTCPNODELAY(final Boolean _tcpNoDelay) {
        Preconditions.checkNotNull(_tcpNoDelay);
        this.tcpNoDelay = _tcpNoDelay;
        return this;
    }

    /**
     * Set max online sessions parameter.
     * @param _maxSessions - max sessions number
     * @throws IllegalArgumentException - if _maxSessions < 0
     * @return configuration object
     */
    public Configuration setMaxSessions(final int _maxSessions) {
        Preconditions.checkArgument(_maxSessions >= 0);
        maxSessions = _maxSessions;
        return this;
    }

    /**
     * Set command parser.
     * @param _parser - parser object
     * @throws NullPointerException - _parser parameter is null
     * @return configuration object
     */
    public Configuration setParser(final ICommandParserFactory _parser) {
        Preconditions.checkNotNull(_parser);
        parser = _parser;
        return this;
    }

    @Override
    public Object clone() {
        Configuration conf = new Configuration(this.address, this.port);
        return conf.setRCVBUF(soRcvBuf).setSoSndBuf(soSndBuf).setREUSEADDR(soReuseAddress).setTCPNODELAY(tcpNoDelay).
                setMaxSessions(maxSessions).setParser(parser);
    }

    /**
     * Get IP address.
     * @return address
     */
    public InetAddress getAddress() {
        return address;
    }

    /**
     * Get port value.
     * @return - port value
     */
    public int getPort() {
        return port;
    }

    /**
     * Get socket SND_BUF size parameter.
     * @return SND_BUF size parameter
     */
    public Integer getSoSndBuf() {
        return soSndBuf;
    }

    /**
     * Get socket RCV buffer size parameter.
     * @return RCV_BUF size
     */
    public Integer getSoRcvBuf() {
        return soRcvBuf;
    }

    /**
     * Get socket reuse address option.
     * @return socket reuse address option
     */
    public Boolean getSoReuseAaddr() {
        return soReuseAddress;
    }

    /**
     * Get socket TCP NO_DELAY option.
     * @return socket NO_DELAY option
     */
    public Boolean getTcpNoDelay() {
        return tcpNoDelay;
    }

    /**
     * Get max allowed session value.
     * @return - value
     */
    public int getMaxSessions() {
        return maxSessions;
    }

    /**
     * Get command parser object.
     * @return parser
     */
    public ICommandParserFactory getParser() {
        return parser;
    }

    /**
     * Get greeting message.
     * @return greeting message
     */
    public String getGreeting() {
        return greeting;
    }

    /**
     * Set greeting message.
     * @param _greeting - greeting message
     */
    public void setGreeting(final String _greeting) {
        Preconditions.checkNotNull(_greeting);
        this.greeting = _greeting;
    }

    /**
     * Get prompt message.
     * @return prompt message
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * Set prompt message.
     * @param _prompt - prompt message
     */
    public void setPrompt(final String _prompt) {
        this.prompt = _prompt;
    }
}
