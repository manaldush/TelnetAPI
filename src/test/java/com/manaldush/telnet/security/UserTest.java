package com.manaldush.telnet.security;

import org.junit.AfterClass;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class UserTest {
    private static final String roleName_1 = "role_1";
    private static final String roleName_2 = "role_2";

    @AfterClass
    public static void release() {
        Role.clear();
        User.clear();
    }

    @Test
    public void build() throws Exception {
        Role.build(roleName_1);
        Role.build(roleName_2);
        Role role_1 = Role.getRole(roleName_1);
        Role role_2 = Role.getRole(roleName_2);
        Set<String> roles = new HashSet<>();
        roles.add(roleName_1);
        User.build("name", "passwd", roles);
        User user = User.checkUser("name", "passwd");
        assertFalse(user == null);
        assertTrue(user.hasRole(roleName_1));
        assertFalse(user.hasRole(roleName_2));
        assertTrue(user.hasRole(role_1));
        assertFalse(user.hasRole(role_2));
        user = User.checkUser("name", "passwd1");
        assertTrue(user == null);
    }

}