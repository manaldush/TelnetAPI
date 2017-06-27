package com.manaldush.telnet.protocol;


import com.google.common.base.Preconditions;

import java.net.SocketOption;

/**
 * Created by Maxim.Melnikov on 22.06.2017.
 */
final class ImplSocketOption<T> implements SocketOption<T> {

    private final String name;
    private final Class<T> type;

    ImplSocketOption(final String _name, final Class<T> _class) {
        Preconditions.checkNotNull(_name);
        Preconditions.checkArgument(_name.isEmpty());
        name = _name;
        type = _class;
    }

    /**
     * Returns the name of the socket option.
     * @return the name of the socket option
     */
    public String name() {
        return name;
    }

    /**
     * Returns the type of the socket option value.
     * @return the type of the socket option value
     */
    public Class<T> type() {
        return type;
    }
}
