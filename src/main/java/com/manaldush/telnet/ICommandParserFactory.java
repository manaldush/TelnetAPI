package com.manaldush.telnet;

import java.text.ParseException;

/**
 * Factory for creation ICommandParser object.
 * Created by Maxim.Melnikov on 27.06.2017.
 */
public interface ICommandParserFactory {
    ICommandParser build(String _cmd) throws ParseException;
}
