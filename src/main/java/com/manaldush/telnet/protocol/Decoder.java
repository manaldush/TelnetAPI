package com.manaldush.telnet.protocol;

import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.protocol.processors.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * Created by Maxim.Melnikov on 26.06.2017.
 */
final class Decoder implements IDecoder {
    private final ISession session;
    private final SocketChannel channel;
    private final static int RESET_BYTE_VALUE = -1;
    private final static boolean RESET_IAC_FLAG = false;
    private final static boolean RESET_CR_FLAG = false;
    private boolean IAC_FLAG;
    private byte cmd;
    private byte option;
    private boolean CR_FLAG = RESET_CR_FLAG;
    private final Charset charset = Charset.forName("ASCII");

    Decoder(final ISession _session, final SocketChannel _channel) {
        session = _session;
        channel = _channel;
        reset();
        resetCR();
    }

    @Override
    public String decode(final ByteBuffer _buffer, final int _bytesNum) throws GeneralTelnetException, IOException {
        for(int counter=0;counter < _bytesNum;counter++) {
            byte b = _buffer.get(counter);
            if (IAC_FLAG && (b & 0xFF) == Constants.IAC) {
                reset();
                resetCR();
                session.addBuffer(b);
            } else if(!IAC_FLAG && (b & 0xFF) == Constants.IAC) {
                IAC_FLAG = true;
            } else if (IAC_FLAG && (b & 0xFF) != Constants.IAC) {
                if (cmd == RESET_BYTE_VALUE) {
                    cmd = b;
                    decodeCommand();
                } else {
                    option = b;
                    decodeCommand();
                }
            } else {
                reset();
                if ((b & 0xFF) == Constants.CR) {
                    CR_FLAG = true;
                } else if ((b & 0xFF) == Constants.LF && CR_FLAG) {
                    ByteBuffer buffer = session.getBuffer();
                    session.resetBuffer();
                    resetCR();
                    if (buffer != null) {
                        buffer.flip();
                        byte[] bytesBuffer = new byte[buffer.limit()];
                        buffer.get(bytesBuffer, 0, buffer.limit());
                        return new String(bytesBuffer, charset);
                    } else {
                        return null;
                    }
                } else if (CR_FLAG) {
                    session.addBuffer((byte) Constants.CR);
                } else {
                    session.addBuffer(b);
                    resetCR();
                }
            }
        }
        return null;
    }

    private void decodeCommand() throws GeneralTelnetException, IOException {
        int bCmd = cmd & 0xFF;
        if (bCmd == Constants.Abort_Output){
            AbortOutputProcessor.build(session).process();
            reset();
        } else if (bCmd == Constants.Interrupt_Process) {
            InterruptionProcessor.build(session).process();
            reset();
        } else if (bCmd == Constants.Are_You_There) {
            KeepAliveProcessor.build(channel).process();
            reset();
        } else if (bCmd == Constants.Erase_Line) {
            EraseLineProcessor.build(session).process();
            reset();
        } else if (bCmd == Constants.Erase_character) {
            EraseCharacterProcessor.build(session).process();
            reset();
        } else if(bCmd == Constants.WILL_NOT) {
            if (option == RESET_BYTE_VALUE) return;
            else {
                NegotiationOptionsProcessor.buildWILLNOT(option, channel).process();
                reset();
            }
        } else if (bCmd == Constants.WILL) {
            if (option == RESET_BYTE_VALUE) return;
            else {
                NegotiationOptionsProcessor.buildWILL(option, channel).process();
                reset();
            }
        } else if (bCmd == Constants.DO_NOT) {
            if (option == RESET_BYTE_VALUE) return;
            else {
                NegotiationOptionsProcessor.buildDONOT(option, channel).process();
                reset();
            }
        } else if (bCmd == Constants.DO) {
            if (option == RESET_BYTE_VALUE) return;
            else {
                NegotiationOptionsProcessor.buildDO(option, channel).process();
                reset();
            }
        }
    }

    private void reset() {
        IAC_FLAG = RESET_IAC_FLAG;
        cmd = RESET_BYTE_VALUE;
        option = RESET_BYTE_VALUE;
    }

    private void resetCR() {
        CR_FLAG = RESET_CR_FLAG;
    }
}
