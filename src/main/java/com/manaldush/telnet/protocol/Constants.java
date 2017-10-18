package com.manaldush.telnet.protocol;

/**
 * Constants file.
 * Created by Maxim.Melnikov on 14.06.2017.
 */
public final class Constants {
    //THE NVT PRINTER AND KEYBOARD
    //Moves the printer to the left margin of the current line.
    public static final int CR   = 0x0D;
    //Moves the printer to the next print line, keeping the same horizontal position.
    public static final int LF   = 0x0A;

    public static final byte[] CRLF = {CR , LF};

    /**ANSI Red color.*/
    public static final String	RED	= "\u001B[31m";
    /**ANSI Green color.*/
    public static final String	GREEN	= "\u001B[32m";
    /**Reset color.*/
    public static final String	RESET_COLOR	= "\u001B[0m";
    //No Operation
    public static final int NUL  = 0x00;
    //Produces an audible or visible signal (which does NOT move the print head).
    public static final int BEL  = 0x07;
    //Moves the print head one character position towards the left margin.
    public static final int BS  = 0x08;
    //Moves the printer to the next horizontal tab stop.
    // It remains unspecified how either party determines or establishes where such tab stops are located.
    public static final int HT  = 0x09;
    // Moves the printer to the next vertical tab stop.
    // It remains unspecified how either party determines or establishes where such tab stops are located.
    public static final int VT  = 0x0B;
    // Moves the printer to the top of the next page, keeping the same horizontal position.
    public static final int FF  = 0x0C;

    //TELNET COMMAND
    public static final int IAC               = 0xFF;
    public static final int SE                = 0xF0;// End of subnegotiation parameters.
    public static final int NOP               = 0xF1;// No operation.
    public static final int DATA_MARK         = 0xF2;// The data stream portion of a Synch.This should always be accompanied by a TCP Urgent notification.
    public static final int Break             = 0xF3;// NVT character BRK.
    public static final int Interrupt_Process = 0xF4;// The function IP.
    public static final int Abort_Output      = 0xF5;// The function AO.
    public static final int Are_You_There     = 0xF6;// The function AYT.
    public static final int Erase_character   = 0xF7;// The function EC.
    public static final int Erase_Line        = 0xF8;// The function EL.
    public static final int Go_ahead          = 0xF9;// The GA signal.
    public static final int SB                = 0xFA;// Indicates that what follows is subnegotiation of the indicated option.
    public static final int WILL              = 0xFB;// Indicates the desire to begin performing, or confirmation that you are now performing, the indicated option.
    public static final int WILL_NOT          = 0xFC;// Indicates the refusal to perform, or continue performing, the indicated option.
    public static final int DO                = 0xFD;// Indicates the request that the other party perform, or confirmation that you are expecting the other party to perform, the indicated option.
    public static final int DO_NOT            = 0xFE;// Indicates the demand that the other party stop performing, or confirmation that you are no longer expecting the other party to perform, the indicated option.
    public static final int OPT_SUPPRESS_GO_AHEAD = 0x01;
    public static final int OPT_ECHO = 0x03;
}