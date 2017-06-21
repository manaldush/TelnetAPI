package com.manaldush.telnet.exceptions;


/**
 * Base exception for telnet errors.
 * Created by Maxim.Melnikov on 06.06.2017.
 */
public class OperationException extends GeneralTelnetException {
    public OperationException(final String _ex) {
        super(_ex);
    }
    public OperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public OperationException() {
    }
}
