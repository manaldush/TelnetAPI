package com.manaldush.telnet;

import org.junit.Test;
import java.text.ParseException;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by Maxim.Melnikov on 27.06.2017.
 */
public class DefaultCommandParserTest {

    @Test
    public void test_1() throws ParseException {
        ICommandParser parser = DefaultCommandParser.build("test method");
        assertTrue(parser.parseCommand().compareTo("test method") == 0);
        parser = DefaultCommandParser.build("test method -i=arg1 -p=arg2");
        assertTrue(parser.parseCommand().compareTo("test method") == 0);
        Map<String, String> tab = parser.parseOptions();
        assertTrue(tab.get("i").compareTo("arg1") == 0);
        assertTrue(tab.get("p").compareTo("arg2") == 0);
        parser = DefaultCommandParser.build("  test method -i=arg1 -p=arg2  ");
        assertTrue(parser.parseCommand().compareTo("test method") == 0);
        tab = parser.parseOptions();
        assertTrue(tab.get("i").compareTo("arg1") == 0);
        assertTrue(tab.get("p").compareTo("arg2") == 0);
    }

    @Test(expected=ParseException.class)
    public void test_2() throws ParseException {
        DefaultCommandParser.build("test method -i arg1 -p=arg2");
    }

    @Test(expected=ParseException.class)
    public void test_3() throws ParseException {
        DefaultCommandParser.build("test method -i -p=arg2");
    }

    @Test(expected=ParseException.class)
    public void test_4() throws ParseException {
        DefaultCommandParser.build("-i=arg1 -p=arg2");
    }

    @Test(expected=ParseException.class)
    public void test_5() throws ParseException {
        DefaultCommandParser.build("test method - -p=arg2");
    }
}