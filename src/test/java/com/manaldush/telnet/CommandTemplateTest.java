package com.manaldush.telnet;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Maxim.Melnikov on 27.06.2017.
 */
public class CommandTemplateTest {
    @Test
    public void test_1() {
        CommandTemplate template = CommandTemplate.build("command test", "description", new ICommandProcessorFactory() {
            @Override
            public ICommandProcessor build(Command _cmd) {
                return null;
            }
        });
        assertTrue(template.getCommand().compareTo("command test") == 0);
        assertTrue(template.getDescription().compareTo("description") == 0);
        assertTrue(template.getCommandProcessorFactory() != null);
        assertTrue(template.getFullDescription().compareTo("description\r\n") == 0);
        assertTrue(template.hasOption("test") == null);
    }

    @Test
    public void test_2() {
        CommandOption option = CommandOption.build("test", "option description");
        List<CommandOption> options = new ArrayList<>();
        options.add(option);
        CommandTemplate template = CommandTemplate.build("command test", "description", options, new ICommandProcessorFactory() {
            @Override
            public ICommandProcessor build(Command _cmd) {
                return null;
            }
        });
        assertTrue(template.getCommand().compareTo("command test") == 0);
        assertTrue(template.getDescription().compareTo("description") == 0);
        assertTrue(template.getCommandProcessorFactory() != null);
        assertTrue(template.getFullDescription().compareTo("description\r\noption description\r\n") == 0);
        assertTrue(template.hasOption("test") == option);
        assertTrue(template.hasOption("failed") == null);
    }
}