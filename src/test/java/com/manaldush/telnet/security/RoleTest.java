package com.manaldush.telnet.security;

import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class RoleTest {
    private static final String role_1 = "role_1";
    private static final String role_2 = "role_2";
    private static final String role_3 = "role_3";

    @AfterClass
    public static void release() {
        Role.clear();
    }

    @Test
    public void build() throws Exception {
        Role.build(role_1);
        Role.build(role_2);
        Role.build(role_2);
        assertTrue(Role.containRole(role_1));
        assertTrue(Role.containRole(role_2));
        assertFalse(Role.containRole(role_3));
        // role_1, role_2, and system role
        assertTrue(Role.size() == 3);
        Role role = Role.getRole(role_1);
        assertFalse(role == null);
        Role.clear();
        // system role
        assertTrue(Role.size() == 1);
        assertTrue(Role.getRole(role_1) == null);
    }

}