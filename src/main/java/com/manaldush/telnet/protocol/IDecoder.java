package com.manaldush.telnet.protocol;

import com.manaldush.telnet.exceptions.GeneralTelnetException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Interface for decoder input stream.
 * Created by Maxim.Melnikov on 27.06.2017.
 */
interface IDecoder {
    /**
     * Decode method, return list of received lines.
     * @param _buffer - buffer for decoding
     * @param _bytesNum - number of bytes
     * @return list of decoded lines
     * @throws GeneralTelnetException - any telnet error
     * @throws IOException -  I/O errors
     */
    List<String> decode(final ByteBuffer _buffer, final int _bytesNum) throws GeneralTelnetException, IOException;
}
