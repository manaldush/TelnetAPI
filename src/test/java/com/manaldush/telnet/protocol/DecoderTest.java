package com.manaldush.telnet.protocol;

import com.manaldush.telnet.IClientSession;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.protocol.processors.KeepAliveProcessor;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;

/**
 * Created by Maxim.Melnikov on 27.06.2017.
 */
public class DecoderTest {

    @Test
    public void test_1() throws GeneralTelnetException, IOException {
        IClientSession session = Mockito.mock(IClientSession.class);
        Decoder decoder = new Decoder(session);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put("test\r\n".getBytes());
        ByteBuffer bufferResponse = ByteBuffer.allocate(100);
        bufferResponse.put("test".getBytes());
        Mockito.when(session.getBuffer()).thenReturn(bufferResponse);
        List<String> res = decoder.decode(buffer, 6);
        assertTrue(res.size() == 1);
        assertTrue("test".compareTo(res.get(0)) == 0);
    }

    @Test
    public void test_2() throws GeneralTelnetException, IOException {
        IClientSession session = Mockito.mock(IClientSession.class);
        Decoder decoder = new Decoder(session);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put("test".getBytes());
        assertTrue(decoder.decode(buffer, 4).size() == 0);
        buffer = ByteBuffer.allocate(100);
        buffer.put(" command\r\n".getBytes());

        ByteBuffer bufferResponse = ByteBuffer.allocate(100);
        bufferResponse.put("test command".getBytes());
        Mockito.when(session.getBuffer()).thenReturn(bufferResponse);
        List<String> decodedLines = decoder.decode(buffer, 10);
        assertTrue(decodedLines.size() == 1);
        assertTrue(decodedLines.get(0).compareTo("test command") == 0);

        buffer = ByteBuffer.allocate(100);
        buffer.put("test\r\n".getBytes());
        bufferResponse = ByteBuffer.allocate(100);
        bufferResponse.put("test".getBytes());
        Mockito.when(session.getBuffer()).thenReturn(bufferResponse);
        decodedLines = decoder.decode(buffer, 6);
        assertTrue(decodedLines.size() == 1);
        assertTrue("test".compareTo(decodedLines.get(0)) == 0);
    }

