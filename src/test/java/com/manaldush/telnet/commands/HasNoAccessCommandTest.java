package com.manaldush.telnet.commands;

import com.manaldush.telnet.IClientSession;
import com.manaldush.telnet.protocol.Constants;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class HasNoAccessCommandTest {
    @Test
    public void process() throws Exception {
        IClientSession session = mock(IClientSession.class);
        HasNoAccessCommand hasNoAccessCommand = HasNoAccessCommand.build(session);
        hasNoAccessCommand.process();
        verify(session, times(1)).write(Constants.RED);
        verify(session, times(1)).write(Constants.RESET_COLOR);
        Field response = HasNoAccessCommand.class.getDeclaredField("HAS_NO_ACCESS_COMMAND");
        response.setAccessible(true);
        verify(session, times(1)).write((String) response.get(HasNoAccessCommand.class));
    }

}