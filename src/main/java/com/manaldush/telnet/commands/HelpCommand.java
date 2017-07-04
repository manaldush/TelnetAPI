package com.manaldush.telnet.commands;

import com.google.common.base.Preconditions;
import com.manaldush.telnet.CommandTemplate;
import com.manaldush.telnet.IClientSession;
import com.manaldush.telnet.ICommandProcessor;
import com.manaldush.telnet.exceptions.AbortOutputProcessException;
import com.manaldush.telnet.exceptions.InterruptProcessException;
import com.manaldush.telnet.exceptions.OperationException;
import java.io.IOException;
import java.util.Map;

/**
 * Created by Maxim.Melnikov on 03.07.2017.
 */
public final class HelpCommand implements ICommandProcessor {

    private final IClientSession session;
    private final Map<String, CommandTemplate> commandTemplates;

    private HelpCommand(IClientSession _session, Map<String, CommandTemplate> _commandTemplates) {
        session = _session;
        commandTemplates = _commandTemplates;
    }

    public static HelpCommand build(IClientSession _session, Map<String, CommandTemplate> _commandTemplates) {
        Preconditions.checkNotNull(_session);
        Preconditions.checkNotNull(_commandTemplates);
        return new HelpCommand(_session, _commandTemplates);
    }

    @Override
    public void process() throws OperationException, IOException {
        if (commandTemplates == null || commandTemplates.isEmpty()) return;
        for (Map.Entry<String, CommandTemplate> entry : commandTemplates.entrySet()) {
            printCommand(entry.getValue());
        }
    }

    @Override
    public void abortOutput() throws AbortOutputProcessException {

    }

    @Override
    public void interruptProcess() throws InterruptProcessException {

    }

    private void printCommand(CommandTemplate _template) throws IOException {
        session.write(_template.getFullDescription());
    }
}
