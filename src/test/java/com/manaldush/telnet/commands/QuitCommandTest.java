package com.manaldush.telnet.commands;

import com.manaldush.telnet.IClientSession;
import com.manaldush.telnet.exceptions.OperationException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class QuitCommandTest {
    @Test
    public void process() throws IOException, OperationException {
        IClientSession clientSession = mock(IClientSession.class);
        QuitCommand cmd = QuitCommand.build(clientSession);
        cmd.process();
        verify(clientSession, times(1)).close();
    }
}