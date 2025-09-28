package biz.atomeo.l9.graphics;

public class L9Painter {
    final L9Picture l9Picture;

    private final static int[] PaletteAmiga = {
            0xff000000,
            0xffff0000,
            0xff30e830,
            0xffffff00,
            0xff0000ff,
            0xffa06800,
            0xff00ffff,
            0xffffffff
    };

    //these colors taken from unreal speccy emulator, default palette
    //on speccy 8 colors plus 2 grade of bright.
    //in l9 games bright often set to 0 on pictures.
    private final static int[] PaletteSpectrum = {
            0xff000000,		//(black)  0
            0xffC00000,		//(red)    2
            0xff00C000,		//(green)  4
            0xffC0C000,		//(yellow) 6
            0xff0000C0,		//(blue)   1
            0xffC000C0,		//(brown)  3
            0xff00C0C0,		//(cyan)   5
            0xffC0C0C0		//(white)  7
    };

    private final int[] L9PaletteIndexes={0,0,0,0};

    private final static int L9Fill_StackSize=512;
    private final int[] l9FillStack;

    private int fillCount;
    private int l9FillColour1;
    private int l9FillColour2;

    public L9Painter(L9Picture l9Picture) {
        this.l9Picture = l9Picture;

        l9FillStack=new int[L9Fill_StackSize];
    }

    public void clear() {
        fillCount = 0;
        l9Picture.clear();
    }

    public L9Picture getL9PictureCopy() {
        return l9Picture.copy();
    }

    public void setColour(int colour, int index) {
        if ((index>7) || (index<0) || (colour >3) || (colour<0)) return;

        L9PaletteIndexes[colour]=index;
        for (int i=0;i<4;i++) {
            //if ((th!=null) && (th.activity!=null) && (th.activity.pref_picpaletteamiga))
            //    l9Picture.palette[i]=PaletteAmiga[L9PaletteIndexes[i]];
            //else
            l9Picture.palette[i]=PaletteSpectrum[L9PaletteIndexes[i]];
        }
    }

    public void drawLine(int x1, int y1, int x2, int y2, int colour1, int colour2) {
        int x0;
        int y0;
        int sx = Math.abs(x2 - x1);
        int sy = Math.abs(y2 - y1);
        int zx = x2 > x1 ? 1 : -1;
        int zy = y2 > y1 ? 1 : -1;
        int err = 0;

        if (sx >= sy) {
            y0 = y1;
            for (x0 = x1; x0 != x2; x0 += zx) {
                l9Picture.plot(x0, y0, colour1, colour2);
                err += sy;
                if (2*err >= sx) {
                    y0+=zy;
                    err-=sx;
                }
            }
            l9Picture.plot(x2, y2, colour1, colour2);
        } else {
            x0 = x1;
            for (y0 = y1; y0 != y2; y0 += zy) {
                l9Picture.plot(x0, y0, colour1, colour2);
                err += sx;
                if (2 * err >= sy) {
                    x0 += zx;
                    err -= sy;
                }
            }
            l9Picture.plot(x2, y2, colour1, colour2);
        }
    }

    public void fill(int x, int y, int colour1, int colour2) {
        fillCount = 0;

        if (x<0 || x> l9Picture.width || y<0 || y> l9Picture.height) return;

        l9FillColour1 = colour1;
        l9FillColour2 = colour2;
        l9FillStack[fillCount++]=x;
        l9FillStack[fillCount++]=y;
    }

    public int fillStep() {
        if (fillCount > 0) {
            int y = l9FillStack[--fillCount];
            int x = l9FillStack[--fillCount];

            boolean fillingUp = false;
            boolean fillingDown = false;

            while ((x>0) && (l9Picture.point(x-1,y) == l9FillColour2))
                x--;

            while ((x< l9Picture.width) && (l9Picture.point(x,y) == l9FillColour2)) {
                l9Picture.plot(x, y, l9FillColour1, l9FillColour2);

                if (y-1 >= 0) {
                    if (l9Picture.point(x,y-1) == l9FillColour2) {
                        if (!fillingUp) {
                            if (fillCount < L9Fill_StackSize-2) {
                                l9FillStack[fillCount++]=x;
                                l9FillStack[fillCount++]=y-1;
                            }
                            fillingUp = true;
                        }
                    } else fillingUp = false;
                }

                if (y+1 < l9Picture.height) {
                    if (l9Picture.point(x,y+1)== l9FillColour2) {
                        if (!fillingDown) {
                            if (fillCount < L9Fill_StackSize-2) {
                                l9FillStack[fillCount++] = x;
                                l9FillStack[fillCount++] = y+1;
                            }
                            fillingDown = true;
                        }
                    } else fillingDown=false;
                }

                x++;
            }
        }
        return fillCount;
    }
}
