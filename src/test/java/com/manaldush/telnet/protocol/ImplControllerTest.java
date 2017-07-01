package com.manaldush.telnet.protocol;

import com.manaldush.telnet.*;
import com.manaldush.telnet.exceptions.ConfigurationException;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;


/**
 * Created by Maxim.Melnikov on 30.06.2017.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(AbstractSelectableChannel.class)
public class ImplControllerTest {

    public final class TestContext {
        private ConfigurationWrapper conf = null;
        private SocketChannel channel = null;
        private ImplController controller = null;
        private SelectionKey key = null;

        public ConfigurationWrapper getConf() {
            return conf;
        }

        public TestContext setConf(ConfigurationWrapper conf) {
            this.conf = conf;
            return this;
        }

        public SocketChannel getChannel() {
            return channel;
        }

        public TestContext setChannel(SocketChannel channel) {
            this.channel = channel;
            return this;
        }

        public ImplController getController() {
            return controller;
        }

        public TestContext setController(ImplController controller) {
            this.controller = controller;
            return this;
        }

        public SelectionKey getKey() {
            return key;
        }

        public TestContext setKey(SelectionKey key) {
            this.key = key;
            return this;
        }
    }

    @Test
    @PrepareForTest(ImplController.class)
    public void test_1() throws IOException, ConfigurationException, ParseException {
        ServerSocketChannel ss = PowerMockito.mock(ServerSocketChannel.class);
        PowerMockito.when(ss.setOption(any(SocketOption.class), any())).thenReturn(ss);
        PowerMockito.when(ss.configureBlocking(any(boolean.class))).thenReturn(ss);
        PowerMockito.when(ss.bind(any(SocketAddress.class))).thenReturn(ss);
        IServerSocketChannelFactory factory = new IServerSocketChannelFactory() {

            private ServerSocketChannel ss = null;

            public IServerSocketChannelFactory setSs(ServerSocketChannel ss) {
                this.ss = ss;
                return this;
            }

            @Override
            public ServerSocketChannel build() throws IOException {
                return ss;
            }
        }.setSs(ss);
        ConfigurationWrapper conf = ConfigurationWrapper.build(Configuration.build("localhost", 123), factory);
        ImplController controller = new ImplController();
        controller.configure(conf);
        controller.start();
        controller.stop();
        // check register operation
        CommandTemplate template = CommandTemplate.build("test", "description", new ICommandProcessorFactory() {
            @Override
            public ICommandProcessor build(Command _cmd, IWriterAdapter _writer) {
                return PowerMockito.mock(ICommandProcessor.class);
            }
        });
        controller.register(template);
        assertFalse(controller.search("test") == null);
        assertTrue(controller.search("test command") == null);

        controller.unregister(template);
        assertTrue(controller.search("test") == null);
        controller.register(template);
        assertFalse(controller.search("test") == null);
        controller.unregister("test");
        assertTrue(controller.search("test") == null);
    }

    @Test
    @PrepareForTest({ImplController.class, SelectionKey.class, Selector.class, SocketChannel.class})
    public void test_2() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, IOException {
        // check accept new connection
        TestContext context = prepareServerAccept();
        ImplController controller = context.getController();
        // Execute method
        Method processKeys = controller.getClass().getDeclaredMethod("processKeys", null);
        processKeys.setAccessible(true);
        processKeys.invoke(controller, null);

        // check session was created in container
        Field field = controller.getClass().getDeclaredField("sessions");
        field.setAccessible(true);
        Map<SocketChannel, ISession> sessions = (Map<SocketChannel, ISession>)field.get(controller);
        assertTrue(sessions.size() == 1);
        SocketChannel channel = sessions.entrySet().iterator().next().getKey();

        // check session was deleted
        controller.resetSession(channel);
        assertTrue(sessions.size() == 0);
    }


    @Test
    @PrepareForTest({ImplController.class, SelectionKey.class, Selector.class, SocketChannel.class})
    public void test_3() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, IOException {
        // check refuse connection because max sessions limit was over
        TestContext context = prepareServerAccept();
        ImplController controller = context.getController();
        // limit sessions
        Field field = controller.getClass().getDeclaredField("sessionsNumber");
        field.setAccessible(true);
        field.set(controller, 10);
        context.getConf().getConf().setMaxSessions(10);
        // Execute method
        Method processKeys = controller.getClass().getDeclaredMethod("processKeys", null);
        processKeys.setAccessible(true);
        processKeys.invoke(controller, null);

        // check session was created in container
        field = controller.getClass().getDeclaredField("sessions");
        field.setAccessible(true);
        Map<SocketChannel, ISession> sessions = (Map<SocketChannel, ISession>)field.get(controller);
        assertTrue(sessions.size() == 0);

        // check response unknown command
        Mockito.verify(context.getChannel()).write(any(ByteBuffer.class));
    }


    @Test
    @PrepareForTest({ImplController.class, SelectionKey.class, Selector.class, SocketChannel.class})
    public void test_4() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, IOException, GeneralTelnetException {
        TestContext context = prepareServerAccept();
        ImplController controller = context.getController();
        // Execute method
        Method processKeys = controller.getClass().getDeclaredMethod("processKeys", null);
        processKeys.setAccessible(true);
        processKeys.invoke(controller, null);

        // create read event
        Field field = controller.getClass().getDeclaredField("DATA_PORTION");
        field.setAccessible(true);
        int data_portion = field.getInt(controller);
        PowerMockito.when(context.getKey().attachment()).thenReturn(ImplController.SOCKET_TYPE.CLIENT);
        PowerMockito.when(context.getKey().channel()).thenReturn(context.getChannel());
        PowerMockito.when(context.getChannel().read(any(ByteBuffer.class))).thenReturn(data_portion).thenReturn(0);
        // mock session object for returning decoding string
        field = controller.getClass().getDeclaredField("sessions");
        field.setAccessible(true);
        Map<SocketChannel, ISession> sessions = (Map<SocketChannel, ISession>)field.get(controller);
        ISession session = PowerMockito.mock(ISession.class);
        sessions.put(context.getChannel(), session);
        PowerMockito.when(session.decode(any(ByteBuffer.class), anyInt())).thenReturn("test");
        processKeys.invoke(controller, null);

        // check client connection success read unknown command
        field = controller.getClass().getDeclaredField("BUFFER_UNKNOWN_COMMAND");
        field.setAccessible(true);
        Mockito.verify(context.getChannel()).write((ByteBuffer) field.get(context.getChannel()));

        // check client connection success read known command
        CommandTemplate template = CommandTemplate.build("test", "description", new ICommandProcessorFactory() {
            @Override
            public ICommandProcessor build(Command _cmd, IWriterAdapter _writer) {
                return PowerMockito.mock(ICommandProcessor.class);
            }
        });
        controller.register(template);
        PowerMockito.when(context.getChannel().read(any(ByteBuffer.class))).thenReturn(4);
        PowerMockito.when(session.decode(any(ByteBuffer.class), anyInt())).thenReturn("test");
        processKeys.invoke(controller, null);
        Mockito.verify(session).addTask(any(ICommandProcessor.class));

        // check client connection success read known long(length > data_portion) command
        template = CommandTemplate.build("test command", "description", new ICommandProcessorFactory() {
            @Override
            public ICommandProcessor build(Command _cmd, IWriterAdapter _writer) {
                return PowerMockito.mock(ICommandProcessor.class);
            }
        });
        controller.register(template);
        PowerMockito.when(context.getChannel().read(any(ByteBuffer.class))).thenReturn(10).thenReturn(2);
        PowerMockito.when(session.decode(any(ByteBuffer.class), anyInt())).thenReturn("test comma").thenReturn("nd");
        processKeys.invoke(controller, null);
        Mockito.verify(session).addTask(any(ICommandProcessor.class));

        // check client connection was closed
        PowerMockito.when(context.getChannel().read(any(ByteBuffer.class))).thenReturn(-1);
        processKeys.invoke(controller, null);
        Mockito.verify(session).close();
    }


    private TestContext prepareServerAccept() throws NoSuchFieldException, IllegalAccessException, IOException {
        ImplController controller = new ImplController();
        // reflect change server socket
        ServerSocketChannel ss = PowerMockito.mock(ServerSocketChannel.class);
        Field field = controller.getClass().getDeclaredField("ss");
        field.setAccessible(true);
        field.set(controller, ss);
        // reflect change state
        field = controller.getClass().getDeclaredField("status");
        field.setAccessible(true);
        field.set(controller, ImplController.STATUS.STARTED);
        // Reflect Selector
        Selector selector = PowerMockito.mock(Selector.class);
        field = controller.getClass().getDeclaredField("selector");
        field.setAccessible(true);
        field.set(controller, selector);
        // Reflect Configuration
        Configuration conf = Configuration.build("localhost", 22);
        ConfigurationWrapper confWrapper = ConfigurationWrapper.build(conf, null);
        field = controller.getClass().getDeclaredField("conf");
        field.setAccessible(true);
        field.set(controller, confWrapper);
        // SelectionKey check
        SelectionKey key = PowerMockito.mock(SelectionKey.class);
        PowerMockito.when(key.attachment()).thenReturn(ImplController.SOCKET_TYPE.SERVER);
        Set<SelectionKey> keys = new HashSet<>();
        keys.add(key);
        PowerMockito.when(selector.select(anyInt())).thenReturn(1);
        PowerMockito.when(selector.selectedKeys()).thenReturn(keys);
        // mock client SelectableChannel return
        PowerMockito.when(key.channel()).thenReturn(ss);
        SocketChannel client = PowerMockito.mock(SocketChannel.class);
        PowerMockito.when(ss.accept()).thenReturn(client);
        return new TestContext().setChannel(client).setConf(confWrapper).setController(controller).setKey(key);
    }
}