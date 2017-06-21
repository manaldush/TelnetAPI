package com.manaldush.telnet.exceptions;

/**
 * Created by Maxim.Melnikov on 20.06.2017.
 */
public class InterruptProcessException extends GeneralTelnetException {
    public InterruptProcessException(final String _ex) {
        super(_ex);
    }
    public InterruptProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    public InterruptProcessException() {
    }
}
