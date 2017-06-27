package com.manaldush.telnet.protocol.processors;

import com.google.common.base.Preconditions;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.protocol.Constants;
import com.manaldush.telnet.protocol.ITelnetCommandProcessor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by Maxim.Melnikov on 26.06.2017.
 */
public final class NegotiationOptionsProcessor implements ITelnetCommandProcessor {
    private final int option;
    private final SocketChannel channel;
    private enum COMMAND_TYPE {WILL, WILL_NOT, DO, DO_NOT}
    private final COMMAND_TYPE cmd;

    private NegotiationOptionsProcessor(COMMAND_TYPE _cmd, int _option, SocketChannel _channel) {
        cmd = _cmd;
        option = _option;
        channel = _channel;
    }

    public static NegotiationOptionsProcessor build(final int _cmd, final int _option, SocketChannel _channel) {
        Preconditions.checkArgument(_cmd == Constants.WILL || _cmd == Constants.WILL_NOT ||
                _cmd == Constants.DO || _cmd == Constants.DO_NOT);
        COMMAND_TYPE cmdType = null;
        if (_cmd == Constants.WILL) cmdType = COMMAND_TYPE.WILL;
        else if (_cmd == Constants.WILL_NOT) cmdType = COMMAND_TYPE.WILL_NOT;
        else if (_cmd == Constants.DO) cmdType = COMMAND_TYPE.DO;
        else if (_cmd == Constants.DO_NOT) cmdType = COMMAND_TYPE.DO_NOT;
        Preconditions.checkNotNull(cmdType);
        return new NegotiationOptionsProcessor(cmdType, _option, _channel);
    }

    @Override
    public void process() throws IOException, GeneralTelnetException {
        if (cmd == COMMAND_TYPE.WILL || cmd == COMMAND_TYPE.WILL_NOT) {
            byte[] response = {(byte)Constants.IAC, (byte)Constants.DO_NOT, (byte)option};
            channel.write(ByteBuffer.allocate(3).put(response));
        } else if (cmd == COMMAND_TYPE.DO || cmd == COMMAND_TYPE.DO_NOT) {
            byte[] response = {(byte)Constants.IAC, (byte)Constants.WILL_NOT, (byte)option};
            channel.write(ByteBuffer.allocate(3).put(response));
        }
    }
}
