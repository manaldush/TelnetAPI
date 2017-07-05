package com.manaldush.telnet.protocol;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;

/**
 * Implementation of IServerSocketChannelFactory interface. Return default Server socket channel.
 * Created by Maxim.Melnikov on 29.06.2017.
 */
final class ImplServerSocketChannel implements IServerSocketChannelFactory {
    /**
     * Build default system server socket channel.
     * @return - server socket channel object
     * @throws IOException - I/O errors
     */
    @Override
    public ServerSocketChannel build() throws IOException {
        return ServerSocketChannel.open();
    }
}
