package com.manaldush.telnet;

import com.manaldush.telnet.exceptions.AbortOutputProcessException;
import com.manaldush.telnet.exceptions.InterruptProcessException;
import com.manaldush.telnet.exceptions.OperationException;
import java.io.IOException;

/**
 * Processor of telnet command.
 *
 * Created by Maxim.Melnikov on 20.06.2017.
 */
public interface ICommandProcessor {
    /**
     * Process telnet command.
     *
     * @throws OperationException - any telnet operation exception
     * @throws IOException - IO errors
     */
    void process() throws OperationException, IOException;

    /**
     * Abort output of command.
     *
     * @throws AbortOutputProcessException - any telnet operation exception
     */
    void abortOutput() throws AbortOutputProcessException;

    /**
     * Interrupt current process.
     *
     * @throws InterruptProcessException - error during interruption process
     */
    void interruptProcess() throws InterruptProcessException;
}
