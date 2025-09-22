package biz.atomeo.l9.legacy;

public class L9Picture {
    int width, height;
    byte[] bitmap;
    int palette[];
    int npalette;

    L9Picture() {
        palette=new int[32];
        bitmap=null;
        width = 0;
        height = 0;
        npalette = 0;
    }
    L9Picture(int x, int y) {
        palette=new int[32];
        bitmap=new byte[x*y];
        width = x;
        height = y;
        npalette = 0;
    }
};