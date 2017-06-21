package com.manaldush.telnet.exceptions;


/**
 * Configuration errors exception.
 * Created by Maxim.Melnikov on 07.06.2017.
 */
public final class ConfigurationException extends GeneralTelnetException {
    ConfigurationException(final String _ex) {
        super(_ex);
    }
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
