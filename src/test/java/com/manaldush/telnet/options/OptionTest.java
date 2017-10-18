package com.manaldush.telnet.options;

import com.manaldush.telnet.IClientSession;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.List;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class OptionTest {
    @Test
    public void test() {
        Option option = new Option((byte)0xFA, true, true) {
            @Override
            protected void innerSubNegotiation(List<Byte> _b, IClientSession _session, Charset _charset) {

            }
        };
        Assert.assertTrue(option.isClientSupported());
        Assert.assertTrue(option.isServerSupported());
        option.setServerState(OptionState.ENABLE);
        option.setClientState(OptionState.ENABLE);
        Assert.assertTrue(option.getServerState() == OptionState.ENABLE);
        Assert.assertTrue(option.getClientState() == OptionState.ENABLE);
        Option opt = spy(option);
        opt.setSubnegotiation(null, null, null);
        verify(opt, times(1)).innerSubNegotiation(null, null,null);
    }
}