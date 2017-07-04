package com.manaldush.telnet.protocol.processors;

import com.manaldush.telnet.IClientSession;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.protocol.Constants;
import com.manaldush.telnet.protocol.ITelnetCommandProcessor;
import java.io.IOException;

/**
 * Created by Maxim.Melnikov on 26.06.2017.
 */
public final class NegotiationOptionsProcessor implements ITelnetCommandProcessor {
    private final int option;
    private final IClientSession session;
    private enum COMMAND_TYPE {WILL, WILL_NOT, DO, DO_NOT}
    private final COMMAND_TYPE cmd;

    private NegotiationOptionsProcessor(COMMAND_TYPE _cmd, int _option, IClientSession _session) {
        cmd = _cmd;
        option = _option;
        session = _session;
    }

    public static NegotiationOptionsProcessor buildDO(final int _option, IClientSession _session) {
        return new NegotiationOptionsProcessor(COMMAND_TYPE.DO, _option, _session);
    }

    public static NegotiationOptionsProcessor buildDONOT(final int _option, IClientSession _session) {
        return new NegotiationOptionsProcessor(COMMAND_TYPE.DO_NOT, _option, _session);
    }

    public static NegotiationOptionsProcessor buildWILL(final int _option, IClientSession _session) {
        return new NegotiationOptionsProcessor(COMMAND_TYPE.WILL, _option, _session);
    }

    public static NegotiationOptionsProcessor buildWILLNOT(final int _option, IClientSession _session) {
        return new NegotiationOptionsProcessor(COMMAND_TYPE.WILL_NOT, _option, _session);
    }

    @Override
    public void process() throws IOException, GeneralTelnetException {
        if (cmd == COMMAND_TYPE.WILL || cmd == COMMAND_TYPE.WILL_NOT) {
            byte[] response = {(byte)Constants.IAC, (byte)Constants.DO_NOT, (byte)option};
            session.write(response);
        } else if (cmd == COMMAND_TYPE.DO || cmd == COMMAND_TYPE.DO_NOT) {
            byte[] response = {(byte)Constants.IAC, (byte)Constants.WILL_NOT, (byte)option};
            session.write(response);
        }
    }
}
