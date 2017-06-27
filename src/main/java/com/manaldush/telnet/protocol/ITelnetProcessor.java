package com.manaldush.telnet.protocol;

import com.manaldush.telnet.exceptions.OperationException;

import java.io.IOException;

/**
 * Created by Maxim.Melnikov on 26.06.2017.
 */
public interface ITelnetProcessor {
    void process() throws OperationException, IOException;
}
