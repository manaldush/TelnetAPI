package com.manaldush.telnet.protocol;

import com.manaldush.telnet.exceptions.GeneralTelnetException;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.junit.Assert.assertTrue;

/**
 * Created by Maxim.Melnikov on 30.06.2017.
 */
public class ImplTelnetSessionTest {
    @Test
    public void test_buffer() {
        // Buffer method check
        SocketChannel channel = Mockito.mock(SocketChannel.class);
        ImplTelnetSession session = new ImplTelnetSession(channel, new ImplController(), 3);
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
        ImplTelnetSession session = new ImplTelnetSession(channel, new ImplController(), 3);
        IDecoder decoder = Mockito.mock(IDecoder.class);
        Field field = session.getClass().getDeclaredField("decoder");
        field.setAccessible(true);
        field.set(session, decoder);
        session.decode(ByteBuffer.allocate(4).put((byte)0x56), 1);
        Mockito.verify(decoder).decode(Mockito.any(ByteBuffer.class), Mockito.anyInt());
    }
}