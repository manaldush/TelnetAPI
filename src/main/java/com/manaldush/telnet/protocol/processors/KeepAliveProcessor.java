package com.manaldush.telnet.protocol.processors;

import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.protocol.Constants;
import com.manaldush.telnet.protocol.ITelnetCommandProcessor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by Maxim.Melnikov on 26.06.2017.
 */
public final class KeepAliveProcessor implements ITelnetCommandProcessor {

    private static final byte[] KEEP_ALIVE_BYTES = {(byte) Constants.IAC, (byte)Constants.NOP};
    private static final ByteBuffer KEEP_ALIVE_BUFFER = ByteBuffer.allocate(2);
    static {
        KEEP_ALIVE_BUFFER.put(KEEP_ALIVE_BYTES);
    }
    private final SocketChannel channel;

    private KeepAliveProcessor(final SocketChannel _channel) {
        channel = _channel;
    }

    public static KeepAliveProcessor build(final SocketChannel _channel) {
        return new KeepAliveProcessor(_channel);
    }

    @Override
    public void process() throws IOException, GeneralTelnetException {
        channel.write(KEEP_ALIVE_BUFFER);
    }
}
