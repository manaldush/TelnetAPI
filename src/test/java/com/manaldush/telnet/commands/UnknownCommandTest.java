package com.manaldush.telnet.commands;

import com.manaldush.telnet.IClientSession;
import com.manaldush.telnet.protocol.Constants;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class UnknownCommandTest {
    @Test
    public void process() throws Exception {
        Field logUnknownCommand = UnknownCommand.class.getDeclaredField("LOG_UNKNOWN_COMMAND");
        logUnknownCommand.setAccessible(true);
        IClientSession session = mock(IClientSession.class);
        UnknownCommand unCmd = UnknownCommand.build(session);
        unCmd.process();
        verify(session, times(1)).write(Constants.RED);
        verify(session, times(1)).write(Constants.RESET_COLOR);
        verify(session, times(1)).write((String) logUnknownCommand.get(UnknownCommand.class));
    }

}