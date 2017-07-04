package com.manaldush.telnet.protocol.processors;


import com.google.common.base.Preconditions;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.IClientSession;
import com.manaldush.telnet.protocol.ITelnetCommandProcessor;

import java.io.IOException;

public final class InterruptionProcessor implements ITelnetCommandProcessor {

    private final IClientSession session;

    private InterruptionProcessor(final IClientSession _session) {
        session = _session;
    }

    public static InterruptionProcessor build(final IClientSession _session) {
        Preconditions.checkNotNull(_session);
        return new InterruptionProcessor(_session);
    }


    @Override
    public void process() throws IOException, GeneralTelnetException {
        session.interruptCurrentTask();
    }
}
