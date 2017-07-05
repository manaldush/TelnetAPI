package com.manaldush.telnet.protocol;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;

/**
 * Factory for creation server socket channel object.
 * Created by Maxim.Melnikov on 29.06.2017.
 */
public interface IServerSocketChannelFactory {
    /**
     * Build server socket channel object.
     * @return ServerSocketChannel object
     * @throws IOException - any I/O exception
     */
    ServerSocketChannel build() throws IOException;
}
