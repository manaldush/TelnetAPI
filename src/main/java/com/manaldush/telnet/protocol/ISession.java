package com.manaldush.telnet.protocol;

import com.manaldush.telnet.ICommandProcessor;
import com.manaldush.telnet.exceptions.AbortOutputProcessException;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.exceptions.InterruptProcessException;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Maxim.Melnikov on 22.06.2017.
 */
public interface ISession {

    void addBuffer(byte _b);

    ByteBuffer getBuffer();

    String decode(final ByteBuffer _buffer, final int _bytesNum) throws GeneralTelnetException, IOException;

    void resetBuffer();

    void eraseCharacter();

    void addTask(ICommandProcessor _task);

    void abortCurrentTask() throws AbortOutputProcessException;

    void interruptCurrentTask() throws InterruptProcessException;

    /**
     * Command for close session.
     */
    void close();
}
