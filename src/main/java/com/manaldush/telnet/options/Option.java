package com.manaldush.telnet.options;

import com.manaldush.telnet.IClientSession;
import java.nio.charset.Charset;
import java.util.List;

import static com.manaldush.telnet.protocol.Constants.BYTE_FF;

/**
 * Describe telnet protocol option.
 */
public abstract class Option {
    private final byte bValue;
    private final int iValue;
    private final boolean isClientSupported;
    private final boolean isServerSupported;

    /**
     * Option builder.
     * @param _v - option byte value
     * @param _isClientSupported - is option can be supported by a clients
     * @param _isServerSupported - is option can be supported by a server
     */
    protected Option(final byte _v, final boolean _isClientSupported, final boolean _isServerSupported) {
        bValue = _v;
        iValue = bValue & BYTE_FF;
        isClientSupported = _isClientSupported;
        isServerSupported = _isServerSupported;
    }

    private OptionState clientState = OptionState.DISABLE;
    private OptionState serverState = OptionState.DISABLE;

    /**
     * Get client state.
     * @return state
     */
    public OptionState getClientState() {
        return clientState;
    }

    /**
     * Set client option state.
     * @param _clientState - state
     */
    public void setClientState(final OptionState _clientState) {
        if (isClientSupported) {
            this.clientState = _clientState;
        } else {
            genIllegalState();
        }
    }

    /**
     * Get server state.
     * @return - server state
     */
    public OptionState getServerState() {
        return serverState;
    }

    /**
     * Get server option state.
     * @param _serverState - server option state
     */
    public void setServerState(final OptionState _serverState) {
        if (isServerSupported) {
            this.serverState = _serverState;
        } else {
            genIllegalState();
        }
    }

    /**
     * Get byte value of option.
     * @return byte option value
     */
    public byte getByteValue() {
        return bValue;
    }

    /**
     * Get integer value of option.
     * @return option int value
     */
    public int getIntValue() {
        return iValue;
    }

    /**
     * Set sub negotiation bytes, without telnet commands.
     * @param _b - list of bytes
     * @param _session - session
     * @param _charset - charset
     */
    public void setSubnegotiation(final List<Byte> _b, final IClientSession _session, final Charset _charset) {
        if (isServerSupported || isClientSupported) {
            innerSubNegotiation(_b, _session, _charset);
        } else {
            genIllegalState();
        }
    }

    /**
     * Define method for processing sub negotiation process.
     * @param _b - list of bytes
     * @param _session - session
     * @param _charset - charset
     */
    protected abstract void innerSubNegotiation(List<Byte> _b, IClientSession _session, Charset _charset);

    private void genIllegalState() {
        throw new IllegalStateException(String.format("Option [%d] is not supported", iValue));
    }

    /**
     * Is client supported.
     * @return true/false
     */
    public boolean isClientSupported() {
        return isClientSupported;
    }

    /**
     * Is server supported.
     * @return true/false
     */
    public boolean isServerSupported() {
        return isServerSupported;
    }
}
