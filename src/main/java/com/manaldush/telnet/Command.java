package com.manaldush.telnet;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Maxim.Melnikov on 19.06.2017.
 */
public final class Command {
    private final CommandTemplate template;
    private final Map<CommandOption, CommandOptionValue> optionValues = new HashMap<CommandOption, CommandOptionValue>();

    private Command(CommandTemplate _template, List<CommandOptionValue> _options) {
        template     = _template;
        for (CommandOptionValue value: _options) {
            optionValues.put(value.getOption(), value);
        }
    }

    /**
     * Create Command object.
     * @param _template - template of command
     * @param _options - options list, can be null/empty object
     * @return Command
     */
    Command build(CommandTemplate _template, List<CommandOptionValue> _options) {
        Preconditions.checkNotNull(_template);
        return new Command(_template, _options);
    }

    public CommandTemplate getTemplate() {
        return template;
    }

    /**
     * Get option value by CommandOption object.
     * @param _option - option
     * @return CommandOptionValue object or null
     */
    public CommandOptionValue getOptionValue(final CommandOption _option) {
        Preconditions.checkNotNull(_option);
        return optionValues.get(_option);
    }

    /**
     * Get option value by CommandOption object.
     * @param _option - option
     * @return CommandOptionValue object or null
     */
    public CommandOptionValue getOptionValue(final String _option) {
        return  getOptionValue(CommandOption.build(_option, "fake description"));
    }
}
