package com.manaldush.telnet.options;

import com.manaldush.telnet.IClientSession;
import java.nio.charset.Charset;
import java.util.List;

public abstract class Option {
    private final byte bValue;
    private final int iValue;
    private final boolean isClientSupported;
    private final boolean isServerSupported;

    protected Option(byte _v, boolean _isClientSupported, boolean _isServerSupported) {
        bValue = _v;
        iValue = bValue & 0xFF;
        isClientSupported = _isClientSupported;
        isServerSupported = _isServerSupported;
    }

    private OptionState clientState = OptionState.DISABLE;
    private OptionState serverState = OptionState.DISABLE;

    public OptionState getClientState() {
        return clientState;
    }

    public void setClientState(OptionState clientState) {
        if (isClientSupported) this.clientState = clientState;
        else genIllegalState();
    }

    public OptionState getServerState() {
        return serverState;
    }

    public void setServerState(OptionState serverState) {
        if (isServerSupported) this.serverState = serverState;
        else genIllegalState();
    }

    public byte getByteValue() {
        return bValue;
    }

    public int getIntValue() {return iValue;}

    public final void setSubnegotiation(List<Byte> _b, IClientSession _session, Charset _charset) {
        if (isServerSupported || isClientSupported) innerSubNegotiation(_b, _session, _charset);
        else genIllegalState();
    }

    protected abstract void innerSubNegotiation(List<Byte> _b, IClientSession _session, Charset _charset);

    private void genIllegalState() {
        throw new IllegalStateException(String.format("Option [%d] is not supported", iValue));
    }

    public boolean isClientSupported() {
        return isClientSupported;
    }

    public boolean isServerSupported() {
        return isServerSupported;
    }
}
