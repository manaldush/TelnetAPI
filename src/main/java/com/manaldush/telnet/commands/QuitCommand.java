package com.manaldush.telnet.commands;

import com.google.common.base.Preconditions;
import com.manaldush.telnet.IClientSession;
import com.manaldush.telnet.ICommandProcessor;
import com.manaldush.telnet.exceptions.AbortOutputProcessException;
import com.manaldush.telnet.exceptions.InterruptProcessException;
import com.manaldush.telnet.exceptions.OperationException;

import java.io.IOException;

/**
 * Quit command.
 * Created by Maxim.Melnikov on 03.07.2017.
 */
public final class QuitCommand implements ICommandProcessor {

    private IClientSession session;

    private QuitCommand(final IClientSession _session) {
        session = _session;
    }

    /**
     * Build QuitCommand object.
     * @param _session - client session
     * @throws NullPointerException - if session is null object
     * @return QuitCommand
     */
    public static QuitCommand build(final IClientSession _session) {
        Preconditions.checkNotNull(_session);
        return new QuitCommand(_session);
    }

    /**
     * Process quit command execution.
     * @throws OperationException - telnet operation exception
     * @throws IOException - I/O error
     */
    @Override
    public void process() throws OperationException, IOException {
        session.close();
    }

    @Override
    public void abortOutput() throws AbortOutputProcessException {

    }

    @Override
    public void interruptProcess() throws InterruptProcessException {

    }
}
