package biz.atomeo.l9.legacy.androidMocks;

public interface Handler {
    default void sendEmptyMessage(int what) {};
    default Message obtainMessage(int i0, char c, int i1) {return null; };
    default Message obtainMessage(int i0, String c) {return null;};
    default void sendMessage(Message message) {};
    public void handleMessage(Message msg);
    default void removeMessages(int msg) {};
}
