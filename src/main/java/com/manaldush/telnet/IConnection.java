package com.manaldush.telnet;

import com.manaldush.telnet.exceptions.OperationException;

import java.io.IOException;

/**
 * Created by Maxim.Melnikov on 20.06.2017.
 */
public interface IConnection {
    /**
     * Read data from client socket.
     * @return received String
     * @throws IOException - IO error
     */
    String read() throws IOException, OperationException;

    /**
     * Read data from client socket.
     * @return received bytes as int[]
     * @throws IOException - IO error
     */
    int[] readBytes() throws IOException, OperationException;

    /**
     * Write incoming message in socket.
     * @param _msg - message
     * @throws IOException - IO error
     */
    void write(String _msg) throws IOException;

    /**
     * Write byte array in socket.
     * @param b - byte array
     * @throws IOException - IO error
     */
    void write(int[] b) throws IOException;

    /**
     * Write byte array in socket.
     * @param b - byte array
     * @throws IOException - IO error
     */
    void write(byte[] b) throws IOException;
    /**
     * Write byte in socket.
     * @param b - byte
     * @throws IOException - IO error
     */
    void write(int b) throws IOException;
    /**
     * Write byte in socket.
     * @param b - byte
     * @throws IOException - IO error
     */
    void write(byte b) throws IOException;

    /**
     *
     * @throws IOException
     */
    void close() throws IOException;
}
