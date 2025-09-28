package biz.atomeo.l9.legacy;

public class L9Picture {
    public int width, height;
    public byte[] bitmap;
    public int[] palette;
    public int npalette;

    public L9Picture(int x, int y) {
        palette=new int[32];
        bitmap=new byte[x*y];
        width = x;
        height = y;
        npalette = 0;
    }

    public L9Picture copy() {
        L9Picture copy = new L9Picture(width, height);
        copy.palette = palette;
        copy.palette = palette.clone();
        copy.bitmap = bitmap.clone();
        return copy;
    }

    public byte point(int x, int y) {
        if (x<0 || x>= width || y<0 || y>= height) return (byte)0xff;
        return bitmap[y* width + x];
    };

    public void plot(int x, int y, int colour1, int colour2) {
        if (x<0 || x>= width || y<0 || y>= height) return;
        if (bitmap[y * width + x] == colour2)
            bitmap[y * width + x]=(byte)colour1;
    };
};