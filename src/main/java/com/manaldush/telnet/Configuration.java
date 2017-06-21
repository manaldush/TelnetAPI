package com.manaldush.telnet;

import com.google.common.base.Preconditions;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Maxim.Melnikov on 20.06.2017.
 */
public final class Configuration implements Cloneable {
    /**Maximum Number of simultaneous user sessions, default value = 10, 0 is not limited.*/
    private int maxSessions = 0;
    /**Server address*/
    private final InetAddress address;
    private final int port;

    private Integer SO_SNDBUF = 1024;

    private Integer SO_RCVBUF = 1024;

    private Boolean SO_REUSEADDR = Boolean.FALSE;

    private Boolean TCP_NODELAY = Boolean.FALSE;

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
        Preconditions.checkArgument(_port >= 65535);
        return new Configuration(InetAddress.getByName(_addr), _port);
    }

    public Configuration setSNDBUF(final Integer SO_SNDBUF) {
        this.SO_SNDBUF = SO_SNDBUF;
        return this;
    }

    public Configuration setRCVBUF(final Integer SO_RCVBUF) {
        this.SO_RCVBUF = SO_RCVBUF;
        return this;
    }

    public Configuration setREUSEADDR(final Boolean SO_REUSEADDR) {
        this.SO_REUSEADDR = SO_REUSEADDR;
        return this;
    }

    public Configuration setTCPNODELAY(final Boolean TCP_NODELAY) {
        this.TCP_NODELAY = TCP_NODELAY;
        return this;
    }

    public Configuration setMaxSessions(int _maxSessions) {
        maxSessions = _maxSessions;
        return this;
    }

    @Override
    public Object clone() {
        Configuration conf = new Configuration(this.address, this.port);
        return conf.setRCVBUF(SO_RCVBUF).setSNDBUF(SO_SNDBUF).setREUSEADDR(SO_REUSEADDR).setTCPNODELAY(TCP_NODELAY).setMaxSessions(maxSessions);
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
}
