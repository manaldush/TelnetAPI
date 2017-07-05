package com.manaldush.telnet.protocol;

import com.manaldush.telnet.exceptions.GeneralTelnetException;

import java.io.IOException;

/**
 * Interface define algorithm for processiong telnet protocol command.
 * Created by Maxim.Melnikov on 26.06.2017.
 */
public interface ITelnetCommandProcessor {
    /**
     * Algorithm for processing telnet protocol command
     * @throws IOException - I/O error
     * @throws GeneralTelnetException - some telnet protocol error
     */
    void process() throws IOException, GeneralTelnetException;
}
