package com.manaldush.telnet.security;

import com.google.common.base.Preconditions;
import java.util.HashSet;
import java.util.Set;

public final class Role {
    private final String name;
    private static final Set<Role> ROLES = new HashSet<>();
    public static final String SYSTEM_ROLE = "system";
    static {
        Role.build(SYSTEM_ROLE);
    }

    Role(String _name) {
        Preconditions.checkNotNull(_name, "Role name must not be null");
        Preconditions.checkArgument(!_name.isEmpty(), "Role name must not be empty");
        name = _name.intern();
    }

    public static void build(String _name) {
        ROLES.add(new Role(_name));
    }

    @Override
    public boolean equals(Object _o) {
        Role r = (Role)_o;
        if (r.name.equals(this.name)) return true;
        else return false;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    public static boolean containRole(String _name) {
        return ROLES.contains(new Role(_name));
    }

    static Role getRole(String _name) {
        Role r = new Role(_name);
        if (ROLES.contains(r)) return r;
        return null;
    }

    public static void clear() {
        ROLES.clear();
        Role.build(SYSTEM_ROLE);
    }

    static int size() {
        return ROLES.size();
    }

    public String getName() {
        return name;
    }
}
