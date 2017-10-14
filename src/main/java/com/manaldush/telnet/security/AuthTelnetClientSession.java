package com.manaldush.telnet.security;

import com.google.common.base.Preconditions;
import com.manaldush.telnet.CommandTemplate;
import com.manaldush.telnet.IClientSession;

import java.util.Set;

public final class AuthTelnetClientSession {
    private final IClientSession session;
    private boolean failed = false;
    private boolean hasUserName = false;
    private boolean hasPasswd = false;
    private String userName;
    private String passwd;
    private User user;

    public AuthTelnetClientSession(IClientSession _session) {
        Preconditions.checkNotNull(_session);
        session = _session;
    }

    public boolean isAuthFailed() {
        return failed;
    }

    public boolean hasUserName() {
        return hasUserName;
    }

    public boolean hasPasswd() {
        return hasPasswd;
    }

    public void setUserName(String _userName) {
        userName = _userName;
        hasUserName = true;
    }

    public void setPasswd(String _passwd) {
        passwd = _passwd;
        hasPasswd = true;
    }

    public void checkUser() {
        User u = User.checkUser(userName, passwd);
        if (u == null) failed = true;
        user = u;
    }

    public IClientSession getSession() {
        return session;
    }

    public boolean checkRoles(CommandTemplate _template) {
        if (isAuthFailed() || !(hasUserName() && hasPasswd())) return false;
        return _template.hasAccess(user);
    }
}
