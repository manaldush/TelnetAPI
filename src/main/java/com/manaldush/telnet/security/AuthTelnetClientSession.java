package com.manaldush.telnet.security;

import com.google.common.base.Preconditions;
import com.manaldush.telnet.CommandTemplate;
import com.manaldush.telnet.IClientSession;

/**
 * Wrapper of session object.Session is completed by auth parameters.
 */
public final class AuthTelnetClientSession {
    /**Client session.*/
    private final IClientSession session;
    /**Is auth failed.*/
    private boolean failed = false;
    /**User name was installed.*/
    private boolean hasUserName = false;
    /**User password was installed.*/
    private boolean hasPasswd = false;
    /**User name.*/
    private String userName;
    /**.Password value.*/
    private String passwd;
    /**User object.*/
    private User user;

    /**
     * Construct auth telnet client session.
     * @param _session - session object
     * @throws NullPointerException - if client session is null object
     */
    public AuthTelnetClientSession(final IClientSession _session) {
        Preconditions.checkNotNull(_session);
        session = _session;
    }

    /**
     * Is auth is failed or not.
     *
     * @return TRUE/FALSE
     */
    public boolean isAuthFailed() {
        return failed;
    }

    /**
     * Is user name was installed.
     *
     * @return TRUE/FALSE
     */
    public boolean hasUserName() {
        return hasUserName;
    }

    /**
     * Is password was installed.
     *
     * @return TRUE/FALSE
     */
    public boolean hasPasswd() {
        return hasPasswd;
    }

    /**
     * Set user name associated with this session.
     *
     * @param _userName - user name
     */
    public void setUserName(final String _userName) {
        userName = _userName;
        hasUserName = true;
    }

    /**
     * Set user password associated with the session.
     *
     * @param _passwd - user password
     */
    public void setPasswd(final String _passwd) {
        passwd = _passwd;
        hasPasswd = true;
    }

    /**
     * Check user exist, associated with session.
     */
    public void checkUser() {
        User u = User.checkUser(userName, passwd);
        if (u == null) {
            failed = true;
        }
        user = u;
    }

    /**
     * Return session object wrapped with current session.
     * @return client session
     */
    public IClientSession getSession() {
        return session;
    }

    /**
     * Check if associated with session user has access for CommandTemplate.
     * @param _template - command template
     * @return TRUE/FALSE
     */
    public boolean checkRoles(final CommandTemplate _template) {
        if (isAuthFailed() || !(hasUserName() && hasPasswd())) {
            return false;
        }
        return _template.hasAccess(user);
    }
}
