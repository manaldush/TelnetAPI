package com.manaldush.telnet.options;

/**
 * Options states.
 */
public enum OptionState {
    /**Option is enabled.*/
    ENABLE,
    /**Option is disabled.*/
    DISABLE,
    /**Option is disabled but was started process of enabling of option.*/
    ENABLING,
    /**Option is enabled but was started process of disabling of option.*/
    DISABLING
}
