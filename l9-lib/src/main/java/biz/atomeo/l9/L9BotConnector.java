package biz.atomeo.l9;

import biz.atomeo.l9.legacy.L9;
import biz.atomeo.l9.legacy.L9Bitmap;
import biz.atomeo.l9.legacy.L9Picture;

import java.util.ArrayList;
import java.util.List;

public class L9BotConnector extends L9 {
    final IOAdapter ioAdapter;
    final TextOutputAdapter textOutputAdapter;
    final InputAdapter inputAdapter;
    boolean mockDrawing = false;
    public final static boolean GIF_ANIMATION = false;

    public L9BotConnector(TextOutputAdapter textOutputAdapter,
                          InputAdapter inputAdapter,
                          IOAdapter ioAdapter) {
        super();
        this.ioAdapter = ioAdapter;
        this.textOutputAdapter = textOutputAdapter;
        this.inputAdapter = inputAdapter;

        l9FillStack=new int[L9Fill_StackSize];
        SelectedPalette=new int[32];
    }

    @Override
    public void os_printchar(char c) {
        textOutputAdapter.printChar(c);
    }

    @Override
    public void os_flush() {
        textOutputAdapter.flush();
    }

    @Override
    public char os_readchar(int millis) {
        return '\n';
    }

    @Override
    public byte[] os_load(String filename) {
        return ioAdapter.loadFile(filename);
    }

    @Override
    public byte[] os_open_script_file() {
        return new byte[0];
    }

    @Override
    public String os_get_game_file(String NewName) {
        return "";
    }

    @Override
    public String os_set_filenumber(String NewName, int num) {
        return "";
    }

    @Override
    public boolean os_save_file(byte[] buff) {
        return false;
    }

    @Override
    public byte[] os_load_file() {
        return new byte[0];
    }

    private final static int PICSPEED = 10;
    private L9Picture l9Picture;

    @Override
    public void os_graphics(int mode) {
        picMode = mode;
        if (mode==2) {
            l9BitmapType = L9Bitmap.detectBitmaps(ioAdapter);
            if (l9BitmapType == L9Bitmap.NO_BITMAPS) picMode = 0;
        };

        int[] pw={0};
        int[] ph={0};
        GetPictureSize(pw,ph);
        picWidth =pw[0];
        picHeight =ph[0];

        if (picWidth <=0 || picHeight <=0 || mode==0) return;
        l9Picture = new L9Picture(picWidth, picHeight);
    }

    @Override
    public void os_cleargraphics() {
        if (picMode ==0 || picMode ==2 || l9Picture==null) return;
        l9FillCount = 0;
        for (int i = 0; i< l9Picture.height * l9Picture.width; i++)
            l9Picture.bitmap[i]=0;
    }

    @Override
    public void os_start_drawing(int pic) {
        if (ioAdapter.isPictureCached(pic)) mockDrawing = true;

        List<L9Picture> pictures = new ArrayList<>();
        while (l9DoPeriodGfxTask(GIF_ANIMATION)) {
            if (l9Picture!=null) pictures.add(l9Picture.copy());
        }

        if (!pictures.isEmpty()) {
            ioAdapter.cachePicture(pic, pictures);
        }
    }

    boolean l9DoPeriodGfxTask(boolean enabledAnimation) {
        int j = 0;
        if (picMode ==0) return false;
        else if (picMode ==1) {
            if (!enabledAnimation) {
                while ((l9FillStep()>0) || runGraphics()) j++;
            } else {
                int steps = PICSPEED;
                for (int i=0; i<steps; i++)
                    if (l9FillStep()>0) j++;
                    else if (runGraphics()) j++;
            }
        }
        return j!=0;
    }

    int[] L9PaletteIndexes={0,0,0,0};
    int[] SelectedPalette;

    @Override
    public void os_setcolour(int colour, int index) {
        if (mockDrawing) return;

        if (picMode ==0 || picMode ==2) return;
        if ((index>7) || (index<0) || (colour >3) || (colour<0)) return;
        L9PaletteIndexes[colour]=index;
        L9UpdatePalette();
    }

