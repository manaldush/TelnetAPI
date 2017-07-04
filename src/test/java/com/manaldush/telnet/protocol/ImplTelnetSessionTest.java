package com.manaldush.telnet.protocol;

import com.manaldush.telnet.IClientSession;
import com.manaldush.telnet.IController;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;

/**
 * Created by Maxim.Melnikov on 30.06.2017.
 */
public class ImplTelnetSessionTest {
    @Test
    public void test_buffer() {
        // Buffer method check
        SocketChannel channel = Mockito.mock(SocketChannel.class);
        SelectionKey key = Mockito.mock(SelectionKey.class);
        ImplTelnetClientSession session = new ImplTelnetClientSession(channel, new ImplController(), 3, key);
        session.addBuffer((byte)0x33);
        session.addBuffer((byte)0x33);
        session.addBuffer((byte)0x33);
        session.addBuffer((byte)0x33);
        ByteBuffer buffer = session.getBuffer();
        assertTrue(buffer.flip().limit() == 4);
        // check erase character
        session.eraseCharacter();
        buffer = session.getBuffer();
        assertTrue(buffer.flip().limit() == 3);
        session.resetBuffer();
        assertTrue(session.getBuffer() == null);
    }
    @Test
    public void test_decode() throws NoSuchFieldException, IllegalAccessException, GeneralTelnetException, IOException {
        SocketChannel channel = Mockito.mock(SocketChannel.class);
        SelectionKey key = Mockito.mock(SelectionKey.class);
        ImplTelnetClientSession session = new ImplTelnetClientSession(channel, new ImplController(), 3, key);
        IDecoder decoder = Mockito.mock(IDecoder.class);
        Field field = session.getClass().getDeclaredField("decoder");
        field.setAccessible(true);
        field.set(session, decoder);
        session.decode(ByteBuffer.allocate(4).put((byte)0x56), 1);
        Mockito.verify(decoder).decode(any(ByteBuffer.class), Mockito.anyInt());
    }

    @Test
    public void test_write_1() throws IOException {
        SocketChannel channel = Mockito.mock(SocketChannel.class);
        SelectionKey key = Mockito.mock(SelectionKey.class);
        ImplController controller = Mockito.mock(ImplController.class);
        IClientSession session = new ImplTelnetClientSession(channel, controller,10, key);
        session.write("test");
        Mockito.verify(channel).write(any(ByteBuffer.class));
    }

    @Test
    public void test_write_2() throws IOException {
        SelectionKey key = Mockito.mock(SelectionKey.class);
        SocketChannel channel = Mockito.mock(SocketChannel.class);
        ImplController controller = Mockito.mock(ImplController.class);
        IClientSession session = new ImplTelnetClientSession(channel, controller, 10, key);
        Mockito.when(channel.write(any(ByteBuffer.class))).thenThrow(IOException.class);
        try {
            session.write("test");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Mockito.verify(channel).write(any(ByteBuffer.class));
    }
}