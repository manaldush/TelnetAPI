package com.manaldush.telnet.protocol;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;

/**
 * Created by Maxim.Melnikov on 29.06.2017.
 */
final class ImplServerSocketChannel implements IServerSocketChannelFactory {
    @Override
    public ServerSocketChannel build() throws IOException {
        return ServerSocketChannel.open();
    }
}
