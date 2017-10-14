package com.manaldush.telnet.protocol;

import com.manaldush.telnet.IClientSession;
import com.manaldush.telnet.exceptions.DecodingException;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.protocol.processors.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of IDecoder interface.
 * Created by Maxim.Melnikov on 26.06.2017.
 */
final class Decoder implements IDecoder {
    private final IClientSession session;
    private final static int RESET_BYTE_VALUE = -1;
    private final static boolean RESET_IAC_FLAG = false;
    private final static boolean RESET_CR_FLAG = false;
    private final static boolean RESET_CMD_FLAG = false;
    private boolean IAC_FLAG;
    private boolean CMD_FLAG;
    private byte cmd;
    private byte option;
    private boolean CR_FLAG = RESET_CR_FLAG;
    private final Charset charset = Charset.forName("ASCII");
    private List<Byte> subNegotiation = null;
    private static final int MAX_NEGOTIATION_LEN = 1000;

    Decoder(final IClientSession _session) {
        session = _session;
        reset();
        resetCR();
    }
    /**
     * Decode method, return list of received lines. Line must enr with CRLF. Telnet protocol commands will be processed
     * and decoding continue. If received line without CRLF, it will be saved in session buffer.
     * @param _buffer - buffer for decoding
     * @param _bytesNum - number of bytes
     * @return list of decoded lines
     * @throws GeneralTelnetException - any telnet error
     * @throws IOException -  I/O errors
     */
    @Override
    public List<String> decode(final ByteBuffer _buffer, final int _bytesNum) throws GeneralTelnetException, IOException {
        List<String> result = new ArrayList<>();
        for(int counter=0;counter < _bytesNum;counter++) {
            byte b = _buffer.get(counter);
            boolean iac = checkIAC(b);
            if (CMD_FLAG) {
                boolean sb = checkSB(cmd);
                if (iac && IAC_FLAG && sb) {
                    processCommandByte(b);
                    resetIAC();
                } else if (sb && iac) {
                    IAC_FLAG = true;
                } else processCommandByte(b);
            } else {
                if (IAC_FLAG && iac) {
                    resetIAC();
                    session.addBuffer(b);
                } else if(!IAC_FLAG && iac) {
                    IAC_FLAG = true;
                } else if (IAC_FLAG && !iac) {
                    resetIAC();
                    CMD_FLAG = true;
                    processCommandByte(b);
                } else {
                    processDataByte(b, result);
                }
            }
        }
        return result;
    }

    private void decodeCommand() throws GeneralTelnetException, IOException {
        int bCmd = cmd & 0xFF;

        switch (bCmd) {
            case Constants.Abort_Output:
                AbortOutputProcessor.build(session).process();
                reset();
                break;
            case Constants.Interrupt_Process:
                InterruptionProcessor.build(session).process();
                reset();
                break;
            case Constants.Are_You_There:
                KeepAliveProcessor.build(session).process();
                reset();
                break;
            case Constants.Erase_Line:
                EraseLineProcessor.build(session).process();
                reset();
                break;
            case Constants.Erase_character:
                EraseCharacterProcessor.build(session).process();
                reset();
                break;
            case Constants.WILL_NOT:
                if (option == RESET_BYTE_VALUE) return;
                else {
                    NegotiationOptionsProcessor.buildWILLNOT(option, session).process();
                    reset();
                }
                break;
            case Constants.WILL:
                if (option == RESET_BYTE_VALUE) return;
                else {
                    NegotiationOptionsProcessor.buildWILL(option, session).process();
                    reset();
                }
                break;
            case Constants.DO_NOT:
                if (option == RESET_BYTE_VALUE) return;
                else {
                    NegotiationOptionsProcessor.buildDONOT(option, session).process();
                    reset();
                }
                break;
            case Constants.DO:
                if (option == RESET_BYTE_VALUE) return;
                else {
                    NegotiationOptionsProcessor.buildDO(option, session).process();
                    reset();
                }
                break;
            case Constants.SE:
                SubNegotiationEndCommand.build(session, subNegotiation, option, charset).process();
                reset();
                break;
            case Constants.SB:
                break;
            default:
                // unknown command
                reset();
                break;
        }
    }

    private void reset() {
        CMD_FLAG = RESET_CMD_FLAG;
        cmd = RESET_BYTE_VALUE;
        option = RESET_BYTE_VALUE;
        subNegotiation = null;
        resetIAC();
        resetCR();
    }

    private void resetIAC() {
        IAC_FLAG = RESET_IAC_FLAG;
    }

    private void resetCR() {
        CR_FLAG = RESET_CR_FLAG;
    }

    private void processCommandByte(byte b) throws GeneralTelnetException, IOException {
        if (cmd == RESET_BYTE_VALUE) {
            cmd = b;
            decodeCommand();
        }else if(((cmd & 0xFF)) == Constants.SB) {
            if (option == RESET_BYTE_VALUE) {
                option = b;
                return;
            }
            if (subNegotiation == null) subNegotiation = new LinkedList<>();
            if ( IAC_FLAG && ((b & 0xFF)) == Constants.SE) {
                cmd = b;
                decodeCommand();
            } else if (subNegotiation.size() == MAX_NEGOTIATION_LEN)
                throw new DecodingException(String.format("Sub negotiation process: bytes length is more then max negotiation length [%d]",
                        MAX_NEGOTIATION_LEN));
            else subNegotiation.add(b);
        } else {
            option = b;
            decodeCommand();
        }
    }

    private void processDataByte(byte b, List<String> result) {
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
                result.add(new String(bytesBuffer, charset));
            }
        } else if (CR_FLAG) {
            session.addBuffer((byte) Constants.CR);
        } else {
            session.addBuffer(b);
            resetCR();
        }

    }

    private boolean checkIAC(byte _b) {
        return (_b & 0xFF) == Constants.IAC;
    }

    private boolean checkSB(byte _b) {
        return (_b & 0xFF) == Constants.SB;
    }
}
