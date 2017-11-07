package com.manaldush.telnet.security;

import com.google.common.base.Preconditions;
import java.util.HashSet;
import java.util.Set;

/**
 * Describe role object, applicable for user.
 *
 * Created by Maxim.Melnikov on 19.06.2017.
 */
public final class Role {
    /**Role name.*/
    private final String name;
    /**Available roles set.*/
    private static final Set<Role> ROLES = new HashSet<>();
    /**Name of system role, available for all users by default. This role is used for availability of such system
     * commands as 'help' or 'quit'.*/
    public static final String SYSTEM_ROLE = "system";
    static {
        Role.build(SYSTEM_ROLE);
    }

    /**
     * Construct role object.
     * @param _name - name of created role
     * @throws NullPointerException - if role name is null object
     * @throws IllegalArgumentException - if role name is empty
     */
    Role(final String _name) {
        Preconditions.checkNotNull(_name, "Role name must not be null");
        Preconditions.checkArgument(!_name.isEmpty(), "Role name must not be empty");
        name = _name.intern();
    }

    /**
     * Create role with name=_name and add role in role list.
     *
     * @param _name - role name
     */
    public static void build(final String _name) {
        ROLES.add(new Role(_name));
    }

    @Override
    public boolean equals(final Object _o) {
        Role r = (Role) _o;
        return r.name.equals(this.name) ? true : false;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    /**
     * Check role exist.
     * @param _name - name of role
     * @return - TRUE/FALSE
     */
    public static boolean containRole(final String _name) {
        return ROLES.contains(new Role(_name));
    }

    /**
     * Return role with name=_name or null if role with _name does not exist.
     * @param _name - name of role
     * @return role or null
     */
    static Role getRole(final String _name) {
        Role r = new Role(_name);
        if (ROLES.contains(r)) {
            return r;
        }
        return null;
    }

    /**
     * Reset roles list, only default system role will be exist.
     */
    public static void clear() {
        ROLES.clear();
        Role.build(SYSTEM_ROLE);
    }

    /**
     * Size of roles list.
     * @return size
     */
    static int size() {
        return ROLES.size();
    }

    /**
     * Get name of role.
     * @return role name
     */
    public String getName() {
        return name;
    }
}