    void L9UpdatePalette() {
        for (int i=0;i<4;i++) {
            //if ((th!=null) && (th.activity!=null) && (th.activity.pref_picpaletteamiga))
            //    l9Picture.palette[i]=PaletteAmiga[L9PaletteIndexes[i]];
            //else
                l9Picture.palette[i]=PaletteSpectrum[L9PaletteIndexes[i]];
        };
    };

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

    @Override
    public void os_drawline(int x1, int y1, int x2, int y2, int colour1, int colour2) {
        if (mockDrawing || picMode !=1) return;

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
                };
            };
            l9Picture.plot(x2, y2, colour1, colour2);
        } else {
            x0 = x1;
            for (y0 = y1; y0 != y2; y0 += zy) {
                l9Picture.plot(x0, y0, colour1, colour2);
                err += sx;
                if (2 * err >= sy) {
                    x0 += zx;
                    err -= sy;
                };
            };
            l9Picture.plot(x2, y2, colour1, colour2);
        };
    }

    //размер буфера заливки, в словах.
    final static int L9Fill_StackSize=512;
    int[] l9FillStack;
    //текущее заполнение буфера заливки
    int l9FillCount;
    //текущие цвета для заливки.
    int l9FillColour1;
    int l9FillColour2;

    @Override
    public void os_fill(int x, int y, int colour1, int colour2) {
        if (mockDrawing) return;

        l9FillCount = 0;
        l9FillColour1 = colour1;
        l9FillColour2 = colour2;
        if (picMode ==0 || picMode ==2) return;
        if (x<0 || x> picWidth || y<0 || y> picHeight) return;
        l9FillStack[l9FillCount++]=x;
        l9FillStack[l9FillCount++]=y;
    }

    int l9FillStep() {
        int x;
        int y;
        if (l9FillCount > 0) {
            y = l9FillStack[--l9FillCount];
            x = l9FillStack[--l9FillCount];

            boolean fillingUp = false;
            boolean fillingDown = false;
            while ((x>0) && (l9Picture.point(x-1,y) == l9FillColour2))
                x--;
            while ((x< picWidth) && (l9Picture.point(x,y) == l9FillColour2)) {
                l9Picture.plot(x, y, l9FillColour1, l9FillColour2);

                if (y-1 >= 0) {
                    if (l9Picture.point(x,y-1) == l9FillColour2) {
                        if (!fillingUp) {
                            if (l9FillCount < L9Fill_StackSize-2) {
                                l9FillStack[l9FillCount++]=x;
                                l9FillStack[l9FillCount++]=y-1;
                            };
                            fillingUp = true;
                        };
                    } else fillingUp = false;
                }

                if (y+1< picHeight) {
                    if (l9Picture.point(x,y+1)== l9FillColour2) {
                        if (!fillingDown) {
                            if (l9FillCount < L9Fill_StackSize-2) {
                                l9FillStack[l9FillCount++] = x;
                                l9FillStack[l9FillCount++] = y+1;
                            }
                            fillingDown = true;
                        }
                    } else fillingDown=false;
                }

                x++;
            };
        };
        return l9FillCount;
    };

    int l9BitmapType =0;
    int lastpic=-1;

    private int picWidth =0;
    private int picHeight =0;
    private int picMode;

    @Override
    public void os_show_bitmap(int pic, int x, int y) {
        if (ioAdapter.isPictureCached(pic)) return;

        if (picMode ==0 || picMode ==1 ) {
            lastpic=-1;
            return;
        }

        if (pic!=lastpic || picWidth ==0 || picHeight ==0) {
            lastpic=pic;
            L9Picture l9picture = L9Bitmap.decodeBitmap(ioAdapter, l9BitmapType, pic, x, y);
            if (l9picture!=null) {
                ioAdapter.cachePicture(pic, List.of(l9picture));
            }
        }
    }

    @Override
    public void os_debug(String str) {
        //System.out.println("D:"+str);
    }

    @Override
    public void os_verbose(String str) {
        //System.out.println("V:"+str);
    }
}
