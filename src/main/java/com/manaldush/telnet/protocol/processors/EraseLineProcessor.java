package com.manaldush.telnet.protocol.processors;

import com.google.common.base.Preconditions;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.IClientSession;
import com.manaldush.telnet.protocol.ITelnetCommandProcessor;

import java.io.IOException;

/**
 * Created by Maxim.Melnikov on 26.06.2017.
 */
public final class EraseLineProcessor implements ITelnetCommandProcessor {
    private final IClientSession session;

    private EraseLineProcessor(final IClientSession _session) {
        session = _session;
    }

    public static EraseLineProcessor build(final IClientSession _session) {
        Preconditions.checkNotNull(_session);
        return new EraseLineProcessor(_session);
    }
    @Override
    public void process() throws IOException, GeneralTelnetException {
        session.resetBuffer();
    }
}
