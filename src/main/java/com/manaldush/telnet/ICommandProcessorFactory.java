package com.manaldush.telnet;

/**
 * Created by Maxim.Melnikov on 20.06.2017.
 */
public interface ICommandProcessorFactory {
    /**
     * Create command processor object.
     * @param _cmd - command object
     * @return command processor object
     */
    ICommandProcessor build(Command _cmd);
}
