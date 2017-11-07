package com.manaldush.telnet;

import org.junit.Test;

import java.net.UnknownHostException;
import java.text.ParseException;

import static org.junit.Assert.assertTrue;

/**
 * Created by Maxim.Melnikov on 27.06.2017.
 */
public class ConfigurationTest {
    @Test
    public void test_1() throws UnknownHostException {
        Configuration conf = Configuration.build("localhost", 1000);
        conf.setSoSndBuf(2048);
        conf.setRCVBUF(2048);
        conf.setMaxSessions(10);
        conf.setREUSEADDR(Boolean.TRUE);
        conf.setTCPNODELAY(Boolean.TRUE);
        conf.setParser(new ICommandParserFactory() {
            @Override
            public ICommandParser build(String _cmd) throws ParseException {
                return null;
            }
        });
        Configuration conf2 = (Configuration) conf.clone();
        assertTrue(conf2.getSoReuseAaddr());
        assertTrue(conf2.getTcpNoDelay());
        assertTrue(conf2.getPort() == 1000);
        assertTrue(conf2.getMaxSessions() == 10);
        assertTrue(conf2.getSoRcvBuf() == 2048);
        assertTrue(conf2.getSoSndBuf() == 2048);
        assertTrue(conf2.getAddress().getHostName().compareTo("localhost") == 0);
        assertTrue(conf2.getParser() != null);
    }
}