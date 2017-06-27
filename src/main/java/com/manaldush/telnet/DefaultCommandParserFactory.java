package com.manaldush.telnet;

import java.text.ParseException;

/**
 * Created by Maxim.Melnikov on 27.06.2017.
 */
public final class DefaultCommandParserFactory implements ICommandParserFactory {
    public DefaultCommandParserFactory() {}

    @Override
    public ICommandParser build(String _cmd) throws ParseException {
        return DefaultCommandParser.build(_cmd);
    }
}
