package com.manaldush.telnet;

/**
 * Created by Maxim.Melnikov on 19.06.2017.
 */

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

/**
 * Description of telnet command.
 * Created by Maxim.Melnikov on 06.06.2017.
 */
public final class CommandTemplate {
    private final static String CRLF = "\r\n";
    private final String command;
    /**Description of command.*/
    private final String description;
    private final String fullDescription;
    private final ICommandProcessorFactory commandProcessorFactory;
    /**List of available options.*/
    private final List<CommandOption> options;

    private CommandTemplate(String _command, String _description, final List<CommandOption> _options,
                            final ICommandProcessorFactory _commandProcessorFactory) {
        this.command = _command;
        this.description = _description;
        this.options = _options;
        String fullDescriptionBuffer = this.description + CRLF;
        if (this.options != null) {
            for (CommandOption option : this.options) {
                fullDescriptionBuffer = fullDescriptionBuffer + option.getDescription() + CRLF;
            }
        }
        this.fullDescription = fullDescriptionBuffer;
        commandProcessorFactory = _commandProcessorFactory;
    }

    /**
     * Create Telnet command object
     * @param _command - telnet command
     * @param _description - description
     * @param _options - опции команды
     * @return telnet command
     */
    public static CommandTemplate build(final String _command, final String _description, final List<CommandOption> _options,
                                        final ICommandProcessorFactory _commandProcessorFactory) {
        Preconditions.checkNotNull(_command);
        Preconditions.checkNotNull(_description);
        Preconditions.checkArgument(!_command.isEmpty());
        if (_options == null || _options.isEmpty())
            return CommandTemplate.build(_command, _description, _commandProcessorFactory);
        else
            return new CommandTemplate(_command, _description, new ArrayList<CommandOption>(_options), _commandProcessorFactory);
    }

    /**
     * Create Telnet command object
     * @param _command - telnet command
     * @param _description - description
     * @return telnet command
     */
    public static CommandTemplate build(final String _command, final String _description,
                                        final ICommandProcessorFactory _commandProcessorFactory) {
        return CommandTemplate.build(_command, _description, null, _commandProcessorFactory);
    }

    /**
     * Возвращает краткое описание команды, без описания опций запуска.
     * @return telnet command description
     */
    protected String getDescription() {
        return this.description;
    }

    /**
     * Возвращает полное описание команды, с описанием опций запуска.
     * @return full telnet command description
     */
    protected String getFullDescription() {
        return this.fullDescription;
    }

    /**
     * Return command execution pattern.
     * @return - command string
     */
    public String getCommand() {
        return command;
    }

    public ICommandProcessorFactory getCommandProcessorFactory() {
        return commandProcessorFactory;
    }
}