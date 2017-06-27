package com.manaldush.telnet.protocol;

import com.manaldush.telnet.exceptions.GeneralTelnetException;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * Created by Maxim.Melnikov on 27.06.2017.
 */
public class DecoderTest {
    @Test
    public void test_1() throws GeneralTelnetException, IOException {
        Decoder decoder = new Decoder(new MockSession(), new MockSocketChannel());
        ByteBuffer buffer = ByteBuffer.allocate(100);
        String cmd = "test\r\n";
        buffer.put(cmd.getBytes());
        String res = decoder.decode(buffer, 6);
        assertTrue("test".compareTo(res) == 0);
    }

    @Test
    public void test_2() throws GeneralTelnetException, IOException {
        Decoder decoder = new Decoder(new MockSession(), new MockSocketChannel());
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put("test".getBytes());
        assertTrue(decoder.decode(buffer, 4) == null);
        buffer = ByteBuffer.allocate(100);
        buffer.put(" command\r\n".getBytes());
        assertTrue(decoder.decode(buffer, 10).compareTo("test command") == 0);
        buffer = ByteBuffer.allocate(100);
        buffer.put("test\r\n".getBytes());
        String res = decoder.decode(buffer, 6);
        assertTrue("test".compareTo(res) == 0);
    }
}