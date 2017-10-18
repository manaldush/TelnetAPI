package com.manaldush.telnet.options;

import com.manaldush.telnet.IClientSession;

import java.nio.charset.Charset;
import java.util.List;

public class NotSupportedOption extends Option {
    public NotSupportedOption(byte _v) {
        super(_v, false, false);
    }

    @Override
    protected void innerSubNegotiation(List<Byte> _b, IClientSession _session, Charset _charset) {

    }
}
