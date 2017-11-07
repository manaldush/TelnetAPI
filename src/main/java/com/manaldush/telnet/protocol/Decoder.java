package com.manaldush.telnet.protocol;

import com.manaldush.telnet.IClientSession;
import com.manaldush.telnet.exceptions.DecodingException;
import com.manaldush.telnet.exceptions.GeneralTelnetException;
import com.manaldush.telnet.protocol.processors.AbortOutputProcessor;
import com.manaldush.telnet.protocol.processors.EraseLineProcessor;
import com.manaldush.telnet.protocol.processors.InterruptionProcessor;
import com.manaldush.telnet.protocol.processors.KeepAliveProcessor;
import com.manaldush.telnet.protocol.processors.NegotiationOptionsProcessor;
import com.manaldush.telnet.protocol.processors.EraseCharacterProcessor;
import com.manaldush.telnet.protocol.processors.SubNegotiationEndCommand;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.manaldush.telnet.protocol.Constants.BYTE_FF;

/**
 * Implementation of IDecoder interface.
 * Created by Maxim.Melnikov on 26.06.2017.
 */
final class Decoder implements IDecoder {
    private final IClientSession session;
    private static final int RESET_BYTE_VALUE = -1;
    private static final boolean RESET_IAC_FLAG = false;
    private static final boolean RESET_CR_FLAG = false;
    private static final boolean RESET_CMD_FLAG = false;
    private boolean iacFlag;
    private boolean cmdFlag;
    private byte cmd;
    private byte option;
    private boolean crFlag = RESET_CR_FLAG;
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
    public List<String> decode(final ByteBuffer _buffer, final int _bytesNum)
            throws GeneralTelnetException, IOException {
        List<String> result = new ArrayList<>();
        for (int counter = 0; counter < _bytesNum; counter++) {
            byte b = _buffer.get(counter);
            boolean iac = checkIAC(b);
            if (cmdFlag) {
                boolean sb = checkSB(cmd);
                if (iac && iacFlag && sb) {
                    processCommandByte(b);
                    resetIAC();
                } else if (sb && iac) {
                    iacFlag = true;
                } else {
                    processCommandByte(b);
                }
            } else {
                if (iacFlag && iac) {
                    resetIAC();
                    session.addBuffer(b);
                } else if (!iacFlag && iac) {
                    iacFlag = true;
                } else if (iacFlag && !iac) {
                    resetIAC();
                    cmdFlag = true;
                    processCommandByte(b);
                } else {
                    processDataByte(b, result);
                }
            }
        }
        return result;
    }

    private void decodeCommand() throws GeneralTelnetException, IOException {
        int bCmd = cmd & BYTE_FF;

        switch (bCmd) {
            case Constants.ABORT_OUTPUT:
                AbortOutputProcessor.build(session).process();
                reset();
                break;
            case Constants.INTERRUPT_PROCESS:
                InterruptionProcessor.build(session).process();
                reset();
                break;
            case Constants.ARE_YOU_THERE:
                KeepAliveProcessor.build(session).process();
                reset();
                break;
            case Constants.ERASE_LINE:
                EraseLineProcessor.build(session).process();
                reset();
                break;
            case Constants.ERASE_CHARACTER:
                EraseCharacterProcessor.build(session).process();
                reset();
                break;
            case Constants.WILL_NOT:
                if (option == RESET_BYTE_VALUE) {
                    return;
                } else {
                    NegotiationOptionsProcessor.buildWILLNOT(option, session).process();
                    reset();
                }
                break;
            case Constants.WILL:
                if (option == RESET_BYTE_VALUE) {
                    return;
                } else {
                    NegotiationOptionsProcessor.buildWILL(option, session).process();
                    reset();
                }
                break;
            case Constants.DO_NOT:
                if (option == RESET_BYTE_VALUE) {
                    return;
                } else {
                    NegotiationOptionsProcessor.buildDONOT(option, session).process();
                    reset();
                }
                break;
            case Constants.DO:
                if (option == RESET_BYTE_VALUE) {
                    return;
                } else {
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
        cmdFlag = RESET_CMD_FLAG;
        cmd = RESET_BYTE_VALUE;
        option = RESET_BYTE_VALUE;
        subNegotiation = null;
        resetIAC();
        resetCR();
    }

    private void resetIAC() {
        iacFlag = RESET_IAC_FLAG;
    }

    private void resetCR() {
        crFlag = RESET_CR_FLAG;
    }

    private void processCommandByte(final byte _b) throws GeneralTelnetException, IOException {
        if (cmd == RESET_BYTE_VALUE) {
            cmd = _b;
            decodeCommand();
        } else if (((cmd & BYTE_FF)) == Constants.SB) {
            if (option == RESET_BYTE_VALUE) {
                option = _b;
                return;
            }
            if (subNegotiation == null) {
                subNegotiation = new LinkedList<>();
            }
            if (iacFlag && ((_b & BYTE_FF)) == Constants.SE) {
                cmd = _b;
                decodeCommand();
            } else if (subNegotiation.size() == MAX_NEGOTIATION_LEN) {
                throw new DecodingException(String.format(
                        "Sub negotiation process: bytes length is more then max negotiation length [%d]",
                        MAX_NEGOTIATION_LEN));
            } else {
                subNegotiation.add(_b);
            }
        } else {
            option = _b;
            decodeCommand();
        }
    }

    private void processDataByte(final byte _b, final List<String> _result) throws IOException {
        if ((_b & BYTE_FF) == Constants.CR) {
            crFlag = true;
        } else if ((_b & BYTE_FF) == Constants.LF && crFlag) {
            ByteBuffer buffer = session.getBuffer();
            session.resetBuffer();
            resetCR();
            if (buffer != null) {
                buffer.flip();
                byte[] bytesBuffer = new byte[buffer.limit()];
                buffer.get(bytesBuffer, 0, buffer.limit());
                _result.add(new String(bytesBuffer, charset));
            } else {
                _result.add("");
            }
        } else if (crFlag) {
            session.addBuffer((byte) Constants.CR);
        } else {
            session.addBuffer(_b);
            resetCR();
        }

    }

    private boolean checkIAC(final byte _b) {
        return (_b & BYTE_FF) == Constants.IAC;
    }

    private boolean checkSB(final byte _b) {
        return (_b & BYTE_FF) == Constants.SB;
    }
}
