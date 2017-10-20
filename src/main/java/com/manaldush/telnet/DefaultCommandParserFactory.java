package com.manaldush.telnet;

import java.text.ParseException;

/**
 * Implementation of factory for default Command Parser.
 *
 * Created by Maxim.Melnikov on 27.06.2017.
 */
final class DefaultCommandParserFactory implements ICommandParserFactory {
    /**
     * Constructor factory object.
     */
    DefaultCommandParserFactory() {
    }

    /**
     * Build factory object.
     *
     * @param cmd - string representation of command
     * @return command parser
     * @throws ParseException - any parse string error
     */
    @Override
    public ICommandParser build(final String cmd) throws ParseException {
        return DefaultCommandParser.build(cmd);
    }
}
