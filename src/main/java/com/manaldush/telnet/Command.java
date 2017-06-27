package com.manaldush.telnet;

import com.google.common.base.Preconditions;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A specific execution for the command.
 * Created by Maxim.Melnikov on 19.06.2017.
 */
public final class Command {
    private final CommandTemplate template;
    private final Map<CommandOption, CommandOptionValue> optionValues = new HashMap<>();

    private Command(CommandTemplate _template, List<CommandOptionValue> _options) {
        template     = _template;
        if (_options == null || _options.isEmpty()) {
        } else {
            for (CommandOptionValue value : _options) {
                optionValues.put(value.getOption(), value);
            }
        }
    }

    /**
     * Create Command object.
     * @param _template - template of command
     * @param _options - options list, can be null/empty object
     * @return Command
     */
    public static Command build(CommandTemplate _template, List<CommandOptionValue> _options) throws ParseException {
        Preconditions.checkNotNull(_template);
        if (_options == null || _options.isEmpty()) {
        } else {
            for (CommandOptionValue value : _options) {
                String optName = value.getOption().getOption();
                if (_template.hasOption(value.getOption().getOption()) == null) {
                    throw new ParseException(String.format("Command [%s], illegal option [%s]", _template.getCommand(), optName), 0);
                }
            }
        }
        return new Command(_template, _options);
    }

    /**
     * Create Command object.
     * @param _template - template of command
     * @param _options - key/value map of options
     * @return command
     * @throws ParseException - unknown option
     */
    public static Command build(CommandTemplate _template, Map<String, String> _options) throws ParseException {
        Preconditions.checkNotNull(_template);
        if (_options == null || _options.isEmpty()) {
            return new Command(_template, null);
        } else {
            List<CommandOptionValue> optValues = null;
            for (Map.Entry<String, String> pair : _options.entrySet()) {
                CommandOption option = _template.hasOption(pair.getKey());
                if (option == null) {
                    throw new ParseException(String.format("Command [%s], illegal option [%s]", _template.getCommand(), pair.getKey()), 0);
                } else if (optValues == null) {
                    optValues = new ArrayList<>();
                }
                optValues.add(CommandOptionValue.build(pair.getValue(), option));
            }
            return new Command(_template, optValues);
        }
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
}
