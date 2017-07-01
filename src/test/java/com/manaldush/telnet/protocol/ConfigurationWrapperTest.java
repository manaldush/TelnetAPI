package com.manaldush.telnet.protocol;

import com.manaldush.telnet.Configuration;
import org.junit.Test;

import java.net.UnknownHostException;

import static org.junit.Assert.assertTrue;

/**
 * Created by Maxim.Melnikov on 29.06.2017.
 */
public class ConfigurationWrapperTest {
    @Test
    public void test_1() throws UnknownHostException {
        Configuration conf = Configuration.build("localhost", 22);
        IServerSocketChannelFactory factory = new ImplServerSocketChannel();
        ConfigurationWrapper wrapper = ConfigurationWrapper.build(conf, factory);
        assertTrue(wrapper.getConf() != null);
        assertTrue(wrapper.getSsChannelFactory() != null);
    }
}