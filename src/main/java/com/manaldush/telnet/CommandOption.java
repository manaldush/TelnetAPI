package com.manaldush.telnet;

import com.google.common.base.Preconditions;

/**
 * Option attribute of telnet command.
 * Created by Maxim.Melnikov on 06.06.2017.
 */
public final class CommandOption {
    private final static String OPTION_REGEXP = "[a-z]";
    /**Name of option.*/
    private final String option;
    /**Option Description.*/
    private final String description;

    private CommandOption(final String _option, final String _description) {
        option = _option;
        description = _description;
    }

    /**
     * Create option attribute for telnet command.
     * @param _option - name
     * @param _description - description
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

    public String getOption() {
        return option;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int hashCode() {
        return option.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CommandOption)) {
            return false;
        }
        CommandOption option = (CommandOption) obj;
        return option.getOption().compareTo(this.getOption()) == 0;
    }
}
