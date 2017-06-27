package com.manaldush.telnet;

import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.*;

/**
 * Created by Maxim.Melnikov on 27.06.2017.
 */
public class DefaultCommandParserFactoryTest {

    @Test
    public void test_1() throws ParseException {
        ICommandParser parser = new DefaultCommandParserFactory().build("test command");
        assertTrue(parser.parseCommand().compareTo("test command") == 0);
    }
}