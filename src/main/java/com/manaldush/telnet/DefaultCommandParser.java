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
    private final String cmdPart;
    private final Map<String, String> options;
    private DefaultCommandParser(final String _cmdPart, final Map<String, String> _options) {
        cmdPart = _cmdPart;
        options = _options;
    }
    static DefaultCommandParser build(final String _cmd) throws ParseException {
        Preconditions.checkNotNull(_cmd);
        String trimCmd = _cmd.trim();
        Preconditions.checkArgument(!trimCmd.isEmpty());
        return new DefaultCommandParser(parseCommand(_cmd), parseOptions(_cmd));
    }

    private static String parseCommand(String cmd) throws ParseException {
        int end = cmd.indexOf(0x2D);
        if (end == -1) return cmd;
        String cmdPart = cmd.substring(0, end);
        cmdPart = cmdPart.trim();
        if (cmdPart.isEmpty()) throw new ParseException("Command string is empty", 0);
        return cmdPart;
    }

    private static Map<String, String> parseOptions(String cmd) throws ParseException {
        int end = cmd.indexOf(0x2D);
        if (end == -1) return null;
        String optPart = cmd.substring(end);
        optPart = optPart.trim();
        if (optPart.isEmpty()) return null;
        String[] attrs = optPart.split(" ");
        Map<String, String> options = new HashMap<>();
        for (String attr : attrs) {
            if (!attr.startsWith("-") || attr.compareTo("-") == 0) {
                throw new ParseException(String.format("Command [%s]: illegal option format [%s], should start from '-'", cmd, attr), 0);
            }
            attr = attr.substring(1);
            String[] kv = attr.split("=");
            if (kv.length != 2) {
                throw new ParseException(String.format("Command [%s]: illegal option format [%s]", cmd, attr), 0);
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
     * Parse options string and return map<option,value>
     * @return map<option,value>
     * @throws ParseException
     */
    @Override
    public Map<String, String> parseOptions() throws ParseException {
        return options;
    }
}
