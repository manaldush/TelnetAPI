package com.manaldush.telnet.exceptions;

/**
 * Created by Maxim.Melnikov on 20.06.2017.
 */
public class AbortOutputProcessException extends GeneralTelnetException {
    /**
     * Create abort output exception/
     * @param _ex - message
     */
    public AbortOutputProcessException(final String _ex) {
        super(_ex);
    }
    public AbortOutputProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    public AbortOutputProcessException() {
    }
}
