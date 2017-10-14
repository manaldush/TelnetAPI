package com.manaldush.telnet.options;

import com.google.common.base.Preconditions;
import com.manaldush.telnet.IClientSession;

import java.nio.charset.Charset;
import java.util.List;

public abstract class Option {
    private final byte bValue;
    private final int iValue;

    protected Option(byte _v) {
        bValue = _v;
        iValue = bValue & 0xFF;
    }

    private OptionState clientState = OptionState.DISABLE;
    private OptionState serverState = OptionState.DISABLE;

    public OptionState getClientState() {
        return clientState;
    }

    public void setClientState(OptionState clientState) {
        this.clientState = clientState;
    }

    public OptionState getServerState() {
        return serverState;
    }

    public void setServerState(OptionState serverState) {
        this.serverState = serverState;
    }

    public byte getByteValue() {
        return bValue;
    }

    public int getIntValue() {return iValue;}

    public abstract void setSubnegotiation(List<Byte> _b, IClientSession _session, Charset _charset);
}
