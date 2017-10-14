package com.manaldush.telnet.security;

import com.manaldush.telnet.CommandTemplate;
import com.manaldush.telnet.IClientSession;
import com.manaldush.telnet.ICommandProcessorFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class AuthTelnetClientSessionTest {
    private static final String username = "test";
    private static final String passwd = "test";

    @BeforeClass
    public static void init() {
        Role.build("test");
        Role.build("test2");
    }

    @AfterClass
    public static void release() {
        Role.clear();
    }

    @Test
    public void test() {
        IClientSession session = mock(IClientSession.class);
        AuthTelnetClientSession authSession = new AuthTelnetClientSession(session);
        assertFalse(authSession.isAuthFailed());
        assertFalse(authSession.hasPasswd());
        assertFalse(authSession.hasUserName());
        authSession.setUserName(username);
        assertFalse(authSession.isAuthFailed());
        assertFalse(authSession.hasPasswd());
        assertTrue(authSession.hasUserName());
        authSession.setPasswd(passwd);
        assertFalse(authSession.isAuthFailed());
        assertTrue(authSession.hasPasswd());
        assertTrue(authSession.hasUserName());
        Set<String> roles = new HashSet<>();
        roles.add("test");
        User.build(username, passwd, roles);
        authSession.checkUser();
        assertFalse(authSession.isAuthFailed());
        assertTrue(authSession.hasPasswd());
        assertTrue(authSession.hasUserName());

        CommandTemplate cmdTemplate = CommandTemplate.build("cmd", "description", mock(ICommandProcessorFactory.class));
        cmdTemplate.addRole("test");
        assertTrue(authSession.checkRoles(cmdTemplate));

        // check unknown role
        cmdTemplate = CommandTemplate.build("cmd", "description", mock(ICommandProcessorFactory.class));
        cmdTemplate.addRole("test2");
        assertFalse(authSession.checkRoles(cmdTemplate));
    }
}