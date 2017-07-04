package com.manaldush.telnet.protocol;

import com.manaldush.telnet.Command;
import com.manaldush.telnet.ICommandProcessor;
import com.manaldush.telnet.IClientSession;
import com.manaldush.telnet.exceptions.AbortOutputProcessException;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.exceptions.InterruptProcessException;
import com.manaldush.telnet.exceptions.OperationException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maxim.Melnikov on 22.06.2017.
 */
final class ImplTelnetClientSession implements IClientSession {
    private static final Charset DEFAULT_CHARSET = Charset.forName("ASCII");
    private ByteBuffer buffer = null;
    private final List<ICommandProcessor> tasks = new ArrayList<>();
    private final SocketChannel channel;
    private final ImplController controller;
    private final int initBufferSize;
    private final SelectionKey key;
    private ICommandProcessor currentTask = null;
    private Thread currentThread;
    private TaskExecutor executor;
    private boolean stop = false;
    private IDecoder decoder;

    ImplTelnetClientSession(final SocketChannel _channel, final ImplController _controller, final int _initBufferSize, SelectionKey _key) {
        channel = _channel;
        controller = _controller;
        initBufferSize = _initBufferSize;
        decoder = new Decoder(this);
        key = _key;
    }

    @Override
    public void addBuffer(byte _b) {
        if (buffer == null) buffer = ByteBuffer.allocate(initBufferSize);
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

    @Override
    public ByteBuffer getBuffer() {
        //return copy
        if (buffer == null) return null;
        ByteBuffer nbuffer = ByteBuffer.allocate(buffer.capacity());
        nbuffer.put(buffer.array());
        nbuffer.position(buffer.position());
        return nbuffer;
    }

    @Override
    public List<String> decode(final ByteBuffer _buffer, final int _bytesNum) throws GeneralTelnetException, IOException {
        synchronized (this) {
            if (stop) return null;
        }
        return decoder.decode(_buffer, _bytesNum);
    }

    @Override
    public void resetBuffer() {
        buffer = null;
    }

    @Override
    public void eraseCharacter() {
        if (buffer == null) return;
        if (buffer.position() == 0) return;
        buffer.put(buffer.position(),(byte)0);
        buffer.position(buffer.position()-1);
    }

    @Override
    public void addTask(Command _cmd) {
        ICommandProcessor task =
                _cmd.getTemplate().getCommandProcessorFactory().build(_cmd, this);
        boolean thrStart = false;
        synchronized (this) {
            if (stop) return;
            tasks.add(task);
            if (currentThread == null) {
                currentThread = new Thread(new TaskExecutor());
                thrStart = true;
            }
        }
        if (thrStart) currentThread.start();
    }

    @Override
    public synchronized void abortCurrentTask() throws AbortOutputProcessException {
        if (stop) return;
        if (currentTask == null) return;
        currentTask.abortOutput();
    }

    @Override
    public synchronized void interruptCurrentTask() throws InterruptProcessException {
        if (stop) return;
        if (currentTask == null) return;
        currentTask.interruptProcess();
    }

    @Override
    public void write(String _msg) throws IOException {
        byte[] b = _msg.getBytes(DEFAULT_CHARSET);
        ByteBuffer buffer = ByteBuffer.allocate(b.length + 2);
        buffer.put(b);
        buffer.put((byte)Constants.CR);
        buffer.put((byte)Constants.LF);
        buffer.flip();
        innerWrite(buffer);
    }

    @Override
    public void write(byte[] _b) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(_b.length);
        buffer.put(_b);
        innerWrite(buffer);
    }

    private void innerWrite(ByteBuffer buffer) throws IOException {
        try {
            channel.write(buffer);
        } catch (IOException ex) {
            close();
            throw ex;
        }
    }

    @Override
    public synchronized void close() {
        if (stop) return;
        key.cancel();
        if (executor == null) {
            this.resetSession();
            return;
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

    private class TaskExecutor implements Runnable {

        @Override
        public void run() {
            for(;;) {
                synchronized (ImplTelnetClientSession.this) {
                    currentTask = null;
                    if (ImplTelnetClientSession.this.stop){
                        executor = null;
                        ImplTelnetClientSession.this.resetSession();
                        currentThread = null;
                        return;
                    } else if (tasks.size() == 0) {
                        executor = null;
                        currentThread = null;
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
}
