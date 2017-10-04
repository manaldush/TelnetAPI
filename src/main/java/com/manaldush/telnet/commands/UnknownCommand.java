package com.manaldush.telnet.commands;

import com.google.common.base.Preconditions;
import com.manaldush.telnet.IClientSession;
import com.manaldush.telnet.ICommandProcessor;
import com.manaldush.telnet.exceptions.AbortOutputProcessException;
import com.manaldush.telnet.exceptions.InterruptProcessException;
import com.manaldush.telnet.exceptions.OperationException;
import com.manaldush.telnet.protocol.Constants;

import java.io.IOException;

public final class UnknownCommand implements ICommandProcessor {
    private IClientSession session;
    private final static String LOG_UNKNOWN_COMMAND = "unknown command";

    /**
     * Build UnknownCommand object.
     * @param _session - client session
     * @throws NullPointerException - if session is null object
     * @return UnknownCommand
     */
    public static UnknownCommand build(IClientSession _session) {
        Preconditions.checkNotNull(_session);
        return new UnknownCommand(_session);
    }

    private UnknownCommand(IClientSession _session) {
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
        session.write(LOG_UNKNOWN_COMMAND);
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
