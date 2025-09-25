package biz.atomeo.l9;

import biz.atomeo.l9.legacy.L9;

public class L9BotConnector extends L9 {
    final IOAdapter ioAdapter;
    final TextOutputAdapter textOutputAdapter;
    final InputAdapter inputAdapter;

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
    public void os_graphics(int mode) {}

    @Override
    public void os_cleargraphics() {}

    @Override
    public void os_setcolour(int colour, int index) {}

    @Override
    public void os_drawline(int x1, int y1, int x2, int y2, int colour1, int colour2) {}

    @Override
    public void os_fill(int x, int y, int colour1, int colour2) {}

    @Override
    public void os_show_bitmap(int pic, int x, int y) {}

    @Override
    public void os_debug(String str) {
        //System.out.println("D:"+str);
    }

    @Override
    public void os_verbose(String str) {
        //System.out.println("V:"+str);
    }
}
