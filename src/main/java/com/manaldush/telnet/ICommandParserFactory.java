package com.manaldush.telnet;

import java.text.ParseException;

/**
 * Factory for creation ICommandParser object.
 *
 * Created by Maxim.Melnikov on 27.06.2017.
 */
public interface ICommandParserFactory {
    /**
     * Build parser for command.
     *
     * @param cmd - string representation of command
     * @return parser for command
     * @throws ParseException - if command for string was not found
     */
    ICommandParser build(String cmd) throws ParseException;
}
