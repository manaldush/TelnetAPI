package com.manaldush.telnet.protocol;

import com.google.common.base.Preconditions;
import com.manaldush.telnet.Configuration;

/**
 * Created by Maxim.Melnikov on 29.06.2017.
 */
public final class ConfigurationWrapper implements Cloneable {
    private final Configuration conf;
    private final IServerSocketChannelFactory ssChannelFactory;
    private ConfigurationWrapper(Configuration _conf, IServerSocketChannelFactory _ssChannelFactory) {
        conf = _conf;
        ssChannelFactory = _ssChannelFactory;
    }

    public static ConfigurationWrapper build(Configuration _conf, IServerSocketChannelFactory _ssChannelFactory) {
        Preconditions.checkNotNull(_conf);
        if (_ssChannelFactory == null) return new ConfigurationWrapper(_conf, new ImplServerSocketChannel());
        else return new ConfigurationWrapper(_conf, _ssChannelFactory);
    }

    public Configuration getConf() {
        return conf;
    }

    public IServerSocketChannelFactory getSsChannelFactory() {
        return ssChannelFactory;
    }

    @Override
    public Object clone() {
        return ConfigurationWrapper.build((Configuration) this.getConf().clone(), this.getSsChannelFactory());
    }
}
