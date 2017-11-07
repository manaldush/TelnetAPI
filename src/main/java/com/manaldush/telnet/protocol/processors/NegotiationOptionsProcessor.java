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
    /**Types of telnet command negotiation option.*/
    private enum CommandType { WILL, WILL_NOT, DO, DO_NOT }
    private final CommandType cmd;

    private NegotiationOptionsProcessor(final CommandType _cmd, final byte _option, final IClientSession _session) {
        cmd = _cmd;
        option = _option;
        session = _session;
    }

    /**
     * Create DO negotiation processor with option.
     * @param _option - option
     * @param _session - session
     * @return negotiation processor
     */
    public static NegotiationOptionsProcessor buildDO(final byte _option, final IClientSession _session) {
        return new NegotiationOptionsProcessor(CommandType.DO, _option, _session);
    }

    /**
     * Create DO NOT negotiation processor with option.
     * @param _option - option
     * @param _session - session
     * @return negotiation processor
     */
    public static NegotiationOptionsProcessor buildDONOT(final byte _option, final IClientSession _session) {
        return new NegotiationOptionsProcessor(CommandType.DO_NOT, _option, _session);
    }

    /**
     * Create WILL negotiation processor with option.
     * @param _option - option
     * @param _session - session
     * @return negotiation processor
     */
    public static NegotiationOptionsProcessor buildWILL(final byte _option, final IClientSession _session) {
        return new NegotiationOptionsProcessor(CommandType.WILL, _option, _session);
    }

    /**
     * Create WILL NOT negotiation processor with option.
     * @param _option - option
     * @param _session - session
     * @return negotiation processor
     */
    public static NegotiationOptionsProcessor buildWILLNOT(final byte _option, final IClientSession _session) {
        return new NegotiationOptionsProcessor(CommandType.WILL_NOT, _option, _session);
    }

    @Override
    public void process() throws IOException, GeneralTelnetException {
        Option opt = session.getOption(option);
        if (cmd == CommandType.WILL) {
            processWillCommand(opt);
        } else if (cmd == CommandType.WILL_NOT) {
            processWillNotCommand(opt);
        } else if (cmd == CommandType.DO) {
            processDoCommand(opt);
        } else if (cmd == CommandType.DO_NOT) {
            processDoNotCommand(opt);
        }
    }

    private void processWillCommand(final Option _opt) throws IOException {
        if (_opt.isClientSupported()) {
            if (_opt.getClientState() == OptionState.DISABLE) {
                // approve
                byte[] response = {(byte) Constants.IAC, (byte) Constants.DO, option};
                session.write(response);
                _opt.setClientState(OptionState.ENABLE);
            } else if (_opt.getClientState() == OptionState.ENABLING) {
                _opt.setClientState(OptionState.ENABLE);
            } else if (_opt.getClientState() == OptionState.DISABLING) {
                // client want to continue use option
                _opt.setClientState(OptionState.ENABLE);
            }
        } else {
            // not supported
            byte[] response = {(byte) Constants.IAC, (byte) Constants.DO_NOT, option};
            session.write(response);
        }
    }

    private void processWillNotCommand(final Option _opt) throws IOException {
        if (_opt.isClientSupported()) {
            if (_opt.getClientState() == OptionState.ENABLING) {
                _opt.setClientState(OptionState.DISABLE);
            } else if (_opt.getClientState() == OptionState.ENABLE) {
                byte[] response = {(byte) Constants.IAC, (byte) Constants.DO_NOT, option};
                session.write(response);
                _opt.setClientState(OptionState.DISABLE);
            } else if (_opt.getClientState() == OptionState.DISABLING) {
                _opt.setClientState(OptionState.DISABLE);
            }
        }
    }

    private void processDoCommand(final Option _opt) throws IOException {
        if (_opt.isServerSupported()) {
            if (_opt.getServerState() == OptionState.DISABLE) {
                // approve
                byte[] response = {(byte) Constants.IAC, (byte) Constants.WILL, option};
                session.write(response);
                _opt.setServerState(OptionState.ENABLE);
            } else if (_opt.getServerState() == OptionState.ENABLING) {
                _opt.setServerState(OptionState.ENABLE);
            } else if (_opt.getServerState() == OptionState.DISABLING) {
                _opt.setServerState(OptionState.ENABLE);
            }
        } else {
            // not supported
            byte[] response = {(byte) Constants.IAC, (byte) Constants.WILL_NOT, option};
            session.write(response);
        }
    }

    private void processDoNotCommand(final Option _opt) throws IOException {
        if (_opt.isServerSupported()) {
            if (_opt.getServerState() == OptionState.ENABLING) {
                _opt.setServerState(OptionState.DISABLE);
            } else if (_opt.getServerState() == OptionState.ENABLE) {
                byte[] response = {(byte) Constants.IAC, (byte) Constants.WILL_NOT, option};
                session.write(response);
                _opt.setServerState(OptionState.DISABLE);
            } else if (_opt.getServerState() == OptionState.DISABLING) {
                _opt.setServerState(OptionState.DISABLE);
            }
        }
    }
}
