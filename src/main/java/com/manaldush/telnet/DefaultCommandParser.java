package com.manaldush.telnet;

import com.google.common.base.Preconditions;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of ICommandParser,
 * example of command "test -i opt1 -p opt2"
 * Created by Maxim.Melnikov on 27.06.2017.
 */
final class DefaultCommandParser implements ICommandParser {
    /**Part of command without options.*/
    private final String cmdPart;
    /**Parsed options.*/
    private final Map<String, String> options;
    /**Dash char, used for parsing command.*/
    private static final int DASH_CHAR = 0x2D;

    /**
     * Build command parser object.
     * @param _cmdPart - command part of command string
     * @param _options - option's values
     */
    private DefaultCommandParser(final String _cmdPart, final Map<String, String> _options) {
        cmdPart = _cmdPart;
        options = _options;
    }

    /**
     * Build command parser object.
     * @param _cmd - command part of command string
     * @return command parser
     * @throws ParseException any parse error of command string
     */
    static DefaultCommandParser build(final String _cmd) throws ParseException {
        Preconditions.checkNotNull(_cmd);
        String trimCmd = _cmd.trim();
        Preconditions.checkArgument(!trimCmd.isEmpty());
        return new DefaultCommandParser(parseCommand(_cmd), parseOptions(_cmd));
    }

    /**
     * Build command parser object.
     * @param _cmd - command part of command string
     * @return - return command part of string
     * @throws ParseException any parse error of command string
     */
    private static String parseCommand(final String _cmd) throws ParseException {
        int end = _cmd.indexOf(DASH_CHAR);
        if (end == -1) {
            return _cmd;
        }
        String cmdPart = _cmd.substring(0, end);
        cmdPart = cmdPart.trim();
        if (cmdPart.isEmpty()) {
            throw new ParseException("Command string is empty", 0);
        }
        return cmdPart;
    }

    /**
     * Parse options string part.
     * @param _cmd - command string
     * @return options map
     * @throws ParseException - any parse error
     */
    private static Map<String, String> parseOptions(final String _cmd) throws ParseException {
        int end = _cmd.indexOf(DASH_CHAR);
        if (end == -1) {
            return null;
        }
        String optPart = _cmd.substring(end);
        optPart = optPart.trim();
        if (optPart.isEmpty()) {
            return null;
        }
        String[] attrs = optPart.split(" ");
        Map<String, String> options = new HashMap<>();
        for (String attr : attrs) {
            if (!attr.startsWith("-") || attr.compareTo("-") == 0) {
                throw new ParseException(String.format(
                        "Command [%s]: illegal option format [%s], should start from '-'", _cmd, attr), 0);
            }
            attr = attr.substring(1);
            String[] kv = attr.split("=");
            if (kv.length != 2) {
                throw new ParseException(String.format("Command [%s]: illegal option format [%s]", _cmd, attr), 0);
            } else {
                options.put(kv[0], kv[1]);
            }
        }
        return options;
    }
    /**
     * Return command text.
     * @return - result string
     * @throws ParseException - parse exception error
     */
    @Override
    public String parseCommand() throws ParseException {
        return cmdPart;
    }

    /**
     * Parse options string and return map<option,value>.
     * @return map of option name as key and option value as string
     * @throws ParseException - any parse error
     */
    @Override
    public Map<String, String> parseOptions() throws ParseException {
        return options;
    }
}
