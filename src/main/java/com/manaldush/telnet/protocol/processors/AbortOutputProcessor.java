package com.manaldush.telnet.protocol.processors;

import com.google.common.base.Preconditions;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.protocol.ISession;
import com.manaldush.telnet.protocol.ITelnetCommandProcessor;

import java.io.IOException;

/**
 * Created by Maxim.Melnikov on 26.06.2017.
 */
public final class AbortOutputProcessor implements ITelnetCommandProcessor {
    private final ISession session;

    private AbortOutputProcessor(final ISession _session) {
        session = _session;
    }

    public static AbortOutputProcessor build(final ISession _session) {
        Preconditions.checkNotNull(_session);
        return new AbortOutputProcessor(_session);
    }

    @Override
    public void process() throws IOException, GeneralTelnetException {
        session.abortCurrentTask();
    }
}