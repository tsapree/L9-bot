package biz.atomeo.l9.api;

public interface TextOutputAdapter {
    void printChar(char c);
    void flush();
    String getMessage();
}
