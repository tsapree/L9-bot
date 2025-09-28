package biz.atomeo.l9.legacy;

//import biz.atomeo.l9.legacy.androidMocks.SpannableStringBuilder;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

//import android.graphics.Bitmap;
//import android.os.Handler;
//import android.os.Message;
//import android.text.SpannableStringBuilder;
//import android.util.Log;
//import android.widget.EditText;

public class L9implement extends L9 {
	String cmdStr;
	//DebugStorage ds;
	String vStr;
	//Handler mHandler;
	Library lib;
	Threads th;
	//ArrayList<SpannableStringBuilder> tempLog;
	
	//EditText et;
	byte gamedata[];

	L9implement( Library l, Threads t) {
		lib=l;
		gamedata=null;
		cmdStr=null;
		//ds=new DebugStorage();
		th=t;
	};

    @Override
    public void os_printchar(char c) {
		//if (c==0x0d) log_debug(ds.getstr());
		//else if (ds.putchar(c)) log_debug(ds.getstr());
        //Message msg = mHandler.obtainMessage(Threads.MACT_PRINTCHAR, c, 0);
		//mHandler.sendMessage(msg);
	};

    @Override
    public byte[] os_load(String filename) {
		return lib.fileLoadGame(filename);
	};

    @Override
    public void os_debug(String str) {
		//log_debug(ds.getstr());
		log_debug(str);
	};

    @Override
    public void os_verbose(String str) {
		log_verbose(str);
	};

	void log_debug(String str) {
		final String LOG_TAG = "l9droid";
	//	if (str.length()>0) 
	//		Log.d(LOG_TAG, str);
	};
	
	void log_verbose(String str) {
		final String LOG_TAG = "l9droid";
	//	if (str.length()>0) 
	//		Log.v(LOG_TAG, str);
	};

    public void os_flush() {

	}
	
	void step() {
		while (L9State==L9StateRunning || L9State==L9StateCommandReady) RunGame();
	};

    //плейбэк скрипта
    public byte[] os_open_script_file() {
		byte[] script = {'u','n','f','a','s',' ','p','a','r','a','\r',
				'u','\r',
				't','a','k','e',' ','p','a','r','a','\r'
		};
		return script;
	};

    @Override
    public void os_graphics(int mode) {
	};


    @Override
    public void os_cleargraphics() {
	}

    @Override
    public void os_start_drawing(int pic) {

    }

    @Override
    public void os_show_bitmap(int pic, int x, int y) {}

    @Override
    public void os_setcolour(int colour, int index) {}

    @Override
    public void os_drawline(int x1, int y1, int x2, int y2, int colour1, int colour2) {}

    @Override
    public void os_fill(int x, int y, int colour1, int colour2) {}

	String findPictureFile(String filename) {
		String pictureFile=lib.changeFileExtension(filename, "pic");
		if (lib.FileExist(pictureFile)) return pictureFile;
		pictureFile=lib.changeFileExtension(filename, "pic");
		if (lib.FileExist(pictureFile)) return pictureFile;
		pictureFile=lib.changeFileExtension(pictureFile, "cga");
		if (lib.FileExist(pictureFile)) return pictureFile;
		pictureFile=lib.changeFileExtension(pictureFile, "hrc");
		if (lib.FileExist(pictureFile)) return pictureFile;
		pictureFile="picture.dat";
		if (lib.FileExist(pictureFile)) return pictureFile;
		return null;
	}

    @Override
    public String os_get_game_file(String NewName) {
		return os_set_filenumber(NewName,0);
	};

    @Override
    public String os_set_filenumber(String NewName, int num) {
		int i=NewName.length();
		char c;
		while (i>0) {
			c=NewName.charAt(--i);
			if (c=='/' || c=='\\') return NewName;
			if (c>'0' && c<='9') break;
		};
		return NewName.substring(0, i)+String.valueOf(num)+NewName.substring(i+1, NewName.length());
	};

    @Override
	public char os_readchar(int millis) {
		char key=0;
		if (millis!=0) {
			//mHandler.sendEmptyMessage(Threads.MACT_L9WAITFORCHAR);
			try {
				for (int i=0;i<millis;i++) {
					TimeUnit.MILLISECONDS.sleep(100);
					if ((th!=null) && (th.keyPressed!=0)) {
						key=th.keyPressed;
						break;
					}
				};
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//mHandler.sendEmptyMessage(Threads.MACT_L9WORKING);
		}
		return key;
	};

    @Override
	public boolean os_save_file(byte[] buff) {
		String prefix = "state";
		if (th!=null && th.activity!=null) prefix=th.activity.pref_syssaveprefix;
		String path=Library.DIR_SAVES+"/"+prefix+".sav";

		path=lib.getAbsolutePath(path);
		path=lib.unifyFile(path);
		save_piclog(path);
		return lib.fileSaveFromArray(path,buff);
	};

    @Override
	public byte[] os_load_file() {
		th.choosing_restore_filename=true;
		//mHandler.sendEmptyMessage(Threads.MACT_L9SELECTFILENAMETORESTORE);
		try {
			while (th.choosing_restore_filename) {
				TimeUnit.MILLISECONDS.sleep(100);
			};
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String path=th.choosed_restore_filename;
		if ((path==null) || (path.length()<1)) return null;
		path=lib.getAbsolutePath(path);
//		load_piclog(path,th.history);
		//mHandler.sendEmptyMessage(Threads.MACT_REPLACE_LOG);
		return lib.fileLoadToArray(path);
	};
	
	public boolean restore_autosave(String path) {

		if (path==null) return false;
		byte[] buff=lib.fileLoadToArray(path);
		GameState tempGS=new GameState();
		if (buff==null) return false;
		if (tempGS.setFromCloneInBytes(buff, l9memory, listarea)) {
			workspace=tempGS.clone();
			codeptr=acodeptr+workspace.codeptr;
//			load_piclog(path,th.history);
			//mHandler.sendEmptyMessage(Threads.MACT_REPLACE_LOG);
			return true;
		};
		return false;
	};
	
	public boolean autosave(String path) {
		workspace.codeptr=(short)((codeptr-acodeptr)&0xffff);
		workspace.listsize=LISTAREASIZE;
		workspace.stacksize=STACKSIZE;
		workspace.filename=LastGame;
		save_piclog(path);
		byte[] buff=workspace.getCloneInBytes(l9memory, listarea);
//		if (!lib.fileSaveFromArray(path, buff))
//	    	return false;
//		else
	    	return true;
	};
	
	void save_piclog(String path) {
		String name;
		
//		name=lib.changeFileExtension(path, "log");
//		lib.SaveLogFromSpannableArrayAdapter(name, th.lvAdapter, th.logStrId);
//
//		name=lib.changeFileExtension(path, "png");
//		if (PicMode!=0) waitPictureToDraw();
//		if (bm!=null) lib.pictureSaveFromBitmap(name, bm);
//		else lib.deleteFile(name);
	}

//	void load_piclog(String path, History h) {
//		String name;
//		name=lib.changeFileExtension(path, "png");
//		waitPictureToDraw();
//		bm=lib.pictureLoadToBitmap(name);
//		if (bm!=null) mHandler.sendEmptyMessage(Threads.MACT_GFXUPDATE);
//
//		name=lib.changeFileExtension(path, "log");
//		tempLog=lib.LoadLogToSpannableArrayList(name,(th!=null&&th.activity!=null)?th.activity.pref_logcommandcolor:0);
//		h.clear();
//		for (SpannableStringBuilder logStr:tempLog) {
//			h.add(lib.getSpannedString(logStr));
//		}
//	}

}
