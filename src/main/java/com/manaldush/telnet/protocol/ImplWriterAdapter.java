package com.manaldush.telnet.protocol;

import com.manaldush.telnet.IWriterAdapter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * Adapter interface for writing entity.
 * Created by Maxim.Melnikov on 29.06.2017.
 */
final class ImplWriterAdapter implements IWriterAdapter {

    private final ISession session;
    private final SocketChannel channel;
    private static final Charset DEFAULT_CHARSET = Charset.forName("ASCII");

    private ImplWriterAdapter(ISession _session, SocketChannel _channel) {
        session = _session;
        channel = _channel;
    }

    /**
     * Create implementation of writer adapter.
     * @param _session - session object
     * @param _channel - channel object
     * @return
     */
    static ImplWriterAdapter build(ISession _session, SocketChannel _channel) {
        return new ImplWriterAdapter(_session, _channel);
    }

    @Override
    public void write(String _msg) throws IOException {
        byte[] b = _msg.getBytes(DEFAULT_CHARSET);
        ByteBuffer buffer = ByteBuffer.allocate(b.length + 2);
        buffer.put(b);
        buffer.put((byte)Constants.CR);
        buffer.put((byte)Constants.LF);
        buffer.flip();
        innerWrite(buffer);
    }

    @Override
    public void write(byte[] _b) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(_b.length);
        buffer.put(_b);
        innerWrite(buffer);
    }

    private void innerWrite(ByteBuffer buffer) throws IOException {
        try {
            channel.write(buffer);
        } catch (IOException ex) {
            session.close();
            throw ex;
        }
    }
}
