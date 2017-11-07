package com.manaldush.telnet.protocol;

import com.google.common.base.Preconditions;
import com.manaldush.telnet.Configuration;

/**
 * Created by Maxim.Melnikov on 29.06.2017.
 */
public final class ConfigurationWrapper implements Cloneable {
    private final Configuration conf;
    private final IServerSocketChannelFactory ssChannelFactory;
    private ConfigurationWrapper(final Configuration _conf, final IServerSocketChannelFactory _ssChannelFactory) {
        conf = _conf;
        ssChannelFactory = _ssChannelFactory;
    }

    /**
     * Config file wrapper build method.
     * @param _conf - configuration
     * @param _ssChannelFactory - socket factory
     * @throws NullPointerException - configuration is null
     * @return wrapper
     */
    public static ConfigurationWrapper build(final Configuration _conf,
                                             final IServerSocketChannelFactory _ssChannelFactory) {
        Preconditions.checkNotNull(_conf);
        if (_ssChannelFactory == null) {
            return new ConfigurationWrapper(_conf, new ImplServerSocketChannel());
        } else {
            return new ConfigurationWrapper(_conf, _ssChannelFactory);
        }
    }

    /**
     * Get configuration.
     * @return configuration object
     */
    public Configuration getConf() {
        return conf;
    }

    /**
     * Get server socket factory object.
     * @return factory
     */
    public IServerSocketChannelFactory getSsChannelFactory() {
        return ssChannelFactory;
    }

    @Override
    public Object clone() {
        return ConfigurationWrapper.build((Configuration) this.getConf().clone(), this.getSsChannelFactory());
    }
}
