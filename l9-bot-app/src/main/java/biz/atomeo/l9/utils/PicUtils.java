package biz.atomeo.l9.utils;

import biz.atomeo.l9.graphics.L9Picture;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.pixels.Pixel;

public class PicUtils {
    public static ImmutableImage l9PictureToImmutableImage(L9Picture pic) {
        Pixel[] pixels = new Pixel[pic.bitmap.length];
        int n = 0;
        //Not very efficient
        for (int y = 0; y < pic.height; y++)
            for (int x = 0; x < pic.width; x++)
                pixels[y*pic.width+x] = new Pixel(x, y, pic.palette[pic.bitmap[n++]] | 0xff000000);
        return ImmutableImage.create(pic.width, pic.height, pixels);
    }
}
