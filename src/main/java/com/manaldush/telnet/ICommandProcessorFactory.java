package com.manaldush.telnet;

/**
 * Factory, create ICommandProcessor object for command and session.
 *
 * Created by Maxim.Melnikov on 20.06.2017.
 */
public interface ICommandProcessorFactory {
    /**
     * Create command processor object.
     * @param _cmd - command object
     * @param _session - Session within which the command is called
     * @return command processor object
     */
    ICommandProcessor build(Command _cmd, IClientSession _session);
}
