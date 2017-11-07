package com.manaldush.telnet.protocol;

import com.manaldush.telnet.Command;
import com.manaldush.telnet.ICommandProcessor;
import com.manaldush.telnet.IClientSession;
import com.manaldush.telnet.exceptions.AbortOutputProcessException;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.exceptions.InterruptProcessException;
import com.manaldush.telnet.exceptions.OperationException;
import com.manaldush.telnet.options.DefaultOption;
import com.manaldush.telnet.options.NotSupportedOption;
import com.manaldush.telnet.options.Option;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.manaldush.telnet.protocol.Constants.BYTE_FF;
import static com.manaldush.telnet.protocol.Constants.CRLF;

/**
 * Implementation of IClientSession interface.
 *
 * Created by Maxim.Melnikov on 22.06.2017.
 */
final class ImplTelnetClientSession implements IClientSession {
    /**Default charset of transport data.*/
    private static final Charset DEFAULT_CHARSET = Charset.forName("ASCII");
    /**Buffer of read data.*/
    private ByteBuffer buffer = null;
    /**List of tasks that processed incoming commands.*/
    private final List<ICommandProcessor> tasks = new ArrayList<>();
    /**Socket object.*/
    private final SocketChannel channel;
    /**Telnet controller.*/
    private final ImplController controller;
    /**Init buffer size. When size of read data is more than initBufferSize, re-allocate buffer
     * size = buffer size + initBufferSize.*/
    private final int initBufferSize;
    /**Used for catch event of incoming data in socket.*/
    private final SelectionKey key;
    /**Current processing command.*/
    private ICommandProcessor currentTask = null;
    /**Current thread, processing commands.*/
    private Thread currentThread;
    /**Execute tasks in thread currentThread.*/
    private TaskExecutor executor;
    /**Should stop.*/
    private boolean stop = false;
    /**Decoder of incoming data.*/
    private IDecoder decoder;
    /**Prompt chars.*/
    private final String prompt;
    /**Available telnet protocol options.*/
    private final Map<Integer, Option> options;

    /**
     * Construct implementation of telnet client session.
     *
     * @param _channel - socket channel
     * @param _controller - controller
     * @param _initBufferSize - init buffer size
     * @param _key - selection key
     * @param _prompt - prompt chars
     */
    ImplTelnetClientSession(final SocketChannel _channel, final ImplController _controller, final int _initBufferSize,
                            final SelectionKey _key, final String _prompt) {
        channel = _channel;
        controller = _controller;
        initBufferSize = _initBufferSize;
        decoder = new Decoder(this);
        key = _key;
        prompt = _prompt;
        options = new HashMap<>();
        initOptions();
    }

    /**
     * Add buffer to current read buffer of session.
     *
     * @param _b - byte
     */
    @Override
    public void addBuffer(final byte _b) {
        if (buffer == null) {
            buffer = ByteBuffer.allocate(initBufferSize);
        }
        if (buffer.remaining() > 0) {
            buffer.put(_b);
        } else {
            buffer.flip();
            ByteBuffer nbuffer = ByteBuffer.allocate(buffer.capacity() + initBufferSize);
            nbuffer.put(buffer);
            nbuffer.put(_b);
            buffer = nbuffer;
        }
    }

    /**
     * Get buffer object.
     * @return buffer
     */
    @Override
    public ByteBuffer getBuffer() {
        //return copy
        if (buffer == null) {
            return null;
        }
        ByteBuffer nbuffer = ByteBuffer.allocate(buffer.capacity());
        nbuffer.put(buffer.array());
        nbuffer.position(buffer.position());
        return nbuffer;
    }

    /**
     * Decode byte buffer, that was read from connection.
     * @param _buffer - byte buffer
     * @param _bytesNum - number of readed bytes
     * @return list of read strings
     * @throws GeneralTelnetException - any telnet protocol error during processing
     * @throws IOException - IO errors
     */
    @Override
    public List<String> decode(final ByteBuffer _buffer, final int _bytesNum)
            throws GeneralTelnetException, IOException {
        synchronized (this) {
            if (stop) {
                return null;
            }
        }
        return decoder.decode(_buffer, _bytesNum);
    }

    /**
     * Reset buffer.
     */
    @Override
    public void resetBuffer() {
        buffer = null;
    }

    /**
     * Erase last character from buffer.
     */
    @Override
    public void eraseCharacter() {
        if (buffer == null) {
            return;
        }
        if (buffer.position() == 0) {
            return;
        }
        buffer.put(buffer.position(), (byte) 0);
        buffer.position(buffer.position() - 1);
    }

