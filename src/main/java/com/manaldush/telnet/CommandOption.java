package com.manaldush.telnet;

import com.google.common.base.Preconditions;

/**
 * Option attribute of telnet command.
 * Created by Maxim.Melnikov on 06.06.2017.
 */
public final class CommandOption {
    /**Option name regexp.*/
    private static final String OPTION_REGEXP = "[a-z]{1,5}";
    /**Name of option.*/
    private final String option;
    /**Option Description.*/
    private final String description;

    /**
     * Construct command option object.
     * @param _option - option string name
     * @param _description - description of option
     */
    private CommandOption(final String _option, final String _description) {
        option = _option;
        description = _description;
    }

    /**
     * Create option attribute for telnet command.
     * @param _option - name
     * @param _description - description
     * @throws NullPointerException - if _option/_description is null
     * @throws IllegalArgumentException - if _option/_description is empty or _option do not match regexp
     * @return - option
     */
    public static CommandOption build(final String _option, final String _description) {
        Preconditions.checkNotNull(_option);
        Preconditions.checkNotNull(_description);
        Preconditions.checkArgument(!_description.isEmpty());
        Preconditions.checkArgument(!_option.isEmpty());
        Preconditions.checkArgument(_option.matches(OPTION_REGEXP));
        return new CommandOption(_option, _description);
    }

    /**
     * Get option name.
     * @return option name
     */
    public String getOption() {
        return option;
    }

    /**
     * Get description.
     * @return description
     */
    public String getDescription() {
        return description;
    }

    @Override
    public int hashCode() {
        return option.hashCode();
    }

    @Override
    public boolean equals(final Object _obj) {
        if (!(_obj instanceof CommandOption)) {
            return false;
        }
        CommandOption opt = (CommandOption) _obj;
        return opt.getOption().compareTo(this.getOption()) == 0;
    }
}
