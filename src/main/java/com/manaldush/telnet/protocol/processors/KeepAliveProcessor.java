package com.manaldush.telnet.protocol.processors;

import com.manaldush.telnet.IClientSession;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.protocol.Constants;
import com.manaldush.telnet.protocol.ITelnetCommandProcessor;
import java.io.IOException;

/**
 * Created by Maxim.Melnikov on 26.06.2017.
 */
public final class KeepAliveProcessor implements ITelnetCommandProcessor {

    private static final byte[] KEEP_ALIVE_BYTES = {(byte) Constants.IAC, (byte)Constants.NOP};
    private final IClientSession session;

    private KeepAliveProcessor(final IClientSession _session) {
        session = _session;
    }

    public static KeepAliveProcessor build(final IClientSession _session) {
        return new KeepAliveProcessor(_session);
    }

    @Override
    public void process() throws IOException, GeneralTelnetException {
        session.write(KEEP_ALIVE_BYTES);
    }
}
