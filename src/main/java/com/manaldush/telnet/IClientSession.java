package com.manaldush.telnet;

import com.manaldush.telnet.exceptions.AbortOutputProcessException;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.exceptions.InterruptProcessException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Describe session object and its capabilities.
 * Session contain all information about processing context.
 * Created by Maxim.Melnikov on 22.06.2017.
 */
public interface IClientSession {

    /**
     * Add buffer to current read buffer of session.
     * @param _b - byte
     */
    void addBuffer(byte _b);

    /**
     * Get buffer object.
     * @return buffer
     */
    ByteBuffer getBuffer();

    /**
     * Decode byte buffer, that was read from connection.
     * @param _buffer - byte buffer
     * @param _bytesNum - number of readed bytes
     * @return list of read strings
     * @throws GeneralTelnetException - any telnet protocol error during processing
     * @throws IOException - IO errors
     */
    List<String> decode(final ByteBuffer _buffer, final int _bytesNum) throws GeneralTelnetException, IOException;

    /**
     * Reset buffer.
     */
    void resetBuffer();

    /**
     * Erase last character from buffer.
     */
    void eraseCharacter();

    /**
     * Create task from command and add it in queue for processing.
     * @param _cmd - command
     */
    void addTask(Command _cmd);

    /**
     * Abort output of current executed task.
     * @throws AbortOutputProcessException - if some error occured during processing output abort in current executed task
     */
    void abortCurrentTask() throws AbortOutputProcessException;

    /**
     * Interrupt current executed task.
     * @throws InterruptProcessException - if some error occured during processing interruption current executed task
     */
    void interruptCurrentTask() throws InterruptProcessException;

    /**
     * Write message in connection.
     * @param _msg - message
     * @throws IOException - if IO problem occured
     */
    void write(String _msg) throws IOException;

    /**
     * Write bytes in connection.
     * @param _b - bytes
     * @throws IOException - if IO problem occured
     */
    void write(byte[] _b) throws IOException;

    /**
     * Command for close session.
     */
    void close();
}
