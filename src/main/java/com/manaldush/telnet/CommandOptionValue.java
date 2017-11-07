package com.manaldush.telnet;

import com.google.common.base.Preconditions;

/**
 * Option value for command.
 * Created by Maxim.Melnikov on 06.06.2017.
 */
public final class CommandOptionValue {
    /**Option value.*/
    private final String value;
    /**Command option.*/
    private final CommandOption option;

    /**
     * Construct command option value object.
     *
     * @param _value - option value
     * @param _option - option object
     */
    private CommandOptionValue(final String _value, final CommandOption _option) {
        value = _value;
        option = _option;
    }

    /**
     * Construct command option value object. Set value = null.
     *
     * @param _option - option object
     */
    private CommandOptionValue(final CommandOption _option) {
        value = null;
        option = _option;
    }

    /**
     * Build Command option value object.
     *
     * @param _value - option value
     * @param _option - option object
     * @throws NullPointerException - _value or _option are null
     * @return command option value object
     */
    public static CommandOptionValue build(final String _value, final CommandOption _option) {
        Preconditions.checkNotNull(_option);
        Preconditions.checkNotNull(_value);
        return new CommandOptionValue(_value, _option);
    }

    /**
     * Build Command option value object.Set value = null.
     *
     * @param _option - option object
     * @return command option value object
     */
    public static CommandOptionValue build(final CommandOption _option) {
        Preconditions.checkNotNull(_option);
        return new CommandOptionValue(_option);
    }

    /**
     * Get option value.
     *
     * @return option value
     */
    public String getValue() {
        return value;
    }

    /**
     * Get option.
     *
     * @return command option
     */
    public CommandOption getOption() {
        return option;
    }
}
