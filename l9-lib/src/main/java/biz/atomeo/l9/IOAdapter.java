package biz.atomeo.l9;

public interface IOAdapter {
    byte[] loadFile(String fileName);
    String getGamePath(L9Game game);
    String getPicPath(L9Game game);
}
