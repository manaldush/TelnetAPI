package com.manaldush.telnet.protocol.processors;

import com.manaldush.telnet.IClientSession;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.options.Option;
import com.manaldush.telnet.options.OptionState;
import com.manaldush.telnet.protocol.Constants;
import com.manaldush.telnet.protocol.ITelnetCommandProcessor;
import java.io.IOException;

/**
 * Created by Maxim.Melnikov on 26.06.2017.
 */
public final class NegotiationOptionsProcessor implements ITelnetCommandProcessor {
    private final byte option;
    private final IClientSession session;
    private enum COMMAND_TYPE {WILL, WILL_NOT, DO, DO_NOT}
    private final COMMAND_TYPE cmd;

    private NegotiationOptionsProcessor(COMMAND_TYPE _cmd, byte _option, IClientSession _session) {
        cmd = _cmd;
        option = _option;
        session = _session;
    }

    public static NegotiationOptionsProcessor buildDO(final byte _option, IClientSession _session) {
        return new NegotiationOptionsProcessor(COMMAND_TYPE.DO, _option, _session);
    }

    public static NegotiationOptionsProcessor buildDONOT(final byte _option, IClientSession _session) {
        return new NegotiationOptionsProcessor(COMMAND_TYPE.DO_NOT, _option, _session);
    }

    public static NegotiationOptionsProcessor buildWILL(final byte _option, IClientSession _session) {
        return new NegotiationOptionsProcessor(COMMAND_TYPE.WILL, _option, _session);
    }

    public static NegotiationOptionsProcessor buildWILLNOT(final byte _option, IClientSession _session) {
        return new NegotiationOptionsProcessor(COMMAND_TYPE.WILL_NOT, _option, _session);
    }

    @Override
    public void process() throws IOException, GeneralTelnetException {
        Option opt = session.getOption(option);
        if (cmd == COMMAND_TYPE.WILL) {
            processWillCommand(opt);
        } else if (cmd == COMMAND_TYPE.WILL_NOT) {
            processWillNotCommand(opt);
        } else if (cmd == COMMAND_TYPE.DO) {
            processDoCommand(opt);
        } else if (cmd == COMMAND_TYPE.DO_NOT) {
            processDoNotCommand(opt);
        }
    }

    private void processWillCommand(Option opt) throws IOException {
        if (opt.isClientSupported()) {
            if (opt.getClientState() == OptionState.DISABLE) {
                // approve
                byte[] response = {(byte) Constants.IAC, (byte) Constants.DO, option};
                session.write(response);
                opt.setClientState(OptionState.ENABLE);
            } else if (opt.getClientState() == OptionState.ENABLING) {
                opt.setClientState(OptionState.ENABLE);
            } else if (opt.getClientState() == OptionState.ENABLE) {
                // skip
            } else if (opt.getClientState() == OptionState.DISABLING) {
                // client want to continue use option
                opt.setClientState(OptionState.ENABLE);
            }
        } else {
            // not supported
            byte[] response = {(byte) Constants.IAC, (byte) Constants.DO_NOT, option};
            session.write(response);
        }
    }

    private void processWillNotCommand(Option opt) throws IOException {
        if (opt.isClientSupported()) {
            if (opt.getClientState() == OptionState.DISABLE) {
                //skip
            } else if (opt.getClientState() == OptionState.ENABLING) {
                opt.setClientState(OptionState.DISABLE);
            } else if (opt.getClientState() == OptionState.ENABLE) {
                byte[] response = {(byte) Constants.IAC, (byte) Constants.DO_NOT, option};
                session.write(response);
                opt.setClientState(OptionState.DISABLE);
            } else if (opt.getClientState() == OptionState.DISABLING) {
                opt.setClientState(OptionState.DISABLE);
            }
        } else {
            //skip
        }
    }

    private void processDoCommand(Option opt) throws IOException {
        if (opt.isServerSupported()) {
            if (opt.getServerState() == OptionState.DISABLE) {
                // approve
                byte[] response = {(byte) Constants.IAC, (byte) Constants.WILL, option};
                session.write(response);
                opt.setServerState(OptionState.ENABLE);
            } else if (opt.getServerState() == OptionState.ENABLING) {
                opt.setServerState(OptionState.ENABLE);
            } else if (opt.getServerState() == OptionState.ENABLE) {
                // skip
            } else if (opt.getServerState() == OptionState.DISABLING) {
                opt.setServerState(OptionState.ENABLE);
            }
        } else {
            // not supported
            byte[] response = {(byte) Constants.IAC, (byte) Constants.WILL_NOT, option};
            session.write(response);
        }
    }

    private void processDoNotCommand(Option opt) throws IOException {
        if (opt.isServerSupported()) {
            if (opt.getServerState() == OptionState.DISABLE) {
                //skip
            } else if (opt.getServerState() == OptionState.ENABLING) {
                opt.setServerState(OptionState.DISABLE);
            } else if (opt.getServerState() == OptionState.ENABLE) {
                byte[] response = {(byte) Constants.IAC, (byte) Constants.WILL_NOT, option};
                session.write(response);
                opt.setServerState(OptionState.DISABLE);
            } else if (opt.getServerState() == OptionState.DISABLING) {
                opt.setServerState(OptionState.DISABLE);
            }
        } else {
            //skip
        }
    }
}
