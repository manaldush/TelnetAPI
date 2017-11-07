package com.manaldush.telnet;

import com.google.common.base.Preconditions;
import com.manaldush.telnet.security.Role;
import com.manaldush.telnet.security.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Template of telnet command, describe commands which system will process.
 * Created by Maxim.Melnikov on 06.06.2017.
 */
public final class CommandTemplate {
    /**CRLF chars.*/
    private static final String CRLF = "\r\n";
    /**Command part.*/
    private final String command;
    /**Description of command.*/
    private final String description;
    /**Expanded description.*/
    private final String fullDescription;
    /**Processor of command.*/
    private final ICommandProcessorFactory commandProcessorFactory;
    /**List of available options.*/
    private final List<CommandOption> options;
    /**Tab char, used for creation expanded description.*/
    private static final String INDENT = "    ";
    /**DASH char constant.*/
    private static final String DASH = " - ";
    /**Accepted roles.*/
    private final HashSet<String> roles = new HashSet<>();

    /**
     * Construct command template object.
     *
     * @param _command - command part
     * @param _description - description object
     * @param _options - options list
     * @param _commandProcessorFactory - command processor
     */
    private CommandTemplate(final String _command, final String _description, final List<CommandOption> _options,
                            final ICommandProcessorFactory _commandProcessorFactory) {
        this.command = _command;
        this.description = _description;
        this.options = _options;
        String fullDescriptionBuffer = command + DASH + this.description;
        if (this.options != null) {
            for (CommandOption option : this.options) {
                fullDescriptionBuffer = fullDescriptionBuffer + CRLF + INDENT + INDENT + option.getOption()
                        + DASH + option.getDescription();
            }
        }
        this.fullDescription = fullDescriptionBuffer;
        commandProcessorFactory = _commandProcessorFactory;
    }

    /**
     * Create Telnet command object.
     *
     * @param _command - telnet command
     * @param _description - description
     * @param _options - опции команды
     * @param _commandProcessorFactory - command processor
     * @throws IllegalArgumentException - if command part string is empty
     * @throws NullPointerException - if command part string is null, if description is is null
     * @return telnet command
     */
    public static CommandTemplate build(final String _command, final String _description,
                                        final List<CommandOption> _options,
                                        final ICommandProcessorFactory _commandProcessorFactory) {
        Preconditions.checkNotNull(_command);
        Preconditions.checkNotNull(_description);
        Preconditions.checkArgument(!_command.isEmpty());
        if (_options == null || _options.isEmpty()) {
            return new CommandTemplate(_command, _description, null, _commandProcessorFactory);
        } else {
            return new CommandTemplate(_command, _description, new ArrayList<>(_options), _commandProcessorFactory);
        }
    }

    /**
     * Create Telnet command object.
     *
     * @param _command - telnet command
     * @param _description - description
     * @param _commandProcessorFactory - command processor
     * @throws IllegalArgumentException - if command part string is empty
     * @throws NullPointerException - if command part string is null, if description is is null
     * @return telnet command
     */
    public static CommandTemplate build(final String _command, final String _description,
                                        final ICommandProcessorFactory _commandProcessorFactory) {
        return CommandTemplate.build(_command, _description, null, _commandProcessorFactory);
    }

    /**
     * Return description of command.
     * @return telnet command description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Return expanded description of command, + options description.
     * @return full telnet command description
     */
    public String getFullDescription() {
        return this.fullDescription;
    }

    /**
     * Return command execution pattern.
     * @return - command string
     */
    public String getCommand() {
        return command;
    }

    /**
     * Get command processor factory object.
     * @return command processor
     */
    public ICommandProcessorFactory getCommandProcessorFactory() {
        return commandProcessorFactory;
    }

    /**
     * Check option is valid for this command.
     * @param _name - name of option
     * @return command option
     */
    public CommandOption hasOption(final String _name) {
        if (options == null) {
            return null;
        }
        for (CommandOption option:options) {
            if (option.getOption().compareTo(_name) == 0) {
                return option;
            }
        }
        return null;
    }

    /**
     * Add role for command template.
     * @param _r - role
     */
    public void  addRole(final String _r) {
        if (Role.containRole(_r)) {
            this.roles.add(_r);
        }
    }

    /**
     * Check if user has access for execution this command.
     * @param _user - user object
     * @return TRUE/FALSE
     */
    public boolean hasAccess(final User _user) {
        for (String role: roles) {
            if (_user.hasRole(role)) {
                return true;
            }
        }
        return false;
    }
}
