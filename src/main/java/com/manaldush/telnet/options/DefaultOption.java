package com.manaldush.telnet.options;

import com.manaldush.telnet.IClientSession;

import java.nio.charset.Charset;
import java.util.List;

public class DefaultOption extends Option {
    public DefaultOption(byte _v) {
        super(_v);
    }

    @Override
    public void setSubnegotiation(List<Byte> _b, IClientSession _session, Charset _charset) {

    }
}
