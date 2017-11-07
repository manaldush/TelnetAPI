package com.manaldush.telnet.security;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Describe user object.
 *
 * Created by Maxim.Melnikov on 19.06.2017.
 */

public final class User {
    /**User name.*/
    private final String name;
    /**User password.*/
    private final String passwd;
    /**User roles.*/
    private final Set<Role> roles;
    /**Users map name -> user.*/
    private static final Map<String, User> USERS = new HashMap<>();

    /**
     * Constructor of User object.
     * @param _name - user name
     * @param _passwd - user password
     * @param _roles - roles set
     */
    private User(final String _name, final String _passwd, final Set<Role> _roles) {
        name = _name;
        passwd = _passwd;
        roles = _roles;
    }

    /**
     * Build user object. Create user and add user in user map.
     * @param _name - user name
     * @param _passwd - user password
     * @param _roles - set of roles
     * @throws NullPointerException - if _roles is null pointer
     * @throws IllegalArgumentException - if _roles is empty, if user with such name has been already created
     */
    public static void build(final String _name, final String _passwd, final Set<String> _roles) {
        checkParameters(_name, _passwd);
        Preconditions.checkNotNull(_roles, "Roles list is empty");
        Preconditions.checkArgument(!_roles.isEmpty(), "Roles list is empty");
        Preconditions.checkArgument(!USERS.containsKey(_name),
                String.format("User = [%s] has been already created", _name));
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

    /**
     * Check if role with name = _name has been added for this user.
     *
     * @param _name - role name
     * @return - TRUE/FALSE
     */
    public boolean hasRole(final String _name) {
        return roles.contains(new Role(_name));
    }

    /**
     * Check if role _role has been added for this user.
     * @param _role - role object
     * @return - TRUE/FALSE
     */
    public boolean hasRole(final Role _role) {
        return roles.contains(_role);
    }

    @Override
    public boolean equals(final Object _o) {
        User user = (User) _o;
        return this.name.equals(user.name);
    }

    /**
     * Check if user with name=_name and password=_passwd has been created.
     *
     * @param _name - name of user
     * @param _passwd - password of user
     * @return user or null object
     */
    public static User checkUser(final String _name, final String _passwd) {
        if (_name == null || _passwd == null) {
            return null;
        }
        if (_name.isEmpty() || _passwd.isEmpty()) {
            return null;
        }
        User user = USERS.get(_name);
        if (user == null) {
            return user;
        }
        if (user.passwd.equals(_passwd)) {
            return user;
        } else {
            return null;
        }
    }

    /**
     * Reset created users.
     */
    public static void clear() {
        USERS.clear();
    }

    /**
     * Check parameters name of user and password has legal format.
     *
     * @param _name - name of user
     * @param _passwd - password of user
     */
    private static void checkParameters(final String _name, final String _passwd) {
        Preconditions.checkNotNull(_name, "User name can not be null");
        Preconditions.checkNotNull(_passwd, "Password can not be null");
        Preconditions.checkArgument(!_name.isEmpty(), "User name can not be empty");
        Preconditions.checkArgument(!_passwd.isEmpty(), "Password can not be empty");
    }

}

