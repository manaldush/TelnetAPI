package com.manaldush.telnet;

import java.text.ParseException;

/**
 * Implementation of factory for default Command Parser.
 * Created by Maxim.Melnikov on 27.06.2017.
 */
final class DefaultCommandParserFactory implements ICommandParserFactory {
    DefaultCommandParserFactory() {}

    @Override
    public ICommandParser build(String _cmd) throws ParseException {
        return DefaultCommandParser.build(_cmd);
    }
}
