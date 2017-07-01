package com.manaldush.telnet.protocol;

import com.manaldush.telnet.IWriterAdapter;
import org.junit.Test;
import org.mockito.Mockito;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;

/**
 * Created by Maxim.Melnikov on 29.06.2017.
 */
public class ImplWriterAdapterTest {

    @Test
    public void test_1() throws IOException {
        SocketChannel channel = Mockito.mock(SocketChannel.class);
        ISession session = Mockito.mock(ISession.class);
        IWriterAdapter adapter = ImplWriterAdapter.build(session, channel);
        adapter.write("test");
        Mockito.verify(channel).write(any(ByteBuffer.class));
        Mockito.verify(session, never()).close();
    }

    @Test
    public void test_2() throws IOException {
        SocketChannel channel = Mockito.mock(SocketChannel.class);
        ISession session = Mockito.mock(ISession.class);
        IWriterAdapter adapter = ImplWriterAdapter.build(session, channel);
        Mockito.when(channel.write(any(ByteBuffer.class))).thenThrow(IOException.class);
        try {
            adapter.write("test");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Mockito.verify(channel).write(any(ByteBuffer.class));
        Mockito.verify(session).close();
    }
}