package com.manaldush.telnet;

import com.google.common.base.Preconditions;

/**
 * Option value for command.
 * Created by Maxim.Melnikov on 06.06.2017.
 */
final class CommandOptionValue {
    private final String value;
    private final CommandOption option;

    private CommandOptionValue(final String _value, final CommandOption _option) {
        value = _value;
        option = _option;
    }

    private CommandOptionValue(final CommandOption _option) {
        value = null;
        option = _option;
    }

    static CommandOptionValue build(final String _value, final CommandOption _option) {
        Preconditions.checkNotNull(_option);
        Preconditions.checkNotNull(_value);
        return new CommandOptionValue(_value, _option);
    }

    static CommandOptionValue build(final CommandOption _option) {
        Preconditions.checkNotNull(_option);
        return new CommandOptionValue(_option);
    }

    public String getValue() {
        return value;
    }

    public CommandOption getOption() {
        return option;
    }
}
