package com.manaldush.telnet.commands;

import com.google.common.base.Preconditions;
import com.manaldush.telnet.IClientSession;
import com.manaldush.telnet.ICommandProcessor;
import com.manaldush.telnet.exceptions.AbortOutputProcessException;
import com.manaldush.telnet.exceptions.InterruptProcessException;
import com.manaldush.telnet.exceptions.OperationException;
import com.manaldush.telnet.protocol.Constants;

import java.io.IOException;

/**
 * Execute when user try to execute command and do not have enough garnts for this operation.
 */
public final class HasNoAccessCommand implements ICommandProcessor {
    private static final String HAS_NO_ACCESS_COMMAND = "do not have grants for execution command";
    private final IClientSession session;

    /**
     * Build HasNoAccessCommand object.
     * @param _session - client session
     * @throws NullPointerException - if session is null object
     * @return HasNoAccessCommand
     */
    public static HasNoAccessCommand build(final IClientSession _session) {
        Preconditions.checkNotNull(_session);
        return new HasNoAccessCommand(_session);
    }

    private HasNoAccessCommand(final IClientSession _session) {
        session = _session;
    }

    /**
     * Process telnet command.
     *
     * @throws OperationException - any telnet operation exception
     * @throws IOException        - IO errors
     */
    @Override
    public void process() throws OperationException, IOException {
        session.write(Constants.RED);
        session.write(HAS_NO_ACCESS_COMMAND);
        session.write(Constants.RESET_COLOR);
    }

    /**
     * Abort output of command.
     *
     * @throws AbortOutputProcessException - any telnet operation exception
     * @throws IOException                 - IO errors
     */
    @Override
    public void abortOutput() throws AbortOutputProcessException {

    }

    /**
     * Interrupt current process.
     *
     * @throws InterruptProcessException - error during interruption process
     */
    @Override
    public void interruptProcess() throws InterruptProcessException {

    }
}
