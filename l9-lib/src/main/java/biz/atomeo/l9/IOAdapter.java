package biz.atomeo.l9;

import biz.atomeo.l9.legacy.L9Picture;

import java.util.List;

public interface IOAdapter {
    byte[] loadFile(String fileName);
    String getGamePath(L9Game game);
    String getPicPath(L9Game game);

    boolean fileExist(String file);
    byte[] fileLoadRelativeToArray(String file);

    // check if it is file in cache, and mark to msg it.
    boolean isPictureCached(int picture);
    //create file with picture and cache it
    void cachePicture(int picture, List<L9Picture> pictures);
    //get list of pictures
    Object popPictures();
}