    /**
     * Create task from command and add it in queue for processing.
     * @param _cmd - command
     */
    @Override
    public void addTask(final Command _cmd) {
        ICommandProcessor task =
                _cmd.getTemplate().getCommandProcessorFactory().build(_cmd, this);
        boolean thrStart = false;
        synchronized (this) {
            if (stop) {
                return;
            }
            tasks.add(task);
            if (currentThread == null) {
                currentThread = new Thread(new TaskExecutor());
                thrStart = true;
            }
        }
        if (thrStart) {
            currentThread.start();
        }
    }

    /**
     * Abort output of current executed task.
     * @throws AbortOutputProcessException - if some error occurred during processing output abort
     * in current executed task
     */
    @Override
    public synchronized void abortCurrentTask() throws AbortOutputProcessException {
        if (stop) {
            return;
        }
        if (currentTask == null) {
            return;
        }
        currentTask.abortOutput();
    }

    /**
     * Interrupt current executed task.
     * @throws InterruptProcessException - if some error occured during processing interruption current executed task
     */
    @Override
    public synchronized void interruptCurrentTask() throws InterruptProcessException {
        if (stop) {
            return;
        }
        if (currentTask == null) {
            return;
        }
        currentTask.interruptProcess();
    }

    /**
     * Write message in connection.
     * @param _msg - message
     * @throws IOException - if IO problem occured
     */
    @Override
    public void write(final String _msg) throws IOException {
        byte[] b = str2Bytes(_msg);
        ByteBuffer buf = ByteBuffer.allocate(b.length);
        buf.put(b);
        buf.flip();
        innerWrite(buf);
    }

    /**
     * Write bytes in connection.
     * @param _b - bytes
     * @throws IOException - if IO problem occured
     */
    @Override
    public void write(final byte[] _b) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(_b.length);
        buf.put(_b);
        buf.flip();
        innerWrite(buf);
    }

    private void innerWrite(final ByteBuffer _buffer) throws IOException {
        try {
            channel.write(_buffer);
        } catch (IOException ex) {
            close();
            throw ex;
        }
    }

    private byte[] str2Bytes(final String _msg) {
        return _msg.getBytes(DEFAULT_CHARSET);
    }

    /**
     * Command for close session.
     */
    @Override
    public synchronized void close() {
        innerClose();
    }

    /**
     * Return state of option on client.
     *
     * @param _val - option type
     * @return - option state
     */
    @Override
    public Option getOption(final byte _val) {
        return options.get(_val & BYTE_FF);
    }

    /**
     * Sub negotiation process.
     *
     * @param _val     - value of option
     * @param _b       - sub negotiation bytes between option value and SE command
     * @param _charset
     */
    @Override
    public void subNegotiation(final byte _val, final List<Byte> _b, final Charset _charset) {
        options.get(_val & BYTE_FF).setSubnegotiation(_b, this, _charset);
    }

    @Override
    public void prompt() throws IOException {
        write(Constants.GREEN);
        write(str2Bytes(prompt));
        write(Constants.RESET_COLOR);
    }

    private void innerClose() {
        if (stop) {
            return;
        }
        key.cancel();
        if (executor == null) {
            this.resetSession();
        }
        this.stop = true;
    }

    private void resetSession() {
        try {
            controller.resetSession(channel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Thread used for execution telnet tasks.
     */
    private class TaskExecutor implements Runnable {

        @Override
        public void run() {
            for (;;) {
                synchronized (ImplTelnetClientSession.this) {
                    currentTask = null;
                    if (ImplTelnetClientSession.this.stop) {
                        executor = null;
                        ImplTelnetClientSession.this.resetSession();
                        currentThread = null;
                        return;
                    } else if (tasks.size() == 0) {
                        executor = null;
                        currentThread = null;
                        try {
                            ImplTelnetClientSession.this.write(CRLF);
                            prompt();
                        } catch (IOException e) {
                            e.printStackTrace();
                            innerClose();
                        }
                        return;
                    }
                    currentTask = tasks.remove(0);
                }
                try {
                    currentTask.process();
                } catch (OperationException ex) {
                    ex.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    close();
                }
            }
        }
    }

    private void initOptions() {
        for (byte b = Byte.MIN_VALUE; b < Byte.MAX_VALUE; b++) {
            int i = b & BYTE_FF;
            switch (i) {
                case Constants.OPT_SUPPRESS_GO_AHEAD:
                    options.put(i, new DefaultOption(b, false, true));
                    break;
                case Constants.OPT_ECHO:
                    options.put(i, new DefaultOption(b, false, true));
                    break;
                default:
                    options.put(i, new NotSupportedOption(b));
            }
        }
    }
}
