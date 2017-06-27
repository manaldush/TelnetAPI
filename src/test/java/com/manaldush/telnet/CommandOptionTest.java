package com.manaldush.telnet;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Maxim.Melnikov on 27.06.2017.
 */
public class CommandOptionTest {

    @Test
    public void test_1() {
        CommandOption option = CommandOption.build("opt", "description");
        assertTrue(option.getOption().compareTo("opt") == 0);
        assertTrue(option.getDescription().compareTo("description") == 0);
        CommandOption option_2 = CommandOption.build("opt", "description");
        assertTrue(option.hashCode() == option_2.hashCode());
        assertTrue(option.equals(option_2));
        assertFalse(option.equals(new Object()));
    }

}