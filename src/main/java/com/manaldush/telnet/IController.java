package com.manaldush.telnet;

import com.manaldush.telnet.exceptions.ConfigurationException;
import java.text.ParseException;

/**
 * Describe interface of object, that control telnet module.
 * E object is implementation of interface clonable and describe configuration of controller.
 * Created by Maxim.Melnikov on 20.06.2017.
 */
public interface IController<E extends Cloneable> {
    /**
     * Registration of command in controller.
     * @param _template - command template
     */
    void register(CommandTemplate _template);
    /**
     * Unregister command in controller.
     * @param _template - command template as CommandTemplate object
     */
    void unregister(CommandTemplate _template);

    /**
     * Unregister command in controller.
     * @param _template - command template as string
     */
    void unregister(String _template);

    /**
     * Start controller/
     */
    void start();

    /**
     * Stop controller.
     */
    void stop();

    /**
     * Return command object appropriate to given String.
     * @param _command - string representation of command with options
     * @return - command
     * @throws ParseException - parse Exception of incoming String, illegal options
     */
    Command search(String _command) throws ParseException;

    /**
     * Configure Controller object.
     * @param _conf - configuration object
     */
    void configure(E _conf) throws ConfigurationException;
}
