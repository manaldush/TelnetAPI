package com.manaldush.telnet;

import com.google.common.base.Preconditions;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A specific execution for the command.
 *
 * Created by Maxim.Melnikov on 19.06.2017.
 */
public final class Command {
    /**Command template object.*/
    private final CommandTemplate template;
    /**Map of command option -> command option value.*/
    private final Map<CommandOption, CommandOptionValue> optionValues = new HashMap<>();

    /**
     * Constructor of command.
     * @param _template - template of command
     * @param _options - list of options
     */
    private Command(final CommandTemplate _template, final List<CommandOptionValue> _options) {
        template     = _template;
        if (!(_options == null || _options.isEmpty())) {
            for (CommandOptionValue value : _options) {
                optionValues.put(value.getOption(), value);
            }
        }
    }

    /**
     * Create Command object.
     * @param _template - template of command
     * @param _options - options list, can be null/empty object
     * @throws ParseException - unknown option
     * @throws NullPointerException - template is null object
     * @return Command object
     */
    public static Command build(final CommandTemplate _template, final List<CommandOptionValue> _options)
            throws ParseException {
        Preconditions.checkNotNull(_template);
        if (!(_options == null || _options.isEmpty())) {
            for (CommandOptionValue value : _options) {
                String optName = value.getOption().getOption();
                if (_template.hasOption(value.getOption().getOption()) == null) {
                    throw new ParseException(String.format("Command [%s], illegal option [%s]", _template.getCommand(),
                            optName), 0);
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
     * @throws NullPointerException - template is null object
     */
    public static Command build(final CommandTemplate _template, final Map<String, String> _options)
            throws ParseException {
        Preconditions.checkNotNull(_template);
        if (_options == null || _options.isEmpty()) {
            return new Command(_template, null);
        } else {
            List<CommandOptionValue> optValues = null;
            for (Map.Entry<String, String> pair : _options.entrySet()) {
                CommandOption option = _template.hasOption(pair.getKey());
                if (option == null) {
                    throw new ParseException(String.format("Command [%s], illegal option [%s]", _template.getCommand(),
                            pair.getKey()), 0);
                } else if (optValues == null) {
                    optValues = new ArrayList<>();
                }
                optValues.add(CommandOptionValue.build(pair.getValue(), option));
            }
            return new Command(_template, optValues);
        }
    }

    /**
     * Build command object. Options set to null.
     * @param _template - command template
     * @throws NullPointerException - _template is null object
     * @return command
     */
    public static Command build(final CommandTemplate _template) {
        Preconditions.checkNotNull(_template);
        return new Command(_template, null);
    }

    /**
     * Get template object from command.
     *
     * @return template object of command
     */
    public CommandTemplate getTemplate() {
        return template;
    }

    /**
     * Get option value by CommandOption object.
     *
     * @param _option - option
     * @return CommandOptionValue object or null
     * @throws NullPointerException - if _option is null
     */
    public CommandOptionValue getOptionValue(final CommandOption _option) {
        Preconditions.checkNotNull(_option);
        return optionValues.get(_option);
    }
}
