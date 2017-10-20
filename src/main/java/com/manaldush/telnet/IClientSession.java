package com.manaldush.telnet;

import com.manaldush.telnet.exceptions.AbortOutputProcessException;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.exceptions.InterruptProcessException;
import com.manaldush.telnet.options.Option;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Describe session object and its capabilities.
 * Session contain all information about processing context.
 *
 * Created by Maxim.Melnikov on 22.06.2017.
 */
public interface IClientSession {

    /**
     * Add byte to current read buffer of session.
     *
     * @param b - byte
     */
    void addBuffer(byte b);

    /**
     * Get buffer object.
     *
     * @return buffer
     */
    ByteBuffer getBuffer();

    /**
     * Decode byte buffer, that was read from connection.
     *
     * @param buffer - byte buffer
     * @param bytesNum - number of read bytes
     * @return list of read strings
     * @throws GeneralTelnetException - any telnet protocol error during
     * processing
     * @throws IOException - IO errors
     */
    List<String> decode(ByteBuffer buffer, int bytesNum)
            throws GeneralTelnetException, IOException;

    /**
     * Reset read buffer.
     */
    void resetBuffer();

    /**
     * Erase last character from buffer.
     */
    void eraseCharacter();

    /**
     * Create task from command and add it in queue for processing.
     *
     * @param cmd - command
     */
    void addTask(Command cmd);

    /**
     * Abort output of current executed task.
     *
     * @throws AbortOutputProcessException - if some error occurred during
     * processing output abort in current executed task
     */
    void abortCurrentTask() throws AbortOutputProcessException;

    /**
     * Interrupt current executed task.
     *
     * @throws InterruptProcessException - if some error occurred during
     * processing interruption current executed task
     */
    void interruptCurrentTask() throws InterruptProcessException;

    /**
     * Write message in connection.
     *
     * @param msg - message
     * @throws IOException - if IO problem occurred
     */
    void write(String msg) throws IOException;

    /**
     * Write bytes in connection.
     *
     * @param b - bytes
     * @throws IOException - if IO problem occurred
     */
    void write(byte[] b) throws IOException;

    /**
     * Command for close session.
     */
    void close();

    /**
     * Return option.
     *
     * @param val - option type
     * @return - option state
     */
    Option getOption(byte val);

    /**
     * Sub negotiation process.
     *
     * @param val - value of option
     * @param b - sub negotiation bytes between option value and SE command
     * @param charset - compatible charset
     */
    void subNegotiation(byte val, List<Byte> b, Charset charset);

    /**
     * Print prompt char.
     * @throws IOException - any I/O error
     */
    void prompt() throws IOException;
}
