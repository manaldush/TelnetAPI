package com.manaldush.telnet.commands;

import com.manaldush.telnet.CommandTemplate;
import com.manaldush.telnet.IClientSession;
import com.manaldush.telnet.ICommandProcessorFactory;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class HelpCommandTest {
    @Test
    public void process() throws Exception {
        IClientSession clientSession = mock(IClientSession.class);
        CommandTemplate template_1 = CommandTemplate.build("command1", "description1", mock(ICommandProcessorFactory.class));
        CommandTemplate template_2 = CommandTemplate.build("command2", "description2", mock(ICommandProcessorFactory.class));
        Map<String, CommandTemplate> templates = new HashMap<>();
        templates.put("command1", template_1);
        templates.put("command2", template_2);
        HelpCommand helpCommand = HelpCommand.build(clientSession, templates);
        helpCommand.process();
        verify(clientSession, times(2)).write(anyString());
    }

}