package com.manaldush.telnet.protocol.processors;

import com.manaldush.telnet.IClientSession;
import com.manaldush.telnet.exceptions.OperationException;
import com.manaldush.telnet.protocol.ITelnetCommandProcessor;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Telnet command for end processing sub negotiation telnet command.
 */
public class SubNegotiationEndCommand implements ITelnetCommandProcessor {
    private final IClientSession session;
    private final List<Byte> subNegotiation;
    private final byte option;
    private final Charset charset;

    private SubNegotiationEndCommand(final IClientSession _session, final List<Byte> _subNegotiation, final byte _opt,
                                     final Charset _charset) {
        session = _session;
        subNegotiation = _subNegotiation;
        option = _opt;
        charset = _charset;
    }

    /**
     * Builder of sub negotiation end command.
     * @param _session - session
     * @param _subNegotiation - bytes of negotiation command
     * @param _opt - negotiation option
     * @param _charset - charset
     * @return sub negotiation sub command
     */
    public static SubNegotiationEndCommand build(final IClientSession _session, final List<Byte> _subNegotiation,
                                                 final byte _opt, final Charset _charset) {
        return new SubNegotiationEndCommand(_session, _subNegotiation, _opt, _charset);
    }
    /**
     * Process telnet command.
     *
     * @throws OperationException - any telnet operation exception
     * @throws IOException        - IO errors
     */
    @Override
    public void process() throws OperationException, IOException {
        session.subNegotiation(option, subNegotiation, charset);
    }
}
