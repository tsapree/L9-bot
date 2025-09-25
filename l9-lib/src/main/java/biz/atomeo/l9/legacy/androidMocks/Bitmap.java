package biz.atomeo.l9.legacy.androidMocks;

import java.io.OutputStream;

//Mock for android class
public class Bitmap {
    private int height;
    private int width;
    private int cfg;

    public Bitmap(int w, int h, int cfg) {
        this.width = w;
        this.height = h;
        this.cfg = cfg;
    }

    public void setPixels(int[] buff, int m, int w0, int a, int b, int w1, int h1) {

    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void compress(int fmt, int i, OutputStream out) {
    }

    public Bitmap copy(int newCfg, boolean flag) {
        return new Bitmap(width, height, newCfg);
    }

    public static Bitmap createBitmap(int w, int h, int cfg) {
        return new Bitmap(w, h, cfg);
    }

    public static class Config {
        public final static int ARGB_8888 = 0;
    }

    public static class CompressFormat {
        public final static int PNG = 0;
    }
}
