package biz.atomeo.l9.legacy;

public class DebugStorage {
    private final char[] debug;
    private int debugptr;
    private static final int debugsize=500;

    DebugStorage() {
        debug=new char[debugsize];
        debugptr=0;
    }

    boolean putchar(char c) {
        debug[debugptr++]=c;
        return (debugptr>=debugsize);
    }

    String getstr() {
        String str=String.valueOf(debug, 0, debugptr);
        debugptr=0;
        return str;
    }
}