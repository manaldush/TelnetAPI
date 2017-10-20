package com.manaldush;

import com.manaldush.telnet.*;
import com.manaldush.telnet.exceptions.AbortOutputProcessException;
import com.manaldush.telnet.exceptions.ConfigurationException;
import com.manaldush.telnet.exceptions.InterruptProcessException;
import com.manaldush.telnet.exceptions.OperationException;
import com.manaldush.telnet.protocol.ImplController;
import com.manaldush.telnet.protocol.ConfigurationWrapper;
import com.manaldush.telnet.security.Role;
import com.manaldush.telnet.security.User;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws UnknownHostException, ConfigurationException, InterruptedException {
        // Set address and port
        Configuration conf = Configuration.build("wks-meyle", 1022);
        conf.setPrompt(">>>: ");
        // Set greet string
        conf.setGreeting("Demonstration of telnet command line!!!");
        ConfigurationWrapper configurationWrapper = ConfigurationWrapper.build(conf, null);
        IController controller = new ImplController();
        controller.configure(configurationWrapper);
        // Create user
        Role.build("admin");
        Set<String> userRoles = new HashSet<>();
        userRoles.add("admin");
        User.build("test","test", userRoles);

        // Register command with name 'test' and description 'description'
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
        // Register command2 with name 'test2' and description 'description', with role admin
        CommandTemplate command2 = CommandTemplate.build("test2", "description", new ICommandProcessorFactory() {
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
        });
        command2.addRole("admin");
        controller.register(command2);
        //Start controller
        controller.start();
        Thread.sleep(600000);
        controller.stop();
    }
}
