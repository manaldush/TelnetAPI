package com.manaldush.telnet.protocol;

import com.manaldush.telnet.Command;
import com.manaldush.telnet.IClientSession;
import com.manaldush.telnet.exceptions.AbortOutputProcessException;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.exceptions.InterruptProcessException;
import com.manaldush.telnet.options.Option;
import com.manaldush.telnet.options.OptionState;
import com.manaldush.telnet.protocol.processors.KeepAliveProcessor;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyByte;
import static org.mockito.Mockito.*;

/**
 * Created by Maxim.Melnikov on 27.06.2017.
 */
public class DecoderTest {

    private static boolean test12 = false;

    @Test
    public void test_1() throws GeneralTelnetException, IOException {
        IClientSession session = Mockito.mock(IClientSession.class);
        Decoder decoder = new Decoder(session);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put("test\r\n".getBytes());
        ByteBuffer bufferResponse = ByteBuffer.allocate(100);
        bufferResponse.put("test".getBytes());
        when(session.getBuffer()).thenReturn(bufferResponse);
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
        when(session.getBuffer()).thenReturn(bufferResponse);
        List<String> decodedLines = decoder.decode(buffer, 10);
        assertTrue(decodedLines.size() == 1);
        assertTrue(decodedLines.get(0).compareTo("test command") == 0);

        buffer = ByteBuffer.allocate(100);
        buffer.put("test\r\n".getBytes());
        bufferResponse = ByteBuffer.allocate(100);
        bufferResponse.put("test".getBytes());
        when(session.getBuffer()).thenReturn(bufferResponse);
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
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.ARE_YOU_THERE};
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
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.ABORT_OUTPUT};
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
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.INTERRUPT_PROCESS};
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
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.ERASE_LINE};
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
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.ERASE_CHARACTER};
        buffer.put(cmd);
        List<String> decodedLines = decoder.decode(buffer, 2);
        Mockito.verify(session).eraseCharacter();
        assertTrue(decodedLines.size() == 0);
    }

    @Test
    public void test_8() throws GeneralTelnetException, IOException {
        // test command options
        // DO NOT command, state disable
        IClientSession session = Mockito.mock(IClientSession.class);
        Option option = mock(Option.class);
        when(option.getServerState()).thenReturn(OptionState.DISABLE);
        when(option.isServerSupported()).thenReturn(true);
        when(session.getOption(anyByte())).thenReturn(option);
        Decoder decoder = new Decoder(session);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.DO_NOT, 0x01};
        buffer.put(cmd);
        List<String> decodedLines = decoder.decode(buffer, 3);
        Mockito.verify(session, times(0)).write(any(byte[].class));
        verify(option, times(0)).setServerState(any(OptionState.class));
        assertTrue(decodedLines.size() == 0);

        // DO NOT command, state disabling
        when(option.getServerState()).thenReturn(OptionState.DISABLING);
        decodedLines = decoder.decode(buffer, 3);
        Mockito.verify(session, times(0)).write(any(byte[].class));
        verify(option, times(1)).setServerState(OptionState.DISABLE);
        assertTrue(decodedLines.size() == 0);
        reset(option);

        // DO NOT command, state enabling
        when(option.getServerState()).thenReturn(OptionState.ENABLING);
        when(option.isServerSupported()).thenReturn(true);
        decodedLines = decoder.decode(buffer, 3);
        Mockito.verify(session, times(0)).write(any(byte[].class));
        verify(option, times(1)).setServerState(OptionState.DISABLE);
        assertTrue(decodedLines.size() == 0);
        reset(option);

        // DO NOT command, state enable
        when(option.getServerState()).thenReturn(OptionState.ENABLE);
        when(option.isServerSupported()).thenReturn(true);
        decodedLines = decoder.decode(buffer, 3);
        Mockito.verify(session, times(1)).write(any(byte[].class));
        verify(option, times(1)).setServerState(OptionState.DISABLE);
        assertTrue(decodedLines.size() == 0);
        reset(option);
        reset(session);

        // option not supported
        when(option.getServerState()).thenReturn(OptionState.DISABLE);
        when(option.isServerSupported()).thenReturn(false);
        when(session.getOption(anyByte())).thenReturn(option);
        decodedLines = decoder.decode(buffer, 3);
        Mockito.verify(session, times(0)).write(any(byte[].class));
        verify(option, times(0)).setServerState(any(OptionState.class));
        assertTrue(decodedLines.size() == 0);


        // DO, state disable, option is supported
        option = mock(Option.class);
        when(option.getServerState()).thenReturn(OptionState.DISABLE);
        when(option.isServerSupported()).thenReturn(true);
        session = Mockito.mock(IClientSession.class);
        when(session.getOption(anyByte())).thenReturn(option);
        decoder = new Decoder(session);
        ByteBuffer buffer2 = ByteBuffer.allocate(100);
        byte[] cmd2 = {(byte)Constants.IAC, (byte)Constants.DO, 0x01};
        buffer2.put(cmd2);
        decodedLines = decoder.decode(buffer2, 3);
        Mockito.verify(session, times(1)).write(any(byte[].class));
        verify(option, times(1)).setServerState(OptionState.ENABLE);
        assertTrue(decodedLines.size() == 0);
        reset(session);
        reset(option);


        // DO, state disabling, option is supported
        when(option.getServerState()).thenReturn(OptionState.DISABLING);
        when(option.isServerSupported()).thenReturn(true);
        when(session.getOption(anyByte())).thenReturn(option);
        decodedLines = decoder.decode(buffer2, 3);
        Mockito.verify(session, times(0)).write(any(byte[].class));
        verify(option, times(1)).setServerState(OptionState.ENABLE);
        assertTrue(decodedLines.size() == 0);
        reset(session);
        reset(option);

        // DO, state enable, option is supported
        when(option.getServerState()).thenReturn(OptionState.ENABLE);
        when(option.isServerSupported()).thenReturn(true);
        when(session.getOption(anyByte())).thenReturn(option);
        decodedLines = decoder.decode(buffer2, 3);
        Mockito.verify(session, times(0)).write(any(byte[].class));
        verify(option, times(0)).setServerState(any(OptionState.class));
        assertTrue(decodedLines.size() == 0);
        reset(session);
        reset(option);


        // DO, state enable, option is supported
        when(option.getServerState()).thenReturn(OptionState.ENABLING);
        when(option.isServerSupported()).thenReturn(true);
        when(session.getOption(anyByte())).thenReturn(option);
        decodedLines = decoder.decode(buffer2, 3);
        Mockito.verify(session, times(0)).write(any(byte[].class));
        verify(option, times(1)).setServerState(OptionState.ENABLE);
        assertTrue(decodedLines.size() == 0);
        reset(session);
        reset(option);


        // WILL, state enable, option is supported
        ByteBuffer buffer3 = ByteBuffer.allocate(100);
        byte[] cmd3 = {(byte)Constants.IAC, (byte)Constants.WILL, 0x01};
        buffer3.put(cmd3);
        when(option.getClientState()).thenReturn(OptionState.ENABLE);
        when(option.isClientSupported()).thenReturn(true);
        when(session.getOption(anyByte())).thenReturn(option);
        decodedLines = decoder.decode(buffer3, 3);
        Mockito.verify(session, times(0)).write(any(byte[].class));
        verify(option, times(0)).setClientState(any(OptionState.class));
        assertTrue(decodedLines.size() == 0);
        reset(session);
        reset(option);

        // WILL, state enabling, option is supported
        when(option.getClientState()).thenReturn(OptionState.ENABLING);
        when(option.isClientSupported()).thenReturn(true);
        when(session.getOption(anyByte())).thenReturn(option);
        decodedLines = decoder.decode(buffer3, 3);
        Mockito.verify(session, times(0)).write(any(byte[].class));
        verify(option, times(1)).setClientState(OptionState.ENABLE);
        assertTrue(decodedLines.size() == 0);
        reset(session);
        reset(option);

        // WILL, state disable, option is supported
        when(option.getClientState()).thenReturn(OptionState.DISABLE);
        when(option.isClientSupported()).thenReturn(true);
        when(session.getOption(anyByte())).thenReturn(option);
        decodedLines = decoder.decode(buffer3, 3);
        Mockito.verify(session, times(1)).write(any(byte[].class));
        verify(option, times(1)).setClientState(OptionState.ENABLE);
        assertTrue(decodedLines.size() == 0);
        reset(session);
        reset(option);

        // WILL, state disabling, option is supported
        when(option.getClientState()).thenReturn(OptionState.DISABLING);
        when(option.isClientSupported()).thenReturn(true);
        when(session.getOption(anyByte())).thenReturn(option);
        decodedLines = decoder.decode(buffer3, 3);
        Mockito.verify(session, times(0)).write(any(byte[].class));
        verify(option, times(1)).setClientState(OptionState.ENABLE);
        assertTrue(decodedLines.size() == 0);
        reset(session);
        reset(option);

        // WILL, option is not supported
        when(option.isClientSupported()).thenReturn(false);
        when(session.getOption(anyByte())).thenReturn(option);
        decodedLines = decoder.decode(buffer3, 3);
        Mockito.verify(session, times(1)).write(any(byte[].class));
        verify(option, times(0)).setClientState(any(OptionState.class));
        assertTrue(decodedLines.size() == 0);
        reset(session);
        reset(option);


        // WILL_NOT, state ENABLE, is supported
        ByteBuffer buffer4 = ByteBuffer.allocate(100);
        byte[] cmd4 = {(byte)Constants.IAC, (byte)Constants.WILL_NOT, 0x01};
        buffer4.put(cmd4);
        when(option.getClientState()).thenReturn(OptionState.ENABLE);
        when(option.isClientSupported()).thenReturn(true);
        when(session.getOption(anyByte())).thenReturn(option);
        decodedLines = decoder.decode(buffer4, 3);
        Mockito.verify(session, times(1)).write(any(byte[].class));
        verify(option, times(1)).setClientState(OptionState.DISABLE);
        assertTrue(decodedLines.size() == 0);
        reset(session);
        reset(option);

        // WILL_NOT, state ENABLING, is supported
        when(option.getClientState()).thenReturn(OptionState.ENABLING);
        when(option.isClientSupported()).thenReturn(true);
        when(session.getOption(anyByte())).thenReturn(option);
        decodedLines = decoder.decode(buffer4, 3);
        Mockito.verify(session, times(0)).write(any(byte[].class));
        verify(option, times(1)).setClientState(OptionState.DISABLE);
        assertTrue(decodedLines.size() == 0);
        reset(session);
        reset(option);

        // WILL_NOT, state DISABLE, is supported
        when(option.getClientState()).thenReturn(OptionState.DISABLE);
        when(option.isClientSupported()).thenReturn(true);
        when(session.getOption(anyByte())).thenReturn(option);
        decodedLines = decoder.decode(buffer4, 3);
        Mockito.verify(session, times(0)).write(any(byte[].class));
        verify(option, times(0)).setClientState(any(OptionState.class));
        assertTrue(decodedLines.size() == 0);
        reset(session);
        reset(option);


        // WILL_NOT, state DISABLE, is supported
        when(option.getClientState()).thenReturn(OptionState.DISABLING);
        when(option.isClientSupported()).thenReturn(true);
        when(session.getOption(anyByte())).thenReturn(option);
        decodedLines = decoder.decode(buffer4, 3);
        Mockito.verify(session, times(0)).write(any(byte[].class));
        verify(option, times(1)).setClientState(OptionState.DISABLE);
        assertTrue(decodedLines.size() == 0);
        reset(session);
        reset(option);

        // WILL_NOT, is not supported
        when(option.isClientSupported()).thenReturn(false);
        when(session.getOption(anyByte())).thenReturn(option);
        decodedLines = decoder.decode(buffer4, 3);
        Mockito.verify(session, times(0)).write(any(byte[].class));
        verify(option, times(0)).setClientState(any(OptionState.class));
        assertTrue(decodedLines.size() == 0);
        reset(session);
        reset(option);

    }

    @Test
    public void test_9() throws GeneralTelnetException, IOException {
        // few telnet commands in one decoded line
        IClientSession session = Mockito.mock(IClientSession.class);
        Decoder decoder = new Decoder(session);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.ABORT_OUTPUT, (byte)Constants.IAC, (byte)Constants.INTERRUPT_PROCESS};
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
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.ABORT_OUTPUT, (byte)Constants.IAC, (byte)Constants.INTERRUPT_PROCESS};
        buffer.put(cmd);
        buffer.put("test2\r\n".getBytes());

        ByteBuffer bufferResponse1 = ByteBuffer.allocate(100);
        bufferResponse1.put("test1".getBytes());
        ByteBuffer bufferResponse2 = ByteBuffer.allocate(100);
        bufferResponse2.put("test2".getBytes());
        when(session.getBuffer()).thenReturn(bufferResponse1).thenReturn(bufferResponse2);

        List<String> decodedLines = decoder.decode(buffer, 18);
        Mockito.verify(session).abortCurrentTask();
        Mockito.verify(session).interruptCurrentTask();
        assertTrue(decodedLines.size() == 2);
        assertTrue(decodedLines.get(0).compareTo("test1") == 0);
        assertTrue(decodedLines.get(1).compareTo("test2") == 0);
    }

    @Test
    public void test_11() throws IOException, GeneralTelnetException {
        //Check two IAC bytes in data transmit mode
        IClientSession session = Mockito.mock(IClientSession.class);
        Decoder decoder = new Decoder(session);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.IAC};
        buffer.put(cmd);
        List<String> decodedLines = decoder.decode(buffer, 2);
        Mockito.verify(session).addBuffer((byte)Constants.IAC);
        assertTrue(decodedLines.size() == 0);
    }

    @Test
    public void test_12() throws IOException, GeneralTelnetException, NoSuchFieldException, IllegalAccessException {
        //Check SB SE command
        IClientSession session = new IClientSession() {
            @Override
            public void addBuffer(byte _b) {

            }

            @Override
            public ByteBuffer getBuffer() {
                return null;
            }

            @Override
            public List<String> decode(ByteBuffer _buffer, int _bytesNum) throws GeneralTelnetException, IOException {
                return null;
            }

            @Override
            public void resetBuffer() {

            }

            @Override
            public void eraseCharacter() {

            }

            @Override
            public void addTask(Command _cmd) {

            }

            @Override
            public void abortCurrentTask() throws AbortOutputProcessException {

            }

            @Override
            public void interruptCurrentTask() throws InterruptProcessException {

            }

            @Override
            public void write(String _msg) throws IOException {

            }

            @Override
            public void write(byte[] _b) throws IOException {

            }

            @Override
            public void close() {

            }

            /**
             * Return option.
             *
             * @param _val - option type
             * @return - option state
             */
            @Override
            public Option getOption(byte _val) {
                return null;
            }

            @Override
            public void subNegotiation(byte _val, List<Byte> _b, Charset _charset) {
                assertTrue((_val & 0xFF) == 0x01);
                assertTrue(_b.size() == 3);
                assertTrue((_b.get(0) & 0xFF) == 0x02);
                assertTrue((_b.get(1) & 0xFF) == 0x03);
                assertTrue((_b.get(2) & 0xFF) == 0x04);
                test12 = true;
            }

            @Override
            public void prompt() throws IOException {

            }
        };
        Decoder decoder = new Decoder(session);
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] cmd = {(byte)Constants.IAC, (byte)Constants.SB, 0x01, 0x02, 0x3, 0x4, (byte)Constants.IAC, (byte)Constants.SE};
        buffer.put(cmd);
        List<String> decodedLines = decoder.decode(buffer, 8);
        assertTrue(decodedLines.size() == 0);
        assertTrue(test12);
    }

}