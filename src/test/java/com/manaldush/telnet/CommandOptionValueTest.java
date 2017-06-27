package com.manaldush.telnet;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Maxim.Melnikov on 27.06.2017.
 */
public class CommandOptionValueTest {
    @Test
    public void test_1() {
        CommandOption option = CommandOption.build("test", "description");
        CommandOptionValue optionValue = CommandOptionValue.build("val", option);
        assertTrue(optionValue.getValue().compareTo("val") == 0);
        assertTrue(optionValue.getOption() == option);

        optionValue = CommandOptionValue.build(option);
        assertTrue(optionValue.getValue() == null);
        assertTrue(optionValue.getOption() == option);
    }
}