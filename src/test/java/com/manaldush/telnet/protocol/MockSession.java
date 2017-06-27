package com.manaldush.telnet.protocol;

import com.manaldush.telnet.ICommandProcessor;
import com.manaldush.telnet.exceptions.AbortOutputProcessException;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.exceptions.InterruptProcessException;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Maxim.Melnikov on 27.06.2017.
 */
public class MockSession implements ISession {
    private ByteBuffer buffer = null;
    @Override
    public void addBuffer(byte _b) {
        if (buffer == null) buffer = ByteBuffer.allocate(1024);
        buffer.put(_b);
    }

    @Override
    public ByteBuffer getBuffer() {
        return buffer;
    }

    @Override
    public String decode(ByteBuffer _buffer, int _bytesNum) throws GeneralTelnetException, IOException {
        return null;
    }

    @Override
    public void resetBuffer() {
        buffer = null;
    }

    @Override
    public void eraseCharacter() {

    }

    @Override
    public void addTask(ICommandProcessor _task) {

    }

    @Override
    public void abortCurrentTask() throws AbortOutputProcessException {

    }

    @Override
    public void interruptCurrentTask() throws InterruptProcessException {

    }

    @Override
    public void close() {

    }
}
