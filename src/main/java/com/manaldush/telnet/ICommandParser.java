package com.manaldush.telnet;

import java.text.ParseException;
import java.util.Map;

/**
 * Parser of telnet commands, realization of this interface describe style of telnet command and extract command text and
 * options values from command string.
 * Created by Maxim.Melnikov on 27.06.2017.
 */
public interface ICommandParser {
    /**
     * Return command text.
     * @return - result string
     * @throws ParseException - parse exception error
     */
    String parseCommand() throws ParseException;

    /**
     * Parse options string and return map<option,value>
     * @return map<option,value>
     * @throws ParseException
     */
    Map<String, String> parseOptions() throws ParseException;
}
