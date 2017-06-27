package com.manaldush.telnet.protocol;

import com.manaldush.telnet.exceptions.GeneralTelnetException;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Maxim.Melnikov on 27.06.2017.
 */
interface IDecoder {
    String decode(final ByteBuffer _buffer, final int _bytesNum) throws GeneralTelnetException, IOException;
}
