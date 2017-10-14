package com.manaldush.telnet;

import com.manaldush.telnet.exceptions.AbortOutputProcessException;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.exceptions.InterruptProcessException;
import com.manaldush.telnet.options.OptionState;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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

    /**
     * Return state of option on client.
     * @param _val - option type
     * @return - option state
     */
    OptionState getOptionClientState(byte _val);

    /**
     * Return state of option on server.
     * @param _val - option type
     * @return - option state
     */
    OptionState getOptionServerState(byte _val);

    /**
     * Set option state on a client.
     * @param _val - option value
     * @param _state - state of option
     */
    void setOptionClientState(byte _val, OptionState _state);

    /**
     * Set option state on a server.
     * @param _val - option value
     * @param _state - state of option
     */
    void setOptionServerState(byte _val, OptionState _state);

    /**
     * Sub negotiation process.
     * @param _val - value of option
     * @param _b - sub negotiation bytes between option value and SE command
     */
    void subNegotiation(byte _val, List<Byte> _b, Charset _charset);
}
