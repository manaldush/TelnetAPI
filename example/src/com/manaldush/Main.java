package com.manaldush;

import com.manaldush.telnet.*;
import com.manaldush.telnet.exceptions.AbortOutputProcessException;
import com.manaldush.telnet.exceptions.ConfigurationException;
import com.manaldush.telnet.exceptions.InterruptProcessException;
import com.manaldush.telnet.exceptions.OperationException;
import com.manaldush.telnet.protocol.ImplController;
import com.manaldush.telnet.protocol.ConfigurationWrapper;

import java.io.IOException;
import java.net.UnknownHostException;

public class Main {

    public static void main(String[] args) throws UnknownHostException, ConfigurationException, InterruptedException {
        Configuration conf = Configuration.build("wks-meyle", 1022);
        //conf.setGreeting("Welcome!!!");
        ConfigurationWrapper configurationWrapper = ConfigurationWrapper.build(conf, null);
        IController controller = new ImplController();
        controller.register(CommandTemplate.build("test", "description", new ICommandProcessorFactory() {
            @Override
            public ICommandProcessor build(Command command, final IClientSession _session) {
                return new ICommandProcessor() {
                    @Override
                    public void process() throws OperationException, IOException {
                        _session.write("test response");
                    }

                    @Override
                    public void abortOutput() throws AbortOutputProcessException {

                    }

                    @Override
                    public void interruptProcess() throws InterruptProcessException {

                    }
                };
            }
        }));
        controller.configure(configurationWrapper);
        controller.start();
        Thread.sleep(600000);
        controller.stop();
    }
}
