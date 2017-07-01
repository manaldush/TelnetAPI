package com.manaldush.telnet.protocol;

import com.manaldush.telnet.ICommandProcessor;
import com.manaldush.telnet.exceptions.AbortOutputProcessException;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.exceptions.InterruptProcessException;
import com.manaldush.telnet.exceptions.OperationException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maxim.Melnikov on 22.06.2017.
 */
final class ImplTelnetSession implements ISession {
    private ByteBuffer buffer = null;
    private final List<ICommandProcessor> tasks = new ArrayList<>();
    private final SocketChannel channel;
    private final ImplController controller;
    private final int initBufferSize;
    private ICommandProcessor currentTask = null;
    private Thread currentThread;
    private TaskExecutor executor;
    private boolean stop = false;
    private IDecoder decoder;

    ImplTelnetSession(final SocketChannel _channel, final ImplController _controller, final int _initBufferSize) {
        channel = _channel;
        controller = _controller;
        initBufferSize = _initBufferSize;
        decoder = new Decoder(this, _channel);
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
    public String decode(final ByteBuffer _buffer, final int _bytesNum) throws GeneralTelnetException, IOException {
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
    public void addTask(ICommandProcessor _task) {
        boolean thrStart = false;
        synchronized (this) {
            if (stop) return;
            tasks.add(_task);
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
    public synchronized void close() {
        if (stop) return;
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
                synchronized (ImplTelnetSession.this) {
                    currentTask = null;
                    if (ImplTelnetSession.this.stop){
                        executor = null;
                        ImplTelnetSession.this.resetSession();
                        currentThread = null;
                        return;
                    } else if (tasks.size() == 0) {
                        executor = null;
                        currentThread = null;
                        return;
                    }
                    currentTask = tasks.get(0);
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
