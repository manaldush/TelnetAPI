package com.manaldush.telnet.exceptions;

import com.manaldush.telnet.security.AuthTelnetClientSession;

public class AuthTelnetException extends GeneralTelnetException {
    public AuthTelnetException(String _msg) {
        super(_msg);
    }
}
