package com.manaldush.telnet.protocol;

import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.protocol.processors.KeepAliveProcessor;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import static org.junit.Assert.*;
import static org.mockito.Mockito.times;

/**
 * Created by Maxim.Melnikov on 27.06.2017.
 */
public class DecoderTest {
    @Test
    public void test_1() throws GeneralTelnetException, IOException {
        SocketChannel channel = Mockito.mock(SocketChannel.class);
        ISession session = Mockito.mock(ISession.class);
        Decoder decoder = new Decoder(session, channel);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put("test\r\n".getBytes());
        ByteBuffer bufferResponse = ByteBuffer.allocate(100);
        bufferResponse.put("test".getBytes());
        Mockito.when(session.getBuffer()).thenReturn(bufferResponse);
        String res = decoder.decode(buffer, 6);
        assertTrue("test".compareTo(res) == 0);
    }

    @Test
    public void test_2() throws GeneralTelnetException, IOException {
        SocketChannel channel = Mockito.mock(SocketChannel.class);
        ISession session = Mockito.mock(ISession.class);
        Decoder decoder = new Decoder(session, channel);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put("test".getBytes());
        assertTrue(decoder.decode(buffer, 4) == null);
        buffer = ByteBuffer.allocate(100);
        buffer.put(" command\r\n".getBytes());
        ByteBuffer bufferResponse = ByteBuffer.allocate(100);
        bufferResponse.put("test command".getBytes());
        Mockito.when(session.getBuffer()).thenReturn(bufferResponse);
        assertTrue(decoder.decode(buffer, 10).compareTo("test command") == 0);
        buffer = ByteBuffer.allocate(100);
        buffer.put("test\r\n".getBytes());
        bufferResponse = ByteBuffer.allocate(100);
        bufferResponse.put("test".getBytes());
        Mockito.when(session.getBuffer()).thenReturn(bufferResponse);
        String res = decoder.decode(buffer, 6);
        assertTrue("test".compareTo(res) == 0);
    }

    @Test
    public void test_3() throws GeneralTelnetException, IOException, NoSuchFieldException, IllegalAccessException {
        // test command Are_You_There
        SocketChannel channel = Mockito.mock(SocketChannel.class);
        ISession session = Mockito.mock(ISession.class);
        Decoder decoder = new Decoder(session, channel);
        Field field = KeepAliveProcessor.class.getDeclaredField("KEEP_ALIVE_BUFFER");
        field.setAccessible(true);
        ByteBuffer response = (ByteBuffer) field.get(KeepAliveProcessor.class);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.Are_You_There};
        buffer.put(cmd);
        String res = decoder.decode(buffer, 2);
        Mockito.verify(channel).write(response);
        assertTrue(res == null);
    }

    @Test
    public void test_4() throws GeneralTelnetException, IOException {
        // test command Abort_Output
        SocketChannel channel = Mockito.mock(SocketChannel.class);
        ISession session = Mockito.mock(ISession.class);
        Decoder decoder = new Decoder(session, channel);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.Abort_Output};
        buffer.put(cmd);
        String res = decoder.decode(buffer, 2);
        Mockito.verify(session).abortCurrentTask();
        assertTrue(res == null);
    }

    @Test
    public void test_5() throws GeneralTelnetException, IOException {
        // test command Interrupt_Process
        SocketChannel channel = Mockito.mock(SocketChannel.class);
        ISession session = Mockito.mock(ISession.class);
        Decoder decoder = new Decoder(session, channel);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.Interrupt_Process};
        buffer.put(cmd);
        String res = decoder.decode(buffer, 2);
        Mockito.verify(session).interruptCurrentTask();
        assertTrue(res == null);
    }

    @Test
    public void test_6() throws GeneralTelnetException, IOException {
        // test command Erase Line
        SocketChannel channel = Mockito.mock(SocketChannel.class);
        ISession session = Mockito.mock(ISession.class);
        Decoder decoder = new Decoder(session, channel);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.Erase_Line};
        buffer.put(cmd);
        String res = decoder.decode(buffer, 2);
        Mockito.verify(session).resetBuffer();
        assertTrue(res == null);
    }

    @Test
    public void test_7() throws GeneralTelnetException, IOException {
        // test command Erase Character
        SocketChannel channel = Mockito.mock(SocketChannel.class);
        ISession session = Mockito.mock(ISession.class);
        Decoder decoder = new Decoder(session, channel);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.Erase_character};
        buffer.put(cmd);
        String res = decoder.decode(buffer, 2);
        Mockito.verify(session).eraseCharacter();
        assertTrue(res == null);
    }

    @Test
    public void test_8() throws GeneralTelnetException, IOException {
        // test command options
        // DO NOT
        SocketChannel channel = Mockito.mock(SocketChannel.class);
        ISession session = Mockito.mock(ISession.class);
        Decoder decoder = new Decoder(session, channel);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.DO_NOT, 0x01};
        buffer.put(cmd);
        String res = decoder.decode(buffer, 3);
        Mockito.verify(channel).write(Mockito.any(ByteBuffer.class));
        assertTrue(res == null);

        // DO
        ByteBuffer buffer2 = ByteBuffer.allocate(100);
        byte[] cmd2 = {(byte)Constants.IAC, (byte)Constants.DO, 0x01};
        buffer2.put(cmd2);
        res = decoder.decode(buffer2, 3);
        Mockito.verify(channel, times(2)).write(Mockito.any(ByteBuffer.class));
        assertTrue(res == null);

        // WILL
        ByteBuffer buffer3 = ByteBuffer.allocate(100);
        byte[] cmd3 = {(byte)Constants.IAC, (byte)Constants.WILL, 0x01};
        buffer3.put(cmd3);
        res = decoder.decode(buffer3, 3);
        Mockito.verify(channel, times(3)).write(Mockito.any(ByteBuffer.class));
        assertTrue(res == null);

        // WILL_NOT
        ByteBuffer buffer4 = ByteBuffer.allocate(100);
        byte[] cmd4 = {(byte)Constants.IAC, (byte)Constants.WILL_NOT, 0x01};
        buffer4.put(cmd4);
        res = decoder.decode(buffer4, 3);
        Mockito.verify(channel, times(4)).write(Mockito.any(ByteBuffer.class));
        assertTrue(res == null);
    }
}