package biz.atomeo.l9;

import biz.atomeo.l9.legacy.L9;
import biz.atomeo.l9.legacy.L9Bitmap;
import biz.atomeo.l9.legacy.L9Picture;
import biz.atomeo.l9.legacy.PictureSize;

import java.util.ArrayList;
import java.util.List;

public class L9BotConnector extends L9 {
    /*
     * use GIF_ANIMATION=true for enabling animated gif file generation
     */
    public final static boolean GIF_ANIMATION = false;

    /*
     * if GIF_ANIMATION=true, GIF_ANIMATION_COMMANDS_PER_FRAME accumulates
     * some commands per frame
     */
    private final static int GIF_ANIMATION_COMMANDS_PER_FRAME = 10;

    // if picture found in cache, do not need to regenerate it, mockDrawing save this state
    boolean mockDrawing = false;

    final IOAdapter ioAdapter;
    final TextOutputAdapter textOutputAdapter;
    final InputAdapter inputAdapter;

    private L9Painter painter = null;

    private int l9BitmapType = 0;
    private int lastpic = -1;
    private int picMode;

    public L9BotConnector(TextOutputAdapter textOutputAdapter,
                          InputAdapter inputAdapter,
                          IOAdapter ioAdapter) {
        super();
        this.ioAdapter = ioAdapter;
        this.textOutputAdapter = textOutputAdapter;
        this.inputAdapter = inputAdapter;
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

    @Override
    public void os_graphics(int mode) {
        picMode = mode;
        if (mode==2) {
            l9BitmapType = L9Bitmap.detectBitmaps(ioAdapter);
            if (l9BitmapType == L9Bitmap.NO_BITMAPS) picMode = 0;
        };

        PictureSize ps = getPictureSize();
        if (ps.getWidth() <=0 || ps.getHeight() <=0 || mode==0) return;
        L9Picture l9Picture = new L9Picture(ps.getWidth(), ps.getHeight());
        painter = new L9Painter(l9Picture);
    }

    @Override
    public void os_cleargraphics() {
        if (picMode ==0 || picMode ==2 || painter==null) return;

        painter.clear();
    }

    @Override
    public void os_start_drawing(int pic) {
        if (ioAdapter.isPictureCached(pic)) mockDrawing = true;
        if (painter==null) return;

        List<L9Picture> pictures = new ArrayList<>();
        while (periodGfxTask(GIF_ANIMATION)) {
            L9Picture picCopy = painter.getL9PictureCopy();
            if (picCopy!=null) pictures.add(picCopy);
        }

        if (!pictures.isEmpty()) {
            ioAdapter.cachePicture(pic, pictures);
        }
    }

    private boolean periodGfxTask(boolean enabledAnimation) {
        boolean needToPaintMore = false;
        if (picMode != 1 || painter == null) return false;

        if (!enabledAnimation) {
            while ((painter.fillStep() > 0) || runGraphics())
                needToPaintMore = true;
        } else {
            for (int i=0; i < GIF_ANIMATION_COMMANDS_PER_FRAME; i++)
                if (painter.fillStep() > 0) needToPaintMore = true;
                else if (runGraphics()) needToPaintMore = true;
        }
        return needToPaintMore;
    }

    @Override
    public void os_setcolour(int colour, int index) {
        if (mockDrawing || picMode ==0 || picMode == 2 || painter == null) return;

        painter.setColour(colour, index);
    }

    @Override
    public void os_drawline(int x1, int y1, int x2, int y2, int colour1, int colour2) {
        if (mockDrawing || picMode !=1 || painter == null) return;

        painter.drawLine(x1, y1, x2, y2, colour1, colour2);
    }

    @Override
    public void os_fill(int x, int y, int colour1, int colour2) {
        if (mockDrawing || picMode ==0 || picMode ==2 || painter == null) return;

        painter.fill(x, y, colour1, colour2);
    }

    @Override
    public void os_show_bitmap(int pic, int x, int y) {
        if (ioAdapter.isPictureCached(pic)) return;

        if (picMode ==0 || picMode ==1 ) {
            lastpic=-1;
            return;
        }

        if (pic!=lastpic) {
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
