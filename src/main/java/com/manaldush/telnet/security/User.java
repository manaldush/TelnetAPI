package com.manaldush.telnet.security;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class User {
    private final String name;
    private final String passwd;
    private final Set<Role> roles;
    private static final Map<String, User> USERS = new HashMap<>();

    private User(String _name, String _passwd, Set<Role> _roles) {
        name = _name;
        passwd = _passwd;
        roles = _roles;
    }

    public static void build(String _name, String _passwd, Set<String> _roles) {
        checkParameters(_name, _passwd);
        Preconditions.checkNotNull(_roles, "Roles list is empty");
        Preconditions.checkArgument(_roles != null && !_roles.isEmpty(), "Roles list is empty");
        Preconditions.checkArgument(!USERS.containsKey(_name), String.format("User = [%s] has been already created", _name));
        Set<Role> roles = new HashSet<>();
        roles.add(Role.getRole(Role.SYSTEM_ROLE));
        for (String entry:_roles) {
            Preconditions.checkNotNull(entry, "Roles list contain null role");
            Role role = Role.getRole(entry);
            Preconditions.checkNotNull(role, String.format("Role with name [%s] was not registered", entry));
            roles.add(role);
        }
        USERS.put(_name.intern(), new User(_name.intern(), _passwd.intern(), roles));
    }

    public boolean hasRole(String _name) {
        return roles.contains(new Role(_name));
    }

    public boolean hasRole(Role _role) {
        return roles.contains(_role);
    }

    @Override
    public boolean equals(Object _o) {
        User user = (User) _o;
        return this.name.equals(user.name);
    }

    public static User checkUser(String _name, String _passwd) {
        if (_name == null || _passwd == null) return null;
        if (_name.isEmpty() || _passwd.isEmpty()) return null;
        User user = USERS.get(_name);
        if (user == null) return user;
        if (user.passwd.equals(_passwd)) return user;
        else return null;
    }

    public static void clear() {
        USERS.clear();
    }

    private static void checkParameters(String _name, String _passwd) {
        Preconditions.checkNotNull(_name, "User name can not be null");
        Preconditions.checkNotNull(_passwd, "Password can not be null");
        Preconditions.checkArgument(!_name.isEmpty(), "User name can not be empty");
        Preconditions.checkArgument(!_passwd.isEmpty(), "Password can not be empty");
    }

}
