package com.manaldush.telnet.protocol.processors;

import com.manaldush.telnet.IClientSession;
import com.manaldush.telnet.exceptions.OperationException;
import com.manaldush.telnet.protocol.ITelnetCommandProcessor;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class SubNegotiationEndCommand implements ITelnetCommandProcessor {
    private final IClientSession session;
    private final List<Byte> subNegotiation;
    private final byte option;
    private final Charset charset;

    private SubNegotiationEndCommand(IClientSession _session, List<Byte> _subNegotiation, byte _opt, Charset _charset) {
        session = _session;
        subNegotiation = _subNegotiation;
        option = _opt;
        charset = _charset;
    }

    public static SubNegotiationEndCommand build(IClientSession _session, List<Byte> _subNegotiation, byte _opt,
                                                 Charset _charset) {
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