    @Test
    public void test_3() throws GeneralTelnetException, IOException, NoSuchFieldException, IllegalAccessException {
        // test command Are_You_There
        IClientSession session = Mockito.mock(IClientSession.class);
        Decoder decoder = new Decoder(session);
        Field field = KeepAliveProcessor.class.getDeclaredField("KEEP_ALIVE_BYTES");
        field.setAccessible(true);
        byte[] response = (byte[]) field.get(KeepAliveProcessor.class);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.Are_You_There};
        buffer.put(cmd);
        List<String> decodedLines = decoder.decode(buffer, 2);
        Mockito.verify(session).write(response);
        assertTrue(decodedLines.size() == 0);
    }

    @Test
    public void test_4() throws GeneralTelnetException, IOException {
        // test command Abort_Output
        IClientSession session = Mockito.mock(IClientSession.class);
        Decoder decoder = new Decoder(session);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.Abort_Output};
        buffer.put(cmd);
        List<String> decodedLines = decoder.decode(buffer, 2);
        Mockito.verify(session).abortCurrentTask();
        assertTrue(decodedLines.size() == 0);
    }

    @Test
    public void test_5() throws GeneralTelnetException, IOException {
        // test command Interrupt_Process
        IClientSession session = Mockito.mock(IClientSession.class);
        Decoder decoder = new Decoder(session);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.Interrupt_Process};
        buffer.put(cmd);
        List<String> decodedLines = decoder.decode(buffer, 2);
        Mockito.verify(session).interruptCurrentTask();
        assertTrue(decodedLines.size() == 0);
    }

    @Test
    public void test_6() throws GeneralTelnetException, IOException {
        // test command Erase Line
        IClientSession session = Mockito.mock(IClientSession.class);
        Decoder decoder = new Decoder(session);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.Erase_Line};
        buffer.put(cmd);
        List<String> decodedLines = decoder.decode(buffer, 2);
        Mockito.verify(session).resetBuffer();
        assertTrue(decodedLines.size() == 0);
    }

    @Test
    public void test_7() throws GeneralTelnetException, IOException {
        // test command Erase Character
        IClientSession session = Mockito.mock(IClientSession.class);
        Decoder decoder = new Decoder(session);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.Erase_character};
        buffer.put(cmd);
        List<String> decodedLines = decoder.decode(buffer, 2);
        Mockito.verify(session).eraseCharacter();
        assertTrue(decodedLines.size() == 0);
    }

    @Test
    public void test_8() throws GeneralTelnetException, IOException {
        // test command options
        // DO NOT
        IClientSession session = Mockito.mock(IClientSession.class);
        Decoder decoder = new Decoder(session);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.DO_NOT, 0x01};
        buffer.put(cmd);
        List<String> decodedLines = decoder.decode(buffer, 3);
        Mockito.verify(session).write(Mockito.any(byte[].class));
        assertTrue(decodedLines.size() == 0);

        // DO
        ByteBuffer buffer2 = ByteBuffer.allocate(100);
        byte[] cmd2 = {(byte)Constants.IAC, (byte)Constants.DO, 0x01};
        buffer2.put(cmd2);
        decodedLines = decoder.decode(buffer2, 3);
        Mockito.verify(session, times(2)).write(Mockito.any(byte[].class));
        assertTrue(decodedLines.size() == 0);

        // WILL
        ByteBuffer buffer3 = ByteBuffer.allocate(100);
        byte[] cmd3 = {(byte)Constants.IAC, (byte)Constants.WILL, 0x01};
        buffer3.put(cmd3);
        decodedLines = decoder.decode(buffer3, 3);
        Mockito.verify(session, times(3)).write(Mockito.any(byte[].class));
        assertTrue(decodedLines.size() == 0);

        // WILL_NOT
        ByteBuffer buffer4 = ByteBuffer.allocate(100);
        byte[] cmd4 = {(byte)Constants.IAC, (byte)Constants.WILL_NOT, 0x01};
        buffer4.put(cmd4);
        decodedLines = decoder.decode(buffer4, 3);
        Mockito.verify(session, times(4)).write(Mockito.any(byte[].class));
        assertTrue(decodedLines.size() == 0);
    }

    @Test
    public void test_9() throws GeneralTelnetException, IOException {
        // few telnet commands in one decoded line
        IClientSession session = Mockito.mock(IClientSession.class);
        Decoder decoder = new Decoder(session);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.Abort_Output, (byte)Constants.IAC, (byte)Constants.Interrupt_Process};
        buffer.put(cmd);
        List<String> decodedLines = decoder.decode(buffer, 4);
        Mockito.verify(session).abortCurrentTask();
        Mockito.verify(session).interruptCurrentTask();
        assertTrue(decodedLines.size() == 0);
    }

    @Test
    public void test_10() throws GeneralTelnetException, IOException {
        // few commands in one decoded line
        IClientSession session = Mockito.mock(IClientSession.class);
        Decoder decoder = new Decoder(session);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put("test1\r\n".getBytes());
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.Abort_Output, (byte)Constants.IAC, (byte)Constants.Interrupt_Process};
        buffer.put(cmd);
        buffer.put("test2\r\n".getBytes());

        ByteBuffer bufferResponse1 = ByteBuffer.allocate(100);
        bufferResponse1.put("test1".getBytes());
        ByteBuffer bufferResponse2 = ByteBuffer.allocate(100);
        bufferResponse2.put("test2".getBytes());
        Mockito.when(session.getBuffer()).thenReturn(bufferResponse1).thenReturn(bufferResponse2);

        List<String> decodedLines = decoder.decode(buffer, 18);
        Mockito.verify(session).abortCurrentTask();
        Mockito.verify(session).interruptCurrentTask();
        assertTrue(decodedLines.size() == 2);
        assertTrue(decodedLines.get(0).compareTo("test1") == 0);
        assertTrue(decodedLines.get(1).compareTo("test2") == 0);
    }

}