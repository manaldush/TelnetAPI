package com.manaldush.telnet.protocol.processors;


import com.google.common.base.Preconditions;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.protocol.ISession;
import com.manaldush.telnet.protocol.ITelnetCommandProcessor;

import java.io.IOException;

public final class InterruptionProcessor implements ITelnetCommandProcessor {

    private final ISession session;

    private InterruptionProcessor(final ISession _session) {
        session = _session;
    }

    public static InterruptionProcessor build(final ISession _session) {
        Preconditions.checkNotNull(_session);
        return new InterruptionProcessor(_session);
    }


    @Override
    public void process() throws IOException, GeneralTelnetException {
        session.interruptCurrentTask();
    }
}
