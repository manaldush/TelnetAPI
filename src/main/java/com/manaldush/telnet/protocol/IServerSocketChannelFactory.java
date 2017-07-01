package com.manaldush.telnet.protocol;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;

/**
 * Created by Maxim.Melnikov on 29.06.2017.
 */
public interface IServerSocketChannelFactory {
    ServerSocketChannel build() throws IOException;
}
