package biz.atomeo.l9.error;

public class L9Exception extends Exception {
    public L9Exception(String message) {
        super(message);
    }

    public L9Exception(String message, Throwable cause) {
        super(message, cause);
    }
}
