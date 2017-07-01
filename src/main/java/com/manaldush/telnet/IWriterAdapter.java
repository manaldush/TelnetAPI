package com.manaldush.telnet;

import java.io.IOException;

/**
 * Created by Maxim.Melnikov on 29.06.2017.
 */
public interface IWriterAdapter {
    /**
     * Write message in connection.
     * @param _msg - message
     * @throws IOException - IO Exception
     */
    void write(final String _msg) throws IOException;
}
