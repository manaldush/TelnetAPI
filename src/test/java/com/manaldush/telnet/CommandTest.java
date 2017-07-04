package com.manaldush.telnet;

import org.junit.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by Maxim.Melnikov on 27.06.2017.
 */
public class CommandTest {

    @Test(expected = ParseException.class)
    public void test_1() throws ParseException {
        CommandOption option = CommandOption.build("opt", "description");
        CommandOptionValue optionValue = CommandOptionValue.build("1", option);
        List<CommandOptionValue> optionValues = new ArrayList<>();
        optionValues.add(optionValue);
        CommandTemplate template = CommandTemplate.build("test command", "description", new ICommandProcessorFactory() {
            @Override
            public ICommandProcessor build(Command _cmd, IClientSession _adapter) {
                return null;
            }
        });
        Command cmd = Command.build(template, optionValues);
        assertTrue(cmd.getTemplate() == template);
        assertTrue(cmd.getOptionValue(option).getValue().compareTo("1") == 0);
    }

    @Test
    public void test_2() throws ParseException {
        CommandOption option = CommandOption.build("opt", "description");
        CommandOptionValue optionValue = CommandOptionValue.build("1", option);
        List<CommandOptionValue> optionValues = new ArrayList<>();
        List<CommandOption> options = new ArrayList<>();
        options.add(option);
        optionValues.add(optionValue);
        CommandTemplate template = CommandTemplate.build("test command", "description", options, new ICommandProcessorFactory() {
            @Override
            public ICommandProcessor build(Command _cmd, IClientSession _adapter) {
                return null;
            }
        });
        Command cmd = Command.build(template, optionValues);
        assertTrue(cmd.getTemplate() == template);
        assertTrue(cmd.getOptionValue(option).getValue().compareTo("1") == 0);
    }

    @Test
    public void test_3() throws ParseException {
        List<CommandOption> options = new ArrayList<>();
        CommandOption option = CommandOption.build("opt", "description");
        options.add(option);
        CommandTemplate template = CommandTemplate.build("test command", "description", options, new ICommandProcessorFactory() {
            @Override
            public ICommandProcessor build(Command _cmd, IClientSession _adapter) {
                return null;
            }
        });
        Map<String, String> optionsValues = new HashMap<>();
        optionsValues.put("opt", "1");
        Command cmd = Command.build(template, optionsValues);
        assertTrue(cmd.getTemplate() == template);
        assertTrue(cmd.getOptionValue(option).getValue().compareTo("1") == 0);
    }

    @Test(expected = ParseException.class)
    public void test_4() throws ParseException {
        List<CommandOption> options = new ArrayList<>();
        CommandOption option = CommandOption.build("opt", "description");
        options.add(option);
        CommandTemplate template = CommandTemplate.build("test command", "description", options, new ICommandProcessorFactory() {
            @Override
            public ICommandProcessor build(Command _cmd, IClientSession _adapter) {
                return null;
            }
        });
        Map<String, String> optionsValues = new HashMap<>();
        optionsValues.put("opt", "1");
        optionsValues.put("fake", "1");
        Command.build(template, optionsValues);
    }
}