package com.manaldush.telnet.protocol;

/**
 * Constants file.
 * Created by Maxim.Melnikov on 14.06.2017.
 */
public final class Constants {
    private Constants() { }
    //THE NVT PRINTER AND KEYBOARD
    /**Moves the printer to the left margin of the current line.*/
    public static final int CR   = 0x0D;
    /**Moves the printer to the next print line, keeping the same horizontal position.*/
    public static final int LF   = 0x0A;
    /**HTTP CRLF.*/
    public static final byte[] CRLF = {CR, LF};
    /**ANSI Red color.*/
    public static final String	RED	= "\u001B[31m";
    /**ANSI Green color.*/
    public static final String	GREEN	= "\u001B[32m";
    /**Reset color.*/
    public static final String	RESET_COLOR	= "\u001B[0m";
    /**No Operation.*/
    public static final int NUL  = 0x00;
    /**Produces an audible or visible signal (which does NOT move the print head).*/
    public static final int BEL  = 0x07;
    /**Moves the print head one character position towards the left margin.*/
    public static final int BS  = 0x08;
    /**Moves the printer to the next horizontal tab stop.
    It remains unspecified how either party determines or establishes where such tab stops are located.*/
    public static final int HT  = 0x09;
    /**Moves the printer to the next vertical tab stop.
    It remains unspecified how either party determines or establishes where such tab stops are located.*/
    public static final int VT  = 0x0B;
    /**Moves the printer to the top of the next page, keeping the same horizontal position.*/
    public static final int FF  = 0x0C;
    //TELNET COMMAND
    /**IAC character.*/
    public static final int IAC               = 0xFF;
    /**End of subnegotiation parameters.*/
    public static final int SE                = 0xF0;
    /**No operation.*/
    public static final int NOP               = 0xF1;
    /**The data stream portion of a Synch.This should always be accompanied by a TCP Urgent notification.*/
    public static final int DATA_MARK         = 0xF2;
    /**NVT character BRK.*/
    public static final int BREAK             = 0xF3;
    /**The function IP.*/
    public static final int INTERRUPT_PROCESS = 0xF4;
    /**The function AO.*/
    public static final int ABORT_OUTPUT      = 0xF5;
    /**The function AYT.*/
    public static final int ARE_YOU_THERE     = 0xF6;
    /**The function EC.*/
    public static final int ERASE_CHARACTER   = 0xF7;
    /**The function EL.*/
    public static final int ERASE_LINE        = 0xF8;
    /**The GA signal.*/
    public static final int GO_AHEAD          = 0xF9;
    /**Indicates that what follows is subnegotiation of the indicated option.*/
    public static final int SB                = 0xFA;
    /**Indicates the desire to begin performing, or confirmation that you are now performing, the indicated option.*/
    public static final int WILL              = 0xFB;
    /**Indicates the refusal to perform, or continue performing, the indicated option.*/
    public static final int WILL_NOT          = 0xFC;
    /**Indicates the request that the other party perform, or confirmation that you are expecting the other party to
     * perform, the indicated option.*/
    public static final int DO                = 0xFD;
    /**Indicates the demand that the other party stop performing, or confirmation that you are no longer expecting the
     * other party to perform, the indicated option.*/
    public static final int DO_NOT            = 0xFE;
    /**Suppress go ahead.*/
    public static final int OPT_SUPPRESS_GO_AHEAD = 0x01;
    /**Echo option.*/
    public static final int OPT_ECHO = 0x03;
    /**0xFF byte value.*/
    public static final int BYTE_FF = 0xFF;
}
