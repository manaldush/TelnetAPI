package com.manaldush.telnet.exceptions;

/**
 * Parent exception for this package exceptions.
 * Created by Maxim.Melnikov on 07.06.2017.
 */
class GeneralTelnetException extends Exception {

    GeneralTelnetException() {
        super();
    }

    GeneralTelnetException(String msg) {
        super(msg);
    }

    GeneralTelnetException(String message, Throwable cause) {
        super(message, cause);
    }

    GeneralTelnetException(Throwable cause) {
        super(cause);
    }
}
