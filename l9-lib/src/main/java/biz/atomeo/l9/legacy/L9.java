/***********************************************************************\
*
* Level 9 interpreter
* Version 5.1
* Copyright (c) 1996-2011 Glen Summers and contributors.
* Contributions from David Kinder, Alan Staniforth, Simon Baldwin,
* Dieter Baron and Andreas Scherrer.
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111, USA.
*
* The input routine will repond to the following 'hash' commands
*  #save         saves position file directly (bypasses any
*                disk change prompts)
*  #restore      restores position file directly (bypasses any
*                protection code)
*  #quit         terminates current game, RunGame() will return FALSE
*  #cheat        tries to bypass restore protection on v3,4 games
*                (can be slow)
*  #dictionary   lists game dictionary (press a key to interrupt)
*  #picture <n>  show picture <n>
*  #seed <n>     set the random number seed to the value <n>
*  #play         plays back a file as the input to the game
*
\***********************************************************************/

package biz.atomeo.l9.legacy;

//char		16 bit
//byte		signed 8 bit	->	L9BYTE		unsigned 8 bit quantity
//short 	signed 16 bit	->	L9UINT16	unsigned 16 bit quantity
//int		signed 32 bit	->	L9UINT32	unsigned 32 bit quantity
//boolean					->	L9BOOL		quantity capable of holding the values TRUE (1) and FALSE (0)
//
//0=false
//1=true 
//if (var) -> if(var!=0)
//*getvar()->workspace.vartable[getvar()]&0xffff

import biz.atomeo.l9.constants.L9GameType;
import biz.atomeo.l9.graphics.L9Bitmap;
import biz.atomeo.l9.graphics.PictureSize;

import static biz.atomeo.l9.legacy.L9Utils.*;

abstract public class L9 {
	
	public static final int LISTAREASIZE = 0x800;
	public static final int STACKSIZE = 1024;
	
	private static final int IBUFFSIZE = 500;

	private static final int FIRSTLINESIZE = 96;
	
	private int showtitle=1;
	
    public GameState workspace;
	private short randomseed;
	private short constseed=0;
	public int L9State;
	public static final int L9StateStopped=0;
	public static final int L9StateRunning=1;
	public static final int L9StateWaitForCommand=2;
	public static final int L9StateCommandReady = 3;
	public static final int L9StateWaitBeforeScriptCommand=5;
	public static final int L9StateWaitForKey=6; //TODO
	public static final int L9StateKeyReady=7;   //TODO

	public String LastGame;
	private char[] FirstLine;
	private int FirstLinePos=0;
	private int FirstPicture=-1;
	

//// "L901"
//#define L9_ID 0x4c393031
//
	private static final int RAMSAVESLOTS = 10;
//
//
// Enumerations 
//enum L9GameTypes {L9_V1,L9_V2,L9_V3,L9_V4};
    /*
	private static final int L9_V1=1;
	private static final int L9_V2=2;
	private static final int L9_V3=3;
	private static final int L9_V4=4;

     */
//enum L9MsgTypes { MSGT_V1, MSGT_V2 };
int MSGT_V1=1;
int MSGT_V2=2;
//
/*
Graphics type    Resolution     Scale stack reset
-------------------------------------------------    
GFX_V2           160 x 128            yes
GFX_V3A          160 x 96             yes
GFX_V3B          160 x 96             no
GFX_V3C          320 x 96             no
*/
//enum L9GfxTypes { GFX_V2, GFX_V3A, GFX_V3B, GFX_V3C };
//TOTO: enum?
	private static final int GFX_V2=1;
	private static final int GFX_V3A=2;
	private static final int GFX_V3B=3;
	private static final int GFX_V3C=4;

// Global Variables
//*pictureaddress=NULL
	private int pictureaddress=-1;
    private int picturedata=-1;
    private int picturesize;
    public byte[] l9memory;
    private int startfile;
    private int filesize;
    private int startdata;
    private int datasize;
    public int listarea;

    private int[] L9Pointers;
    private int absdatablock;
    private int list2ptr;
    private int list3ptr;
    private int list9startptr;
    public int acodeptr;
    private int startmd;
    private int endmd;
    private int endwdp5;
    private int wordtable;
    private int dictdata;
    private int defdict;
    private int dictdatalen;
    private int startmdV2;
//
    private int wordcase;
    private int unpackcount;
    private int[] unpackbuf;
    private int dictptr;
    private byte[] threechars;
    private L9GameType l9GameType;
    private int L9MsgType;
//
    private SaveStruct[] ramsavearea;
//
    L9Bitmap l9bitmap;

    private char[] obuff;
    private int wordcount;
    private char[] ibuff;
    private String ibuffstr;
    private int ibuffptr;

    private String InputString;
    private byte[] scriptArray=null;
    private int scriptArrayIndex=0;
	
//
    private boolean Cheating=false;
    private int CheatWord;
    private GameState CheatWorkspace;
//
    private int reflectflag,scale,gintcolour,option;
    private int l9textmode=0;
    private int drawx=0,drawy=0;
    private int screencalled=0;
    private int[] gfxa5={-1};
//Bitmap* bitmap=NULL;

    private int gfx_mode=GFX_V2;
	
	public static final int GFXSTACKSIZE=100;

    private int[] GfxA5Stack;
    private int GfxA5StackPos=0;
    private int[] GfxScaleStack;
    private int GfxScaleStackPos=0;
	
//
    private char lastchar='.';
    private char lastactualchar=0;
    private int d5;
//
    public int codeptr;	// instruction codes - pointer
    private int code;		// instruction codes - code
//
    private int list9ptr;
//
    private int unpackd3;
//
    private short[] exitreversaltable = {0x00,0x04,0x06,0x07,0x01,0x08,0x02,0x03,0x05,0x0a,0x09,0x0c,0x0b,0xff,0xff,0x0f};
//
	//L9UINT16 gnostack[128];
	//L9BYTE gnoscratch[32];
    private int[] gnostack;
    private short[] gnoscratch;
    private short searchdepth;
    private short inithisearchpos;
    private short gnosp;
    private short object;
    private short numobjectfound;
	
	//struct L9V1GameInfo
	//{
	//	L9BYTE dictVal1, dictVal2;
	//	int dictStart, L9Ptrs[5], absData, msgStart, msgLen;
	//};
	//struct L9V1GameInfo L9V1Games[] =
	//{
	//	0x1a,0x24,301, 0x0000,-0x004b, 0x0080,-0x002b, 0x00d0,0x03b0, 0x0f80,0x4857, /* Colossal Adventure */
	//	0x20,0x3b,283,-0x0583, 0x0000,-0x0508,-0x04e0, 0x0000,0x0800, 0x1000,0x39d1, /* Adventure Quest */
	//	0x14,0xff,153,-0x00d6, 0x0000, 0x0000, 0x0000, 0x0000,0x0a20, 0x16bf,0x420d, /* Dungeon Adventure */
	//	0x15,0x5d,252,-0x3e70, 0x0000,-0x3d30,-0x3ca0, 0x0100,0x4120,-0x3b9d,0x3988, /* Lords of Time */
	//	0x15,0x6c,284,-0x00f0, 0x0000,-0x0050,-0x0050,-0x0050,0x0300, 0x1930,0x3c17, /* Snowball */
	//};
    private int[][] L9V1Games =
	{
		{0x1a,0x24,301, 0x0000,-0x004b, 0x0080,-0x002b, 0x00d0,0x03b0, 0x0f80,0x4857}, /* Colossal Adventure */
		{0x20,0x3b,283,-0x0583, 0x0000,-0x0508,-0x04e0, 0x0000,0x0800, 0x1000,0x39d1}, /* Adventure Quest */
		{0x14,0xff,153,-0x00d6, 0x0000, 0x0000, 0x0000, 0x0000,0x0a20, 0x16bf,0x420d}, /* Dungeon Adventure */
		{0x15,0x5d,252,-0x3e70, 0x0000,-0x3d30,-0x3ca0, 0x0100,0x4120,-0x3b9d,0x3988}, /* Lords of Time */
		{0x15,0x6c,284,-0x00f0, 0x0000,-0x0050,-0x0050,-0x0050,0x0300, 0x1930,0x3c17}, /* Snowball */
	};
    private int L9V1Games_dictVal1=0;
    private int L9V1Games_dictVal2=1;
    private int L9V1Games_dictStart=2;
    private int L9V1Games_L9Ptrs=3;//,4,5,6,7;
    private int L9V1Games_absData=8;
    private int L9V1Games_msgStart=9;
    private int L9V1Games_msgLen=10;

    private int L9V1Game = -1;

//vars added by tsap
    private int amessageV2_depth=0;
    private int amessageV25_depth=0;
    private int displaywordref_mdtmode=0;

    private int cfvar,cfvar2; //for CODEFOLLOW
    private String[] CODEFOLLOW_codes = {
	"Goto",
	"intgosub",
	"intreturn",
	"printnumber",
	"messagev",
	"messagec",
	"function",
	"input",
	"varcon",
	"varvar",
	"_add",
	"_sub",
	"ilins",
	"ilins",
	"jump",
	"Exit",
	"ifeqvt",
	"ifnevt",
	"ifltvt",
	"ifgtvt",
	"screen",
	"cleartg",
	"picture",
	"getnextobject",
	"ifeqct",
	"ifnect",
	"ifltct",
	"ifgtct",
	"printinput",
	"ilins",
	"ilins",
	"ilins",
	};
    private String[] CODEFOLLOW_functions = {
		"calldriver",
		"L9Random",
		"save",
		"restore",
		"clearworkspace",
		"clearstack"
	};
    private String[] CODEFOLLOW_drivercalls= {
	"init",
	"drivercalcchecksum",
	"driveroswrch",
	"driverosrdch",
	"driverinputline",
	"driversavefile",
	"driverloadfile",
	"settext",
	"resettask",
	"returntogem",
	"10 *",
	"loadgamedatafile",
	"randomnumber",
	"13 *",
	"driver14",
	"15 *",
	"driverclg",
	"line",
	"fill",
	"driverchgcol",
	"20 *",
	"21 *",
	"ramsave",
	"ramload",
	"24 *",
	"lensdisplay",
	"26 *",
	"27 *",
	"28 *",
	"29 *",
	"allocspace",
	"31 *",
	"showbitmap",
	"33 *",
	"checkfordisc"
	};
	
	public L9() {
		l9bitmap=new L9Bitmap();
		workspace=new GameState();
		unpackbuf=new int[8];
		L9Pointers=new int[12];
		threechars=new byte[34];
		obuff=new char[34];
		gnoscratch=new short[32];
		gnostack=new int [128];
		InputString=null;
		ramsavearea=new SaveStruct[RAMSAVESLOTS];
		for (int i=0;i<RAMSAVESLOTS;i++) {
			ramsavearea[i]=new SaveStruct();
		}
		GfxScaleStack=new int [GFXSTACKSIZE];
		GfxA5Stack=new int [GFXSTACKSIZE];
		
		FirstLine=new char [FIRSTLINESIZE];
	}

	private void initdict(int ptr) {
		dictptr=ptr;
		unpackcount=8;
	}

	//unpack from this form: 00000111 11222223 33334444 45555566 66677777
	private int getdictionarycode() {
		if (unpackcount!=8) return unpackbuf[unpackcount++];
		else {
			// unpackbytes 
			int d1=l9memory[dictptr++]&0xff,d2;
			unpackbuf[0]=(d1>>3);
			d2=l9memory[dictptr++]&0xff;
			unpackbuf[1]=(((d2>>6) + (d1<<2)) & 0x1f);
			d1=l9memory[dictptr++]&0xff;
			unpackbuf[2]=((d2>>1) & 0x1f);
			unpackbuf[3]=(((d1>>4) + (d2<<4)) & 0x1f);
			d2=l9memory[dictptr++]&0xff;
			unpackbuf[4]=(((d1<<1) + (d2>>7)) & 0x1f);
			d1=l9memory[dictptr++]&0xff;
			unpackbuf[5]=((d2>>2) & 0x1f);
			unpackbuf[6]=(((d2<<3) + (d1>>5)) & 0x1f);
			unpackbuf[7]=(d1 & 0x1f);
			unpackcount=1;
			return unpackbuf[0];
		}
	}

	private int getdictionary(int d0) {
		if (d0>=0x1a) return getlongcode();
		else return d0+0x61;
	}

	private int getlongcode() {
		int d0,d1;
		d0=getdictionarycode();
		if (d0==0x10)
		{
			wordcase=1;
			d0=getdictionarycode();
			return getdictionary(d0); // reentrant?
		}
		d1=getdictionarycode();
		return 0x80 | ((d0<<5) & 0xe0) | (d1 & 0x1f);
	}

	private void printchar(char c) {
		if (Cheating) return;

		//if <128, Upper case after ".", "!", "?"
		if ((c&128)!=0) lastchar=(c&=0x7f);
		else if (c!=0x20 && c!=0x0d && (c<'\"' || c>='.'))
		{
			if (lastchar=='!' || lastchar=='?' || lastchar=='.') c= toUpper(c);
			lastchar=c;
		}
		// eat multiple CRs
		if (c!=0x0d || lastactualchar!=0x0d)
		{
			os_printchar(c);
			if (FirstLinePos < FIRSTLINESIZE-1)
				FirstLine[FirstLinePos++]= toLower(c);
		}
		lastactualchar=c;
	}

	private void printstring(String str) {
		for (int i=0;i<str.length();i++) printchar(str.charAt(i));
	}

    private void printstringb(int ptr) {
		char c;
		while((c=(char)(l9memory[ptr++]&0xff))!=0) {
			printchar(c);
		}
	}

    private void printdecimald0(int d0) {
		printstring(String.valueOf(d0));
	}

    //TODO: error*3 - Изврат
    private void error(String txt) {
		for (int i=0;i<txt.length();i++) os_printchar(txt.charAt(i));
	}

    private void error(String txt1, String txt2) {
		String str=String.format(txt1, txt2);
		for (int i=0;i<str.length();i++) os_printchar(str.charAt(i));
	}

    private void error(String txt, int val) {
		String str=String.format(txt, val);
		for (int i=0;i<str.length();i++) os_printchar(str.charAt(i));
	}

    private void error(String txt, int val1, int val2) {
		String str=String.format(txt, val1, val2);
		for (int i=0;i<str.length();i++) os_printchar(str.charAt(i));
	}

    private void printautocase(int d0) {
		if ((d0 & 128)!=0) printchar((char) d0);
		else {
			if (wordcase!=0) printchar(toUpper((char)d0));
			else if (d5<6) printchar((char) d0);
			else {
				wordcase=0;
				printchar(toUpper((char)d0));
			}
		}
	}

    private void displaywordref(int Off) {
		wordcase=0;
		
		//if (wordcase==0) return;
		
		d5=(Off>>12)&7;
		Off&=0xfff;
		if (Off<0xf80) {
		// dwr01 
			int a0,oPtr,a3;
			int d0,d2,i;

			if (displaywordref_mdtmode==1) printchar(' ');
			displaywordref_mdtmode=1;

			// setindex 
			a0=dictdata;
			d2=dictdatalen;

		// dwr02 
			oPtr=a0;
			while (d2!=0 && Off >= L9WORD(a0+2)) {
				a0+=4;
				d2--;
			}
		// dwr04 
			if (a0==oPtr) {
				a0=defdict;
			} else {
				a0-=4;
				Off-=L9WORD(a0+2);
				a0=startdata+L9WORD(a0);
			}
		// dwr04b
			Off++;
			initdict(a0);
			a3=0; // a3 not set in original, prevent possible spam 

			// dwr05 
			while (true) {
				d0=getdictionarycode();
				if (d0<0x1c) {
					// dwr06 
					if (d0>=0x1a) d0=getlongcode();
					else d0+=0x61;
					threechars[a3++]=(byte)(d0&0xff);
				} else {
					d0&=3;
					a3=d0;
					if (--Off==0) break;
				}
			}
			for (i=0;i<d0;i++) printautocase(threechars[i]);

			// dwr10 
			while (true) {
				d0=getdictionarycode();
				if (d0>=0x1b) return;
				printautocase(getdictionary(d0));
			}
		} else {
			if ((d5&2)!=0) printchar(' '); // prespace 
			displaywordref_mdtmode=2;
			Off&=0x7f;
			if (Off!=0x7e) printchar((char)Off);
			if ((d5&1)!=0) printchar(' '); // postspace
		}
	}

    private int getmdlength(int[] Ptr) {
		int tot=0,len;
		do {
			len=((l9memory[Ptr[0]++]&0xff) -1) & 0x3f;
			tot+=len;
		} while (len==0x3f);
		return tot;
	}

    private void printmessage(int Msg) {
		int Msgptr[]={startmd};
		int Data;

		int len;
		int Off;

		while (Msg>0 && Msgptr[0]-endmd<=0) {
			Data=l9memory[Msgptr[0]]&0xff;
			if ((Data&128)!=0) {
				Msgptr[0]++;
				Msg-=Data&0x7f;
			} else {
				len=getmdlength(Msgptr);
				Msgptr[0]+=len;
			}
			Msg--;
		}
		if (Msg<0 || ((l9memory[Msgptr[0]]&128)!=0)) return;

		len=getmdlength(Msgptr);
		if (len==0) return;

		while (len!=0) {
			Data=l9memory[Msgptr[0]++]&0xff;
			len--;
			if ((Data&128)!=0) {
			// long form (reverse word)
				Off=(Data<<8) + (l9memory[Msgptr[0]++]&0xff);
				len--;
			} else {
				Off=(l9memory[wordtable+Data*2]<<8) + (l9memory[wordtable+Data*2+1]&0xff);
			}
			if (Off==0x8f80) break;
			displaywordref(Off);
		}
	}

    /* v2 message stuff */
    private int msglenV2(int ptr) {
		//original function changes ptr sometimes, I'd replaced this functionality by 'j'
		int i=0;
		int j=0;
		int a;

		/* catch berzerking code */
		if (ptr >= startdata+datasize) return 0;

		while ((a=(l9memory[ptr+j]&0xff))==0) {
			j++;
			if (ptr+j >= startdata+datasize) return j;
			i+=255;
		}
		//i+=a;
		return i+j+a;
	}

    private void printcharV2(int c) {
		if (c==0x25) c=0xd;
		else if (c==0x5f) c=0x20;
		printautocase(c);
	}

    private void displaywordV2(int ptr,int msg) {
		int n;
		int a;
		if (msg==0) return;
		while (--msg!=0) {
			ptr+=msglenV2(ptr);
		}
		n=msglenV2(ptr);

		while (--n>0) {
			a=l9memory[++ptr]&0xff;
			if (a<3) return;

			if (a>=0x5e) displaywordV2(startmdV2-1,a-0x5d);
			else printcharV2(a+0x1d);
		}
	}

    private int msglenV1(int ptr) {
		int ptr2=ptr;
		while (ptr2<startdata+datasize && l9memory[ptr2++]!=1) ;
		return ptr2-ptr;
	}

    private void displaywordV1(int ptr,int msg) {
		int n;
		int a;
		while (msg--!=0) {
			ptr+=msglenV1(ptr);
		}
		n=msglenV1(ptr);

		while (--n>0) {
			a=l9memory[ptr++]&0xff;
			if (a<3) return;

			if (a>=0x5e) displaywordV1(startmdV2,a-0x5e);
			else printcharV2(a+0x1d);
		}
	}

    private boolean amessageV2(int ptr,int msg,int w[],int c[]) {
		int n;
		int a;
		if (msg==0) return false;
		while (--msg!=0) {
			ptr+=msglenV2(ptr);
		}
		if (ptr >= startdata+datasize) return false;
		n=msglenV2(ptr);

		while (--n>0) {
			a=l9memory[++ptr]&0xff;
			if (a<3) return true;
			if (a>=0x5e)
			{
				if (++amessageV2_depth>10 || !amessageV2(startmdV2-1,a-0x5d,w,c))
				{
					amessageV2_depth--;
					return false;
				}
				amessageV2_depth--;
			}
			else
			{
				char ch=(char)(a+0x1d);
				if (ch==0x5f || ch==' ') w[0]++;
				else c[0]++;
			}
		}
		return true;
	}

    private boolean amessageV1(int ptr,int msg,int w[],int c[]) {
		int n;
		int a;
		
		while (msg--!=0) {
			ptr+=msglenV1(ptr);
		}
		if (ptr >= startdata+datasize) return false;
		n=msglenV1(ptr);

		while (--n>0) {
			a=l9memory[ptr++]&0xff;
			if (a<3) return true;

			if (a>=0x5e) {
				if (++amessageV25_depth>10 || !amessageV1(startmdV2,a-0x5e,w,c))
				{
					amessageV25_depth--;
					return false;
				}
				amessageV25_depth--;
			} else {
				char ch=(char)(a+0x1d);
				if (ch==0x5f || ch==' ') w[0]++;
				else c[0]++;
			}
		}
		return true;
	}

    private double analyseV2() {
		int words=0,chars=0;
		int i;
		for (i=1;i<256;i++)
		{
			//long w=0,c=0;
			int w[]={0};
			int c[]={0};
			if (amessageV2(startmd,i,w,c)) {
				words+=w[0];
				chars+=c[0];
			}
			else return -1.0;
		}
		return words!=0 ? (double) chars/words : 0.0;
	}

    private double analyseV1() {
		int words=0,chars=0;
		int i;
		for (i=0;i<256;i++)
		{
			//long w=0,c=0;
			int w[]={0};
			int c[]={0};
			if (amessageV1(startmd,i,w,c))
			{
				words+=w[0];
				chars+=c[0];
			}
			else return -1.0;
		}

		return words!=0 ? (double) chars/words : 0.0;
	}

    private void printmessageV2(int Msg) {
		if (L9MsgType==MSGT_V2) displaywordV2(startmd,Msg);
		else displaywordV1(startmd,Msg);
	};

	public void FreeMemory() {
		
	}

    private boolean load(String filename) {
		byte[] filedata=os_load(filename);
		if (filedata==null) return false;
		filesize=filedata.length;
		if (filesize<256) {
			error("\rFile is too small to contain a Level 9 game\r");
			return false;
		};
		byte[] newl9memory=new byte[filesize+LISTAREASIZE];
		//we must save listarea for new file it it is no new game 
		if ((l9memory!=null) && (listarea>0) && ((listarea+LISTAREASIZE)<=l9memory.length)) {
			for (int i=0;i<LISTAREASIZE;i++) newl9memory[filesize+i]=l9memory[listarea+i];
		}
		l9memory=newl9memory;
		listarea=filesize;
		startfile=0;
		for (int i=0;i<filesize;i++) l9memory[startfile+i]=filedata[i];
		return true;
	}

    private int scanmovewa5d0(PosScanCodeMask dat) {
		int ret=L9WORD(dat.Pos);
		(dat.Pos)+=2;
		return ret;
	}

    private int scangetaddr(int Code,PosScanCodeMask dat,int acode) {
		(dat.ScanCodeMask)|=0x20;
		if ((Code&0x20)!=0)
		{
			// getaddrshort 
			byte diff=l9memory[dat.Pos];
			(dat.Pos)++;
			return (dat.Pos)+diff-1;
		}
		else
		{
			return acode+scanmovewa5d0(dat);
		}
	}

    private void scangetcon(int Code,PosScanCodeMask dat) {
		(dat.Pos)++;
		if (!((Code & 64)!=0)) (dat.Pos)++;
		(dat.ScanCodeMask)|=0x40;
	}

    private boolean CheckCallDriverV4(int Pos) {
		int i,j;
	
		// Look back for an assignment from a variable
		// to list9[0], which is used to specify the
		// driver call.
		//
		for (i = 0; i < 2; i++)
		{
			int x = Pos - ((i+1)*3);
			if (((l9memory[x]&0xff) == 0x89) && ((l9memory[x+1]&0xff) == 0x00))
			{
				// Get the variable being copied to list9[0] 
				int var = l9memory[x+2]&0xff;
	
				// Look back for an assignment to the variable. 
				for (j = 0; j < 2; j++)
				{
					int y = x - ((j+1)*3);
					if ((l9memory[y] == 0x48) && ((l9memory[y+2]&0xff) == var))
					{
						// If this a V4 driver call? 
						switch (l9memory[y+1]&0xff)
						{
						case 0x0E:
						case 0x20:
						case 0x22:
							return true;
						}
						return false;
					}
				}
			}
		}
		return false;
	}

    private boolean ValidateSequence(byte[] Image,int iPos,int acode,
                                     ScanData sdat,boolean Rts, boolean checkDriverV4) {
		boolean Finished=false,Valid;
		//int Strange=0;
		int Code;
		sdat.JumpKill=false;
		
		PosScanCodeMask pscm=new PosScanCodeMask();
	
		if (iPos>=filesize)
			return false;
		pscm.Pos=iPos;
		if (pscm.Pos<sdat.Min) sdat.Min=pscm.Pos;
	
		if (Image[pscm.Pos]!=0) return true; // hit valid code 
	
		do
		{
			Code=l9memory[pscm.Pos]&0xff;
			Valid=true;
			if (Image[pscm.Pos]!=0) break; // converged to found code 
			Image[pscm.Pos++]=2;
			if (pscm.Pos>sdat.Max) sdat.Max=pscm.Pos;
	
			pscm.ScanCodeMask=0x9f;
			if ((Code&0x80)!=0)
			{
				pscm.ScanCodeMask=0xff;
				if ((Code&0x1f)>0xa)
					Valid=false;
				pscm.Pos+=2;
			}
			else switch (Code & 0x1f)
			{
				case 0: // goto 
				{
					int Val=scangetaddr(Code,pscm,acode);
					Valid=ValidateSequence(Image,Val,acode,sdat,true,checkDriverV4);
					Finished=true;
					break;
				}
				case 1: // intgosub 
				{
					int Val=scangetaddr(Code,pscm,acode);
					Valid=ValidateSequence(Image,Val,acode,sdat,true,checkDriverV4);
					break;
				}
				case 2: // intreturn 
					Valid=Rts;
					Finished=true;
					break;
				case 3: // printnumber
					pscm.Pos++;
					break;
				case 4: // messagev 
					pscm.Pos++;
					break;
				case 5: // messagec 
					scangetcon(Code,pscm);
					break;
				case 6: // function 
					switch ((int)(l9memory[pscm.Pos++]&0xff))
					{
						case 2:// random 
							pscm.Pos++;
							break;
						case 1:// calldriver
							if (checkDriverV4) {
								if (CheckCallDriverV4(pscm.Pos-2))
									sdat.DriverV4 = true;
							}
							break;
						case 3:// save 
						case 4:// restore 
						case 5:// clearworkspace 
						case 6:// clear stack 
							break;
						case 250: // printstr 
							while (l9memory[pscm.Pos++]!=0);
							break;
	
						default:
							L9DEBUG("scan: illegal function call: %d\r",l9memory[pscm.Pos-1]);
							Valid=false;
							break;
					}
					break;
				case 7: // input 
					pscm.Pos+=4;
					break;
				case 8: // varcon 
					scangetcon(Code,pscm);
					pscm.Pos++;
					break;
				case 9: // varvar
					pscm.Pos+=2;
					break;
				case 10: // _add 
					pscm.Pos+=2;
					break;
				case 11: // _sub 
					pscm.Pos+=2;
					break;
				case 14: // jump 
					//L9DEBUG("jmp at codestart: %d",acode);
					sdat.JumpKill=true;
					Finished=true;
					break;
				case 15: // exit 
					pscm.Pos+=4;
					break;
				case 16: // ifeqvt 
				case 17: // ifnevt 
				case 18: // ifltvt 
				case 19: // ifgtvt 
				{
					int Val;
					pscm.Pos+=2;
					Val=scangetaddr(Code,pscm,acode);
					Valid=ValidateSequence(Image,Val,acode,sdat,Rts,checkDriverV4);
					break;
				}
				case 20: // screen 
					if (l9memory[pscm.Pos++]!=0) pscm.Pos++;
					break;
				case 21: // cleartg 
					pscm.Pos++;
					break;
				case 22: // picture 
					pscm.Pos++;
					break;
				case 23: // getnextobject 
					pscm.Pos+=6;
					break;
				case 24: // ifeqct 
				case 25: // ifnect 
				case 26: // ifltct 
				case 27: // ifgtct 
				{
					int Val;
					pscm.Pos++;
					scangetcon(Code,pscm);
					Val=scangetaddr(Code,pscm,acode);
					Valid=ValidateSequence(Image,Val,acode,sdat,Rts,checkDriverV4);
					break;
				}
				case 28: // printinput 
					break;
				case 12: // ilins 
				case 13: // ilins 
				case 29: // ilins 
				case 30: // ilins 
				case 31: // ilins 
					L9DEBUG("scan: illegal instruction\r");
					Valid=false;
					break;
			}
			if (Valid && ((Code & ~pscm.ScanCodeMask)!=0)) {
				//Strange++;
			};
		} while (Valid && !Finished && pscm.Pos<filesize); // && Strange==0); 
		(sdat.Size)+=pscm.Pos-iPos;
		return Valid; // && Strange==0; 
	}

    private int Scan() {
		
		byte[] Chk = new byte[filesize+1];
		byte[] Image = new byte[filesize];
		int i,num,MaxSize=0;
		int j;
		int d0=0,l9,md,ml,dd,dl;
		int Offset=-1;
		
		ScanData scandata=new ScanData();
		
		//if ((Chk==NULL)||(Image==NULL))
		//{
		//	fprintf(stderr,"Unable to allocate memory for game scan! Exiting...\n");
		//	exit(0);
		//}
	
		Chk[0]=0;
		for (i=1;i<=filesize;i++)
			//Chk[i]=Chk[i-1]+StartFile[i-1];
			Chk[i]=(byte)(((Chk[i-1]&0xff)+(l9memory[startfile+i-1]&0xff))&0xff);
	
		for (i=0;i<filesize-33-1;i++)
		{
			num=L9WORD(i)+1;
	
			//Chk[i] = 0 +...+ i-1
			//Chk[i+n] = 0 +...+ i+n-1
			//Chk[i+n] - Chk[i] = i + ... + i+n
	
			if (num>0x2000 && i+num<=filesize && Chk[i+num]==Chk[i]) {
				md=L9WORD(i+0x2);
				ml=L9WORD(i+0x4);
				dd=L9WORD(i+0xa);
				dl=L9WORD(i+0xc);
	
				if (ml>0 && md>0 && i+md+ml<=filesize && dd>0 && dl>0 && i+dd+dl*4<=filesize) {
					// v4 files may have acodeptr in 8000-9000, need to fix 
					for (j=0;j<12;j++) {
						d0=L9WORD (i+0x12 + j*2);
						if (j!=11 && d0>=0x8000 && d0<0x9000) {
							if (d0>=0x8000+LISTAREASIZE) break;
						} else if (i+d0>filesize) break;
					}
					// list9 ptr must be in listarea, acode ptr in data 
					//if (j<12 || (d0>=0x8000 && d0<0x9000)) continue;
					//if (j<12) continue;
	
					l9=L9WORD(i+0x12 + 10*2);
					if (l9<0x8000 || l9>=0x8000+LISTAREASIZE) continue;
	
					scandata.Size=0;
					scandata.Min=scandata.Max=i+d0;
					scandata.DriverV4=false;
					if (ValidateSequence(Image,i+d0,i+d0,scandata,false,true)) {
						L9DEBUG("Found valid header at %d, code size %d\r",i,scandata.Size);
						if ((scandata.Size>MaxSize) && (scandata.Size>100)) {
							Offset=i;
							MaxSize=scandata.Size;
							l9GameType = scandata.DriverV4? L9GameType.L9_V4: L9GameType.L9_V3;
						}
					}
				}
			}
		}
		return Offset;
	}

    private int ScanV2() {
		byte[] Chk = new byte[filesize+1];
		byte[] Image = new byte[filesize];
		
		int i,MaxSize=0,num;
		int j;
		int d0=0,l9;
		//int Min,Max,Size;
		//boolean JumpKill;
		int Offset=-1;
	
		ScanData scandata=new ScanData();
		
		/*
		if ((Chk==NULL)||(Image==NULL))
		{
			fprintf(stderr,"Unable to allocate memory for game scan! Exiting...\n");
			exit(0);
		}*/
	
		Chk[0]=0;
		for (i=1;i<=filesize;i++)
			Chk[i]=(byte)(((Chk[i-1]&0xff)+(l9memory[i-1]&0xff))&0xff);
	
		//BUGFIXbyTSAP, possible out of array on L9WORD - Filesize-28+28=Filesize
		for (i=0;i<filesize-28-1;i++) {
			num=L9WORD(i+28)+1;
			if (i+num<=filesize && (((Chk[i+num]&0xff)-(Chk[i+32]&0xff))&0xff)==(l9memory[i+0x1e]&0xff)) {
				for (j=0;j<14;j++) {
					 d0=L9WORD (i+ j*2);
					 if (j!=13 && d0>=0x8000 && d0<0x9000) {
						if (d0>=0x8000+LISTAREASIZE) break;
					 } else if (i+d0>filesize) break;
				}
				// list9 ptr must be in listarea, acode ptr in data 
				//if (j<14 || (d0>=0x8000 && d0<0x9000)) continue;
				if (j<14) continue;
	
				l9=L9WORD(i+6 + 9*2);
				if (l9<0x8000 || l9>=0x8000+LISTAREASIZE) continue;
	
				scandata.Size=0;
				scandata.Min=scandata.Max=i+d0;
				if (ValidateSequence(Image,i+d0,i+d0,scandata,false,false)) {
					L9DEBUG("Found valid V2 header at %d, code size %d\r",i,scandata.Size);
					if ((scandata.Size>MaxSize)  && (scandata.Size>100)) {
						Offset=i;
						MaxSize=scandata.Size;
					}
				}
			}
		}
		return Offset;
	}

    private int ScanV1() {
		byte[] Image = new byte[filesize];
		int ImagePtr;
		int i;
		byte Replace;
		int MaxPos=-1;
		int MaxCount=0;
		ScanData scandata=new ScanData();
		int MaxMin,MaxMax;
		boolean MaxJK;
		
		int dictOff1=0;
		int dictOff2=0;
		int dictVal1 = 0xff, dictVal2 = 0xff;
	
		//TODO: есть ли шанс, что массив Image не будет создан?
		//if (Image==NULL)
		//{
		//	fprintf(stderr,"Unable to allocate memory for game scan! Exiting...\n");
		//	exit(0);
		//}
	
		for (i=0;i<filesize;i++) {
			if (((l9memory[startfile+i]==0) && (l9memory[startfile+i+1]==6)) || ((l9memory[startfile+i]==32) && (l9memory[startfile+i+1]==4))) {
				scandata.Size=0;
				scandata.Min=scandata.Max=i;
				scandata.DriverV4=false;
				Replace=0;
				if (ValidateSequence(Image,i,i,scandata, false,false)) {
					if ((scandata.Size>MaxCount) && (scandata.Size>100) && (scandata.Size<10000)) {
						MaxCount=scandata.Size;
						MaxMin=scandata.Min;
						MaxMax=scandata.Max;
	
						MaxPos=i;
						MaxJK=scandata.JumpKill;
					}
					Replace=0;
				}
	
				for (ImagePtr=scandata.Min;ImagePtr<=scandata.Max;ImagePtr++) {
					if (Image[ImagePtr]==2)
						Image[ImagePtr]=Replace;
				}
			}
		}
		L9DEBUG("V1scan found code at %d size %d",MaxPos,MaxCount);
	
		// V1 dictionary detection from L9Cut by Paul David Doherty 
		for (i=0;i<filesize-20;i++) {
			if (l9memory[startfile+i]=='A') {
				if ((l9memory[startfile+i+1]=='T') && (l9memory[startfile+i+2]=='T') && (l9memory[startfile+i+3]=='A') && (l9memory[startfile+i+4]=='C') && ((l9memory[startfile+i+5]&0xff)==0xcb)) {
					dictOff1 = i;
					dictVal1 = l9memory[startfile+dictOff1+6]&0xff;
					break;
				}
			}
		}
		for (i=dictOff1;i<filesize-20;i++) {
			if (l9memory[startfile+i]=='B') {
				if ((l9memory[startfile+i+1]=='U') && (l9memory[startfile+i+2]=='N') && (l9memory[startfile+i+3]=='C') && ((l9memory[startfile+i+4]&0xff) == 0xc8)) {
					dictOff2 = i;
					dictVal2 = l9memory[startfile+dictOff2+5]&0xff;
					break;
				}
			}
		}
		L9V1Game = -1;
		if ((dictVal1 != 0xff) || (dictVal2 != 0xff)) {
			for (i = 0; i < L9V1Games.length; i++) {
				if ((L9V1Games[i][L9V1Games_dictVal1] == dictVal1) && (L9V1Games[i][L9V1Games_dictVal2] == dictVal2)) {
					L9V1Game = i;
					dictdata = startfile+dictOff1-L9V1Games[i][L9V1Games_dictStart];
				}
			}
		}
	
		if (L9V1Game >= 0)
			L9DEBUG ("V1scan found known dictionary: %d",L9V1Game);
	
		if (MaxPos>0) {
			acodeptr=startfile+MaxPos;
			return 0;
		}
		return -1;
	}
	
	//TODO: Нужна ли реализация FullScan?
	/*--was--	#ifdef FULLSCAN
	void FullScan(L9BYTE* StartFile,L9UINT32 FileSize)
	{
		L9BYTE *Image=calloc(FileSize,1);
		L9UINT32 i,Size;
		int Replace;
		L9BYTE* ImagePtr;
		L9UINT32 MaxPos=0;
		L9UINT32 MaxCount=0;
		L9UINT32 Min,Max,MaxMin,MaxMax;
		int Offset;
		L9BOOL JumpKill,MaxJK;
		for (i=0;i<FileSize;i++)
		{
			Size=0;
			Min=Max=i;
			Replace=0;
			if (ValidateSequence(StartFile,Image,i,i,&Size,FileSize,&Min,&Max,FALSE,&JumpKill,NULL))
			{
				if (Size>MaxCount)
				{
					MaxCount=Size;
					MaxMin=Min;
					MaxMax=Max;
	
					MaxPos=i;
					MaxJK=JumpKill;
				}
				Replace=0;
			}
			for (ImagePtr=Image+Min;ImagePtr<=Image+Max;ImagePtr++)
			{
				if (*ImagePtr==2)
					*ImagePtr=Replace;
			}
		}
		printf("%ld %ld %ld %ld %s",MaxPos,MaxCount,MaxMin,MaxMax,MaxJK ? "jmp killed" : "");
		// search for reference to MaxPos 
		Offset=0x12 + 11*2;
		for (i=0;i<FileSize-Offset-1;i++)
		{
			if ((L9WORD(StartFile+i+Offset)) +i==MaxPos)
			{
				printf("possible v3,4 Code reference at : %ld",i);
				// startdata=StartFile+i; 
			}
		}
		Offset=13*2;
		for (i=0;i<FileSize-Offset-1;i++)
		{
			if ((L9WORD(StartFile+i+Offset)) +i==MaxPos)
				printf("possible v2 Code reference at : %ld",i);
		}
		free(Image);
	}
	#endif
	*/

    private boolean findsubs(int testptr, int testsize, int picdata[], int picsize[]) {
		int i, j, length, count;
		int picptr, startptr, tmpptr;

		if (testsize < 16) return false;
		
		//
		//	Try to traverse the graphics subroutines.
		//	
		//	Each subroutine starts with a header: nn | nl | ll
		//	nnn : the subroutine number ( 0x000 - 0x7ff ) 
		//	lll : the subroutine length ( 0x004 - 0x3ff )
		//	
		//	The first subroutine usually has the number 0x000.
		//	Each subroutine ends with 0xff.
		//	
		//	findsubs() searches for the header of the second subroutine
		//	(pattern: 0xff | nn | nl | ll) and then tries to find the
		//	first and next subroutines by evaluating the length fields
		//	of the subroutine headers.
		//
		for (i = 4; i < (int)(testsize - 4); i++) {
			picptr = testptr + i;
			if (	((l9memory[picptr - 1]&0xff)    != 0xff) ||
					((l9memory[picptr] & 0x80) !=0) ||
					((l9memory[picptr + 1] & 0x0c)  !=0) ||
					((l9memory[picptr + 2]&0xff)    < 4) 
				)
				continue;

			count = 0;
			startptr = picptr;
			tmpptr=picptr;

			while (true) {
				length = ((l9memory[picptr + 1] & 0x0f) << 8) + (l9memory[picptr + 2]&0xff);
				if ((length > 0x3ff) || (picptr + length + 4 > testptr + testsize))
					break;
				
				picptr += length;
				if ((l9memory[picptr - 1]&0xff) != 0xff) {
					picptr -= length;
					break;
				}
				if (((l9memory[picptr] & 0x80)!=0) || ((l9memory[picptr + 1] & 0x0c)!=0) || ((l9memory[picptr + 2]&0xff) < 4))
					break;
				
				count++;
			}

			if (count > 10) {
				/* Search for the start of the first subroutine */
				for (j = 4; j < 0x3ff; j++) {
					tmpptr = startptr - j;				
					if ((l9memory[tmpptr]&0xff) == 0xff || tmpptr < testptr)
						break;
						
					length = ((l9memory[tmpptr + 1] & 0x0f) << 8) + l9memory[tmpptr + 2];
					if (tmpptr + length == startptr) {
						startptr = tmpptr;					
						break;
					}
				}
				
				if ((l9memory[tmpptr]&0xff) != 0xff) {
					picdata[0] = startptr;
					picsize[0] = picptr - startptr;
					return true;
				}		
			}
		}
		return false;
	}

    private boolean intinitialise(String filename, String picname) {
	// init 
	// driverclg 
		
		int i;
		int hdoffset;
		int Offset;

		pictureaddress=-1;
		picturedata=-1;
		picturesize=0;
		gfxa5[0]=-1;
		
		if (!load(filename)) {
			error("\rUnable to load: %s\r",filename);
			return false;
		}
		
		L9DEBUG("Loaded ok, size=%d\r",filesize);

        //TODO: проверить наличие игр с несколькими игровыми файлами данных и линейной графикой в отдельном файле,
        //		такие игры будут отображать картинку только на первом загруженном дата-файле, второй и следующие убъют картинки
        if (picname!=null) {
			byte picbuff[]= os_load(picname);
			if (picbuff!=null) {
				//TODO: tempbuff - lame! kill it slowly!
				byte tempbuff[]=new byte[l9memory.length+picbuff.length];
				pictureaddress=l9memory.length;
				picturesize=picbuff.length;
				for (i=0; i<pictureaddress;i++) tempbuff[i]=l9memory[i];
				for (i=0; i<picturesize;i++) tempbuff[pictureaddress+i]=picbuff[i];
				l9memory=tempbuff;
			};
		}

		screencalled=0;
		l9textmode=0;
/*
	#ifdef FULLSCAN
		FullScan(startfile,FileSize);
	#endif

		*/
		Offset=Scan();
		if (Offset<0) {
			Offset=ScanV2();
			l9GameType = L9GameType.L9_V2;
			if (Offset<0) {
				Offset=ScanV1();
				l9GameType = L9GameType.L9_V1;
				if (Offset<0) {
					error("\rUnable to locate valid Level 9 game in file: %s\r",filename);
				 	return false;
				}
			}
		}

		startdata=startfile+Offset;
		datasize=filesize-Offset;

	// setup pointers 
		if (l9GameType == L9GameType.L9_V1) {
			if (L9V1Game < 0) {
				error("\rWhat appears to be V1 game data was found, but the game was not recognised.\rEither this is an unknown V1 game file or, more likely, it is corrupted.\r");
				return false;
			}
			for (i=0;i<5;i++) { //TODO: в оригинале "i<6" но в массиве только 5 значений, поправил, нужно убедиться в правильности (v1_time.sna запускается работает)
				int off=L9V1Games[L9V1Game][L9V1Games_L9Ptrs+i];
				if (off<0)
					L9Pointers[i+2]=acodeptr+off;
				else
					L9Pointers[i+2]=listarea+off;
			}
			absdatablock=acodeptr-L9V1Games[L9V1Game][L9V1Games_absData];
		} else {
			// V2,V3,V4 

			hdoffset= l9GameType == L9GameType.L9_V2 ? 4 : 0x12;

			for (i=0;i<12;i++) {
				int d0=L9WORD(startdata+hdoffset+i*2);
				L9Pointers[i]= (i!=11 && d0>=0x8000 && d0<=0x9000) ? listarea+d0-0x8000 : startdata+d0;
			}
			absdatablock=L9Pointers[0];
			dictdata=L9Pointers[1];
			list2ptr=L9Pointers[3];
			list3ptr=L9Pointers[4];
			list9startptr=L9Pointers[10];
			acodeptr=L9Pointers[11];
		}

		switch (l9GameType) {
			case L9_V1: {
				double a1;
				startmd=acodeptr+L9V1Games[L9V1Game][L9V1Games_msgStart];
				startmdV2=startmd+L9V1Games[L9V1Game][L9V1Games_msgLen];

				a1=analyseV1();
				if (a1>0.0 && a1>2 && a1<10) {
					L9MsgType=MSGT_V1;
					L9DEBUG("V1 msg table: wordlen=%d/10\r",(int)(a1*10));
				} else {
					error("\rUnable to identify V1 message table in file: %s\r",filename);
					return false;
				}
				break;
			}
			case L9_V2: {
				double a2,a1;
				startmd=startdata + L9WORD(startdata+0x0);
				startmdV2=startdata + L9WORD(startdata+0x2);

				// determine message type
				a2=analyseV2();
				if (a2>0.0 && a2>2 && a2<10) {
					L9MsgType=MSGT_V2;
					L9DEBUG("V2 msg table: wordlen=%d/10\r",(int)(a2*10));
				} else {
					a1=analyseV1();
					if (a1>0 && a1>2 && a1<10) {
						L9MsgType=MSGT_V1;
						L9DEBUG("V1 msg table: wordlen=%d/10\r",(int)(a1*10));
					} else {
						error("\rUnable to identify V2 message table in file: %s\r",filename);
						return false;
					}
				};
				break;
			}
			case L9_V3:
			case L9_V4:
				startmd=startdata + L9WORD(startdata+0x2);
				endmd=startmd + L9WORD(startdata+0x4);
				defdict=startdata+L9WORD(startdata+6);
				endwdp5=defdict + 5 + L9WORD(startdata+0x8);
				dictdata=startdata+L9WORD(startdata+0x0a);
				dictdatalen=L9WORD(startdata+0x0c);
				wordtable=startdata + L9WORD(startdata+0xe);
				break;
		};
		
		int[] pdata = {-1};
		int[] psize = {0};

		if (pictureaddress>=0) {
			if (!findsubs(pictureaddress, picturesize, pdata, psize)) {
				//picturedata = -1;
				//picturesize = 0;
			}
		} else {
			if (!findsubs(startdata, datasize, pdata, psize)
				&& !findsubs(startfile, startdata - startfile, pdata, psize))
			{
				//picturedata = -1;
				//picturesize = 0;
			}
		}
		
		picturedata=pdata[0];
		picturesize=psize[0];
		
		for (i=0;i<FIRSTLINESIZE;i++) FirstLine[i]=0;
		FirstLinePos=0;
		
		return true;
	}
	
	/*	L9BOOL checksumgamedata(void)
	{
		return calcchecksum(startdata,L9WORD(startdata)+1)==0;
	}*/

    private int movewa5d0() {
		int ret = L9WORD(codeptr);
		codeptr += 2;
		return ret;
	}

    private int getcon() {
		if ((code & 64)!=0) {
			return l9memory[codeptr++]&0xff;
		} else return movewa5d0();
	}

    private int getaddr() {
		if ((code&0x20)!=0) {
			//diff-signed!
			byte diff=l9memory[codeptr++];
			return codeptr+ diff-1;
		} else {
			return acodeptr+movewa5d0();
		}
	}

    private int getvar() {
		cfvar2=cfvar;
		return cfvar=l9memory[codeptr++]&0xff;
	}

    private void Goto() {
		int target=getaddr();
		if (target==codeptr-2)
			StopGame(); //Endless loop!
		else
			codeptr=target;
	}

    private void intgosub() {
		int newcodeptr=getaddr();
		if (workspace.stackptr==STACKSIZE) {
			error("\rStack overflow error\r");
			//Running=false;
			L9State=L9StateStopped;
			return;
		}
		workspace.stack[workspace.stackptr++]=(short)((codeptr-acodeptr)&0xffff);
		codeptr=newcodeptr;
	}

    private void intreturn() {
		if (workspace.stackptr==0) {
			error("\rStack underflow error\r");
			L9State=L9StateStopped;
			return;
		}
		codeptr=acodeptr+workspace.stack[--workspace.stackptr];
	}

    private void printnumber() {
		printdecimald0(workspace.vartable[getvar()]);
	}

    private void messagec() {
		if (l9GameType == L9GameType.L9_V1 || l9GameType == L9GameType.L9_V2)
			printmessageV2(getcon());
		else
			printmessage(getcon());
	}

    private void messagev() {
		if (l9GameType == L9GameType.L9_V1 || l9GameType == L9GameType.L9_V2)
			printmessageV2(workspace.vartable[getvar()]&0xffff);
		else
			printmessage(workspace.vartable[getvar()]&0xffff);
	}

    private void init(int a6) 				{L9DEBUG("driver - init"); }
    private void randomnumber(int a6) {
		L9DEBUG("driver - randomnumber");
		//TODO: L9SETWORD(a6,rand());
	}
    private void driverclg(int a6)			{L9DEBUG("driver - driverclg"); }
    private void _line(int a6)				{L9DEBUG("driver - line"); }
    private void fill(int a6)				{L9DEBUG("driver - fill"); }
    private void driverchgcol(int a6)		{L9DEBUG("driver - driverchgcol");}
    private void drivercalcchecksum(int a6) {L9DEBUG("driver - calcchecksum");}
    private void driveroswrch(int a6)		{L9DEBUG("driver - driveroswrch");}
    private void driverosrdch(int a6)	{
		L9DEBUG("driver - driverosrdch");
		os_flush();
		if (Cheating) {
			l9memory[a6] = '\r';
		} else {
            //TODO в зависимости от статуса машины либо вернуться и ожидать нажатия символа, либо идти дальше как будто символ введен
			//TODO: если вернуться для ожидания символа - вернуть состояние машины как будто
            // команду не начали выполнять, кажется нужно только уменьшить ptr
            /* max delay of 1/50 sec */
			l9memory[a6]=(byte)os_readchar(20);
		}
	}
    private void driversavefile(int a6)		{L9DEBUG("driver - driversavefile");}
    private void driverloadfile(int a6)		{L9DEBUG("driver - driverloadfile");}
    private void settext(int a6)			{L9DEBUG("driver - settext");}
    private void resettask(int a6)			{L9DEBUG("driver - resettask");}
    private void driverinputline(int a6)	{L9DEBUG("driver - driverinputline");}
    private void returntogem(int a6)		{L9DEBUG("driver - returntogem");}
    private void lensdisplay(int a6) {
		L9DEBUG("driver - lensdisplay");
		printstring("\rLenslok code is ");
		printchar((char)(l9memory[a6]));
		printchar((char)(l9memory[a6+1]));
		printchar('\r');
	}
    private void allocspace(int a6)			{L9DEBUG("driver - allocspace");}
    private void driver14(int a6) {
		L9DEBUG ("driver - call 14");
		l9memory[a6] = 0;
	}
    private void showbitmap(int a6) {
		L9DEBUG("driver - showbitmap");
		os_show_bitmap(l9memory[a6+1],l9memory[a6+3],l9memory[a6+5]);
	}
    private void checkfordisc(int a6) {
		L9DEBUG("driver - checkfordisc");
		l9memory[a6] = 0;
		l9memory[list9startptr+2] = 0;
	}

    private void driver(int d0,int a6) {
		switch (d0) {
			case 0: init(a6); break;
			case 0x0c: randomnumber(a6); break;
			case 0x10: driverclg(a6); break;
			case 0x11: _line(a6); break;
			case 0x12: fill(a6); break;
			case 0x13: driverchgcol(a6); break;
			case 0x01: drivercalcchecksum(a6); break;
			case 0x02: driveroswrch(a6); break;
			case 0x03: driverosrdch(a6); break; //os_readchar
			case 0x05: driversavefile(a6); break;
			case 0x06: driverloadfile(a6); break;
			case 0x07: settext(a6); break;
			case 0x08: resettask(a6); break;
			case 0x04: driverinputline(a6); break;
			case 0x09: returntogem(a6); break;
			case 0x19: lensdisplay(a6); break;
			case 0x1e: allocspace(a6); break;
	/* v4 */
			case 0x0e: driver14(a6); break;
			case 0x20: showbitmap(a6); break;
			case 0x22: checkfordisc(a6); break;
		}
	}

    private void ramsave(int i) {
		L9DEBUG("driver - ramsave %d",i);
		//memmove(ramsavearea+i,workspace.vartable,sizeof(SaveStruct));
		int j;
		int s=ramsavearea[i].listarea.length;
		for (j=0;j<s;j++) 
			ramsavearea[i].listarea[j]=l9memory[listarea+j];
		s=ramsavearea[i].vartable.length;
		for (j=0;j<s;j++)
			ramsavearea[i].vartable[j]=workspace.vartable[j];		
	}

    private void ramload(int i) {
		L9DEBUG("driver - ramload %d",i);
		//memmove(workspace.vartable,ramsavearea+i,sizeof(SaveStruct));
		int j;
		for (j=0;j<ramsavearea[i].listarea.length;j++) 
			l9memory[listarea+j]=ramsavearea[i].listarea[j];
		for (j=0;j<ramsavearea[i].vartable.length;j++)
			workspace.vartable[j]=ramsavearea[i].vartable[j];		
	}

    private void calldriver() {
		int a6=list9startptr;
		int d0=l9memory[a6++]&0xff;

		if (d0==0x16 || d0==0x17) {
			int d1=l9memory[a6]&0xff;
			if (d1>0xfa) l9memory[a6]=1;
			else if (d1+1>=RAMSAVESLOTS) l9memory[a6]=(byte)0xff;
			else {
				l9memory[a6]=0;
				if (d0==0x16) ramsave(d1+1); else ramload(d1+1);
			}
			l9memory[list9startptr]=l9memory[a6];
		} else if (d0==0x0b) {
			String NewName=new String(LastGame);
			if (l9memory[a6]==0) {
				printstring("\rSearching for next sub-game file.\r");
				NewName=os_get_game_file(NewName);
				if (NewName==null) {
					printstring("\rFailed to load game.\r");
					return;
				}
			} else {
				NewName=os_set_filenumber(NewName,l9memory[a6]);
			}
			LoadGame2(NewName,null);
		} else driver(d0,a6);
	}

    private void L9Random() {
		CODEFOLLOW (" %d",randomseed&0xffff);
		randomseed=(short)((((randomseed<<8) + 0x0a - randomseed) <<2) + randomseed + 1);
		workspace.vartable[getvar()]=(short)(randomseed & 0xff);
		CODEFOLLOW (" %d",randomseed&0xffff);
	}

    private void save() {
		L9DEBUG("function - save");
	// does a full save, workpace, stack, codeptr, stackptr, game name, checksum 

		workspace.codeptr=(short)((codeptr-acodeptr)&0xffff);
		workspace.listsize=LISTAREASIZE;
		workspace.stacksize=STACKSIZE;
		workspace.filename=LastGame;
		byte buff[]=workspace.getCloneInBytes(l9memory, listarea);
		if (os_save_file(buff)) printstring("\rGame saved.\r");
		else printstring("\rUnable to save game.\r");
	};

	/*	L9BOOL CheckFile(GameState *gs)
	{
		L9UINT16 checksum;
		int i;
		char c = 'Y';

		if (gs->Id!=L9_ID) return FALSE;
		checksum=gs->checksum;
		gs->checksum=0;
		for (i=0;i<sizeof(GameState);i++) checksum-=*((L9BYTE*) gs+i);
		if (checksum) return FALSE;
		if (stricmp(gs->filename,LastGame))
		{
			printstring("\rWarning: game path name does not match, you may be about to load this position file into the wrong story file.\r");
			printstring("Are you sure you want to restore? (Y/N)");
			os_flush();

			c = '\0';
			while ((c != 'y') && (c != 'Y') && (c != 'n') && (c != 'N')) 
				c = os_readchar(20);
		}
		if ((c == 'y') || (c == 'Y'))
			return TRUE;
		return FALSE;
	}*/

    private void NormalRestore() {
		L9DEBUG("function - restore");
		if (Cheating) {
			// not really an error
			Cheating=false;
			error("\rWord is: %s\r",ibuffstr);
		} else restore();
	}

    private void restore() {
		byte[] buff=os_load_file();
		GameState tempGS=new GameState();
		if (buff!=null) {
			if (tempGS.setFromCloneInBytes(buff, l9memory, listarea)) {
				printstring("\rGame restored.\r");
				if (!LastGame.equalsIgnoreCase(tempGS.filename)) {	
					String newFileName=LastGame.substring(0, findBeginFilename(LastGame))+(tempGS.filename.substring(findBeginFilename(tempGS.filename)).toLowerCase());
					int ret = LoadGame2(newFileName,null);
					if (ret!=L9StateStopped) {
						printstring("\rGamefile changed according to saved game state.\r");
						tempGS.setFromCloneInBytes(buff, l9memory, listarea);
					}
					else printstring("\rSorry, correct game file not found.\r");
				};
				workspace=tempGS.clone();
				codeptr=acodeptr+workspace.codeptr;
			} else printstring("\rSorry, unrecognised format. Unable to restore\r");
		} else printstring("\rUnable to restore game.\r");
	}

    private int findBeginFilename(String path) {
		int begin_filename=path.length()-1;
		char c;
		while (begin_filename>0) {
			c=path.charAt(begin_filename-1);
			if (c=='\\' || c=='/') break;
			begin_filename--;
		};
		return begin_filename;
	};

    private void playback() {
		if (scriptArray!=null) scriptArray=null;
		scriptArray = os_open_script_file();
		scriptArrayIndex=0;
		if (scriptArray!=null)
			printstring("\rPlaying back input from script file.\r\r");
		else
			printstring("\rUnable to play back script file.\r\r");
	}

    private void l9_fgets(char [] s, int si,int n) {
		char c = '\0';
		char c_eof=(char)(-1);
		int count = 0;

		while ((c != '\n') && (c != '\r') && (c != c_eof) && (count < n-1)) {
			if (scriptArrayIndex<scriptArray.length) c=(char) (scriptArray[scriptArrayIndex++]&0xff);
			else c=c_eof;
			s[si++] = c;
			count++;
		}
		s[si] = '\0';

		if (c == c_eof) {
			si--;
			s[si] = '\n';
		} else if (c == '\r') {
			si--;
			s[si] = '\n';

			if (scriptArrayIndex<scriptArray.length) c=(char) (scriptArray[scriptArrayIndex++]&0xff);
			else c=c_eof;
			if ((c != '\r') && (c != c_eof))
				scriptArrayIndex--;
		}
	}

    private boolean scriptinput(char[] ibuff, int size) {
		int ibuffIndex;
		while (scriptArray != null) {
			if (scriptArray.length<=scriptArrayIndex)
				scriptArray=null;
			else {
				ibuffIndex=0;
				ibuff[ibuffIndex]='\0';
				l9_fgets(ibuff,ibuffIndex,size);
				while (ibuff[ibuffIndex] != '\0') {
					switch (ibuff[ibuffIndex]) {
                        case '\n':
                        case '\r':
                        case '[':
                        case ';':
                            ibuff[ibuffIndex] = '\0';
                            break;
                        case '#':
                            if ((ibuffIndex==0) && (stricmp(ibuff,"#seed ",6)))
                                ibuffIndex++;
                            else
                                ibuff[ibuffIndex] = '\0';
                            break;
                        default:
                            ibuffIndex++;
                            break;
                    }
				}
				if (ibuff[0] != '\0') {
					int i=0;
					while (ibuff[i]!=0) printchar(ibuff[i++]);
					lastchar=lastactualchar='.';
					return true;
				}
			}
		}
		return false;
	}

	private void clearworkspace() 	{
        //TODO: возможно, поискать более красивое решение - метод memset
        //TODO: вообще перенести очистку в класс GameState
		//memset(workspace.vartable,0,sizeof(workspace.vartable));
		for (int i=0;i<workspace.vartable.length;i++) workspace.vartable[i]=0; 
	}

    private void ilins(int d0) {
		error("\rIllegal instruction: %d\r",d0);
		//Running=false;
		L9State=L9StateStopped;
	}

    private void function() {
		int d0=l9memory[codeptr++]&0xff;
		CODEFOLLOW(" ",d0==250 ? "printstr" : CODEFOLLOW_functions[d0-1]);

		switch (d0) {
			case 1:
				if (l9GameType == L9GameType.L9_V1)
					StopGame();
				else
					calldriver();
				break;
			case 2: L9Random(); break;
			case 3: save(); break;
			case 4: NormalRestore(); break;
			case 5: clearworkspace(); break;
			case 6: workspace.stackptr=0; break;
			case 250:
				printstringb(codeptr);
				while (l9memory[codeptr++]!=0);
				break;

			default: ilins(d0); //stop w/illegal instruction error
		}
	}

    private void findmsgequiv(int d7) {
		int d4=-1,d0;
		int a2[]={startmd};

		do {
			d4++;
			if (a2[0]>endmd) return;
			d0=l9memory[a2[0]]&0xff;
			if ((d0&0x80)!=0) {
				a2[0]++;
				d4+=d0&0x7f;
			} else if ((d0&0x40)!=0) {
				int d6=getmdlength(a2);
				do {
					int d1;
					if (d6==0) break;

					d1=l9memory[a2[0]++]&0xff;
					d6--;
					if ((d1 & 0x80)!=0) {
						if (d1<0x90) {
							a2[0]++;
							d6--;
						} else {
							d0=(d1<<8) + (l9memory[a2[0]++]&0xff);
							d6--;
							if (d7==(d0 & 0xfff)) {
								d0=((d0<<1)&0xe000) | d4;
                                //TODO: заменить на L9SETWORD
								l9memory[list9ptr+1]=(byte)d0;
								l9memory[list9ptr+0]=(byte)(d0>>8);
								list9ptr+=2;
								if (list9ptr>=list9startptr+0x20) return;
							}
						}
					}
				} while (true);
			}
			else {
				int len=getmdlength(a2);
				a2[0]+=len;
			}
		} while (true);
	}

    private boolean unpackword() {
		int a3;

		if (unpackd3==0x1b) return true;

		a3=(unpackd3&3);

	/*uw01 */
		while (true) {
			int d0=getdictionarycode();
			if (dictptr>=endwdp5) return true;
			if (d0>=0x1b) {
				threechars[a3]=0;
				unpackd3=d0;
				return false;
			}
			threechars[a3++]=(byte)getdictionary(d0);
		}
	}

    private boolean initunpack(int ptr) {
		initdict(ptr);
		unpackd3=0x1c;
		return unpackword();
	}

    private int partword(char c) {
		c= toLower(c);

		if (c==0x27 || c==0x2d) return 0;
		if (c<0x30) return 1;
		if (c<0x3a) return 0;
		if (c<0x61) return 1;
		if (c<0x7b) return 0;
		return 1;
	}

    private int readdecimal() {
		int i=0;
		int r=0;
		while ((obuff[i]!=0) && (obuff[i]>='0') && (obuff[i]<='9')) {
			r=r*10+(obuff[i++]-'0');
		};
		return r;
	}

    private void checknumber() {
		if (obuff[0]>=0x30 && obuff[0]<0x3a) {
			if (l9GameType ==L9GameType.L9_V4) {
				l9memory[list9ptr]=1;
				L9SETWORD(list9ptr+1,readdecimal());
				L9SETWORD(list9ptr+3,0);
			} else {
				L9SETDWORD(list9ptr,readdecimal());
				L9SETWORD(list9ptr+4,0);
			}
		} else {
			L9SETWORD(list9ptr,0x8000);
			L9SETWORD(list9ptr+2,0);
		}
	}

    private void NextCheat() {
		// restore game status 
		//memmove(&workspace,&CheatWorkspace,sizeof(GameState));
		workspace=CheatWorkspace.clone();
		codeptr=acodeptr+workspace.codeptr;

		if (!((l9GameType == L9GameType.L9_V1 || l9GameType == L9GameType.L9_V2)
                ? GetWordV2(CheatWord++) : GetWordV3(CheatWord++))) {
			Cheating=false;
			printstring("\rCheat failed.\r");
			ibuffstr="";
		} else {
			ibuffstr=ibuffstr.concat(" \0");
			ibuff=ibuffstr.toCharArray();
		}
	}

    private void StartCheat() {
		Cheating=true;
		CheatWord=0;

		// save current game status
		//memmove(&CheatWorkspace,&workspace,sizeof(GameState));
		CheatWorkspace=workspace.clone();
		CheatWorkspace.codeptr=(short)((codeptr-acodeptr)&0xffff);

		NextCheat();
	}

	/* v3,4 input routine */
    private boolean GetWordV3(int Word) {
		int subdict=0;
		// 26*4-1=103 

		initunpack(startdata+L9WORD(dictdata));
		unpackword();

		while (Word--!=0)
		{
			if (unpackword())
			{
				if (++subdict==dictdatalen) return false;
				initunpack(startdata+L9WORD(dictdata+(subdict<<2)));
				Word++; // force unpack again 
			}
		}
		ibuffstr="";
		int i=0;
		while (threechars[i]!=0 && i<34) {
			ibuffstr+=(char)(threechars[i++]&0x7f);
		};
		return true;
	};

    private boolean CheckHash() {
		if (stricmp(ibuff,"#cheat")) {
			StartCheat();
		} else if (stricmp(ibuff,"#save")) {
			save();
			return true;
		} else if (stricmp(ibuff,"#restore")) {
			restore();
			return true;
		} else if (stricmp(ibuff,"#quit")) {
			StopGame();
			printstring("\rGame Terminated\r");
			return true;
		} else if (stricmp(ibuff,"#dictionary")) {
			CheatWord=0;
			printstring("\r");
			while ((l9GameType == L9GameType.L9_V1 || l9GameType == L9GameType.L9_V2)
                    ? GetWordV2(CheatWord++) : GetWordV3(CheatWord++)) {
				error("%s ",ibuffstr);
				if ((CheatWord&0x1f)==0) error("\r");
				if (os_stoplist() || L9StateRunning==L9StateStopped) break;
			}
			printstring("\r");
			return true;
		} else if (stricmp(ibuff,"#picture ",9)) {
			int pic=sscanf(ibuff,9);
			if (pic>=0) {
				if (l9GameType == L9GameType.L9_V4)
					os_show_bitmap(pic,0,0);
				else
					show_picture(pic);
			}

			lastactualchar = 0;
			printchar('\r');
			return true;
		} else if (stricmp(ibuff,"#seed ",6)) {
			short seed=(short)sscanf(ibuff,6);
			if ( seed>0 )
				randomseed = constseed = seed;
			lastactualchar = 0;
			printchar('\r');
			return true;
		} else if (stricmp(ibuff,"#play")) {
			playback();
			return true;
		}
		return false;
	}

    private boolean IsInputChar (char c) {
		if (c=='-' || c=='\'')
			return true;
		if ((l9GameType == L9GameType.L9_V3 || l9GameType == L9GameType.L9_V4) && (c=='.' || c==','))
			return true;
		return isalnum(c);
	}

    private boolean corruptinginput() {
		int a0,a2,a6;
		int d0,d1,d2,keywordnumber,abrevword;
		int iptr;
	
		list9ptr=list9startptr;
	
		if (ibuffptr<0) {
			if (Cheating) NextCheat();
			else {
				
				switch (L9State) {
                    case L9StateRunning:
                        if (scriptArray!=null) L9State=L9StateWaitBeforeScriptCommand;
                        else L9State=L9StateWaitForCommand;
                        return false;
                    case L9StateCommandReady:
                        L9State=L9StateRunning;
                        break;
				}
				
				/* flush */
				os_flush();
				lastchar=lastactualchar='.';
				/* get input */
				
				ibuff=new char[IBUFFSIZE];
				if (!scriptinput(ibuff,IBUFFSIZE)) {
					if ((ibuffstr=os_input(IBUFFSIZE))==null) return false; // fall through
					L9DEBUG(">"+ibuffstr);
					ibuffstr=ibuffstr.concat(" \0");
					ibuff=ibuffstr.toCharArray();
				}
				
				//if need empty line after command:
				os_printchar('\r');
				
				if (CheckHash()) return false;
	
				// check for invalid chars
				for (int i=0;i<ibuff.length-1;i++) {
					if (!IsInputChar(ibuff[i]))
						ibuff[i]=' ';
				}

				/* force CR but prevent others */
				os_printchar(lastactualchar='\r');
			}
			ibuffptr=0;
		}
	
		a2=0;
		a6=ibuffptr;
	
	/*ip05 */
		while (true) {
			d0=ibuff[a6++];
			if (d0==0) {
				ibuffptr=-1;
				L9SETWORD(list9ptr,0);
				return true;
			}
			if (partword((char)d0)==0) break;
			if (d0!=0x20) {
				ibuffptr=a6;
				L9SETWORD(list9ptr,d0);
				L9SETWORD(list9ptr+2,0);
				l9memory[list9ptr+1]=(byte)d0;
				obuff[a2]=0x20;
				keywordnumber=-1;
				return true;
			}
		}
	
		a6--;
	/*ip06loop */
		do {
			d0=ibuff[a6++];
			if (partword((char)d0)==1) break;
			d0= toLower((char)d0);
			obuff[a2++]=(char)d0;
		} while (a2<0x1f);
	/*ip06a */
		obuff[a2]=0x20;
		a6--;
		ibuffptr=a6;
		abrevword=-1;
		keywordnumber=-1;
		list9ptr=list9startptr;
	/* setindex */
		a0=dictdata;
		d2=dictdatalen;
		d0=obuff[0]-0x61;
		if (d0<0) {
			a6=defdict;
			d1=0;
		} else {
		/*ip10 */
			d1=0x67;
			if (d0<0x1a) {
				d1=d0<<2;
				d0=obuff[1];
				if (d0!=0x20) d1+=((d0-0x61)>>3)&3;
			}
		/*ip13 */
			if (d1>=d2) {
				checknumber();
				return true;
			}
			a0+=d1<<2;
			a6=startdata+L9WORD(a0);
			d1=L9WORD(a0+2);
		}
	//ip13gotwordnumber 
	
		initunpack(a6);
	//ip14 
		d1--;
		do {
			d1++;
			if (unpackword()) {
            // ip21b
				if (abrevword==-1) break; // goto ip22 
				else d0=abrevword; // goto ip18b 
			} else {
				int a1=0;
				int d6=-1;
	
				a0=0;
			//ip15 
				do {
					d6++;
					d0= toLower((char)(threechars[a1++] & 0x7f));
					d2=obuff[a0++];
				} while (d0==d2);
	
				if (d2!=0x20) {
            // ip17
					if (abrevword==-1) continue;
					else d0=-1;
				}
				else if (d0==0) d0=d1;
				else if (abrevword!=-1) break;
				else if (d6>=4) d0=d1;
				else {
					abrevword=d1;
					continue;
				}
			}
			//ip18b 
			findmsgequiv(d1);
	
			abrevword=-1;
			if (list9ptr!=list9startptr) {
				L9SETWORD(list9ptr,0);
				return true;
			}
		} while (true);
	// ip22 
		checknumber();
		return true;
	}

	/* version 2 stuff hacked from bbc v2 files */
    private boolean IsDictionaryChar(char c) {
		switch (c) {
		    case '?':
            case '-':
            case '\'':
            case '/':
                return true;
		    case '!':
            case '.':
            case ',':
                return true;
		}
		return isUpper(c) || isDigit(c);
	};

    private boolean GetWordV2(int Word) {
		int ptr=dictdata;
		char x;
		while (Word--!=0) {
			do {
				x=(char)(l9memory[ptr++]&0xff);

			//} while (x>0 && x<0x7f);
			//if (x==0) return false; // no more words
			} while (x>31 && x<0x7f);
			if (x<32) return false; // no more words
			ptr++;
		}
		ibuffstr="";
		do {
			x=(char)(l9memory[ptr++]&0xff);
			if (!IsDictionaryChar((char)(x&0x7f))) return false;
			ibuffstr+=(char)(x>32?(x&0x7f):' ');
		} while (x>31 && x<0x7f);
		return true;
	}

    private boolean inputV2() {
		char a,x;
		int ibuffptr,obuffptr,ptr;
		int list0ptr;

		if (Cheating) NextCheat();
		else {
			os_flush();
			
			switch (L9State) {
			case L9StateRunning:
				if (scriptArray!=null) L9State=L9StateWaitBeforeScriptCommand;
				else L9State=L9StateWaitForCommand; //A way to interrupt VM to wait a command
				return false;
			case L9StateCommandReady:
				L9State=L9StateRunning;
				break;
			}
			
			lastchar=lastactualchar='.';
			// get input 
			ibuff=new char[IBUFFSIZE];
			if (!scriptinput(ibuff,IBUFFSIZE)) {
				//TODO: ���������, ��� ������� ������ � os_input, ��� ������ �� �����.
				if ((ibuffstr=os_input(IBUFFSIZE))==null) return false; // fall through
				L9DEBUG(">"+ibuffstr);
				// add space and zero onto end
				ibuffstr=ibuffstr.concat(" \0");
				ibuff=ibuffstr.toCharArray();
			}

			//if need empty line after command:
			os_printchar('\r');
			
			if (CheckHash()) return false;

			// check for invalid chars
			for (int i=0;i<ibuff.length-1;i++) {
				if (!IsInputChar(ibuff[i]))
					ibuff[i]=' ';
			}

			// force CR but prevent others
			os_printchar(lastactualchar='\r');
		}
		wordcount=0;
		ibuffptr=0;
		obuffptr=0;
		list0ptr=dictdata;

		while (ibuff[ibuffptr]==32) ++ibuffptr;

		ptr=ibuffptr;
		do {
			while (ibuff[ptr]==32) ++ptr;
			if (ibuff[ptr]==0) break;
			(wordcount)++;
			do
			{
				a=ibuff[++ptr];
			} while (a!=32 && a!=0);
		} while (ibuff[ptr]>0);

		while (true) {
			ptr=ibuffptr;
			while (ibuff[ibuffptr]==32) ++ibuffptr;

			while (true) {
				a=ibuff[ibuffptr];
				x=(char)(l9memory[list0ptr++]&0xff);

				if (a==32) break;
				if (a==0) {
					obuff[obuffptr++]=0;
					return true;
				}

				++ibuffptr;
				if (!IsDictionaryChar((char)(x&0x7f))) x = 0;
				if (toLower((char)(x&0x7f)) != toLower(a)) {
					while (x>0 && x<0x7f) x=(char)(l9memory[list0ptr++]&0xff);
					if (x==0) {
						do {
							a=ibuff[ibuffptr++];
							if (a==0) {
								obuff[obuffptr]=0;
								return true;
							}
						} while (a!=32);
						while (ibuff[ibuffptr]==32) ++ibuffptr;
						list0ptr=dictdata;
						ptr=ibuffptr;
					} else {
						list0ptr++;
						ibuffptr=ptr;
					}
				} else if (x>=0x7f) break;
			}

			a=ibuff[ibuffptr];
			if (a!=32) {
				ibuffptr=ptr;
				list0ptr+=2;
				continue;
			}
			--list0ptr;
			while ((l9memory[list0ptr++]&0xff)<0x7e);
			obuff[obuffptr++]=(char)(l9memory[list0ptr]&0xff);
			while (ibuff[ibuffptr]==32) ++ibuffptr;
			list0ptr=dictdata;
		}
	}

    private void input() {
		
		if (l9GameType == L9GameType.L9_V3 && FirstPicture >= 0) {
			show_picture(FirstPicture);
			FirstPicture = -1;
		}
		
		// if corruptinginput() returns false then, input will be called again
		// next time around instructionloop, this is used when save() and restore()
		// are called out of line 

		//os_flush();
		
		codeptr--;
		if (l9GameType == L9GameType.L9_V1 || l9GameType == L9GameType.L9_V2) {
			if (inputV2()) { //FALSE IF NO COMMAND ENTERED
				//L9BYTE *obuffptr=(L9BYTE*) obuff;
				codeptr++;
                //todo: проверить правильность конвертации char в short
				workspace.vartable[getvar()]=(short) obuff[0];//*obuffptr++;
				workspace.vartable[getvar()]=(short) obuff[1];//*obuffptr++;
				workspace.vartable[getvar()]=(short) obuff[2];//*obuffptr;
				workspace.vartable[getvar()]=(short)wordcount;
			}
		} else if (corruptinginput()) codeptr+=5;
	}

    private void varcon() {
		int d6=getcon();
		workspace.vartable[getvar()]=(short)d6;
		CODEFOLLOW(" Var[%d]=%d)",cfvar,workspace.vartable[cfvar]);
	}

    private void varvar() {
		int d6=workspace.vartable[getvar()]&0xffff;
		workspace.vartable[getvar()]=(short)d6;
		CODEFOLLOW(" Var[%d]=Var[%d] (=%d)",cfvar,cfvar2,d6);
	}

    private void _add() {
		int d0=workspace.vartable[getvar()]&0xffff;
		workspace.vartable[getvar()]+=d0;
		CODEFOLLOW(" Var[%d]+=Var[%d] (+=%d)",cfvar,cfvar2,d0);
	}

    private void _sub() {
		int d0=workspace.vartable[getvar()]&0xffff;
		workspace.vartable[getvar()]-=d0;
		CODEFOLLOW(" Var[%d]-=Var[%d] (-=%d)",cfvar,cfvar2,d0);
	}

    private void jump() {
		int d0=L9WORD(codeptr);
		int a0;
		codeptr+=2;

		a0=acodeptr+((d0+((workspace.vartable[getvar()]&0xffff)<<1))&0xffff);
		codeptr=acodeptr+L9WORD(a0);
	}

	/* bug */
    private void exit1(byte[] d4,byte[] d5,byte d6,byte d7) {
		int a0=absdatablock;
		byte d1=d7,d0;
		boolean skip=false;
		if (--d1!=0) {
			do {
				d0=l9memory[a0];
				if (l9GameType == L9GameType.L9_V4) {
					if ((d0==0) && (l9memory[a0+1]==0)) {
						skip=true;
						break;
					}
				}
				a0+=2;
			}
			while ((d0&0x80)==0 || (--d1!=0));
		}
		if (!skip) {
			do {
				d4[0]=l9memory[a0++];
				if (((d4[0])&0xf)==d6) {
					d5[0]=l9memory[a0];
					return;
				}
				a0++;
			}
			while (((d4[0])&0x80)==0);
		}

		/* notfn4 */
	//notfn4:

        //TODO: fix by tsap, d6 cannot be >15. превышение массива и crash в snowball.sna
        if (d6>15) d6=15; //пофиксил только выход из диапазона, правильно ли - не знаю.
		//end of fix
		d6=(byte)(exitreversaltable[d6]&0xff);
		a0=absdatablock;
		d5[0]=1;

		do {
			d4[0]=l9memory[a0++];
			if (((d4[0])&0x10)==0 || ((d4[0])&0xf)!=d6) a0++;
			else if (l9memory[a0++]==d7) return;
			/* exit6noinc */
			if (((d4[0])&0x80)!=0) d5[0]++;
		} while (d4[0]!=0);
		d5[0]=0;
	}

    private void Exit() {
		byte[] d4={0};
		byte[] d5={0};
		byte d7=(byte) (workspace.vartable[getvar()]&0xff);
		byte d6=(byte) (workspace.vartable[getvar()]&0xff);
		CODEFOLLOW(" d7=%d d6=%d",d7&0xff,d6&0xff);
		exit1(d4,d5,d6,d7);

		workspace.vartable[getvar()]=(short)((d4[0]&0x70)>>4);
		workspace.vartable[getvar()]=(short)(d5[0]&0xff);
		CODEFOLLOW(" Var[%d]=%d(d4=%d) Var[%d]=%d",cfvar2,(d4[0]&0x70)>>4,d4[0]&0xff,cfvar,d5[0]&0xff);
	}

    private void ifeqvt() {
		int d0=workspace.vartable[getvar()]&0xffff;
		int d1=workspace.vartable[getvar()]&0xffff;
		int a0=getaddr();
		if (d0==d1) codeptr=a0;
		CODEFOLLOW(String.format(" if Var[%d]=Var[%d] goto %d (%s)",cfvar2,cfvar,a0-acodeptr,d0==d1 ? "Yes":"No"));

	}

    private void ifnevt() {
		int d0=workspace.vartable[getvar()]&0xffff;
		int d1=workspace.vartable[getvar()]&0xffff;
		int a0=getaddr();
		if (d0!=d1) codeptr=a0;
		CODEFOLLOW(String.format(" if Var[%d]!=Var[%d] goto %d (%s)",cfvar2,cfvar,a0-acodeptr,d0!=d1 ? "Yes":"No"));
	}

    private void ifltvt() {
		int d0=workspace.vartable[getvar()]&0xffff;
		int d1=workspace.vartable[getvar()]&0xffff;
		int a0=getaddr();
		if (d0<d1) codeptr=a0;
		CODEFOLLOW(String.format(" if Var[%d]<Var[%d] goto %d (%s)",cfvar2,cfvar,a0-acodeptr,d0<d1 ? "Yes":"No"));
	}

    private void ifgtvt() {
		int d0=workspace.vartable[getvar()]&0xffff;
		int d1=workspace.vartable[getvar()]&0xffff;
		int a0=getaddr();
		if (d0>d1) codeptr=a0;
		CODEFOLLOW(String.format(" if Var[%d]>Var[%d] goto %d (%s)",cfvar2,cfvar,a0-acodeptr,d0>d1 ? "Yes":"No"));
	}

    private void ifeqct() {
		int d0=workspace.vartable[getvar()]&0xffff;
		int d1=getcon();
		int a0=getaddr();
		if (d0==d1) codeptr=a0;
		CODEFOLLOW(String.format(" if Var[%d]=%d goto %d (%s)",cfvar,d1,a0-acodeptr,d0==d1 ? "Yes":"No"));
	}

    private void ifnect() {
		int d0=workspace.vartable[getvar()]&0xffff;
		int d1=getcon();
		int a0=getaddr();
		if (d0!=d1) codeptr=a0;
		CODEFOLLOW(String.format(" if Var[%d]!=%d goto %d (%s)",cfvar,d1,a0-acodeptr,d0!=d1 ? "Yes":"No"));
	}

    private void ifltct() {
		int d0=workspace.vartable[getvar()]&0xffff;
		int d1=getcon();
		int a0=getaddr();
		if (d0<d1) codeptr=a0;
		CODEFOLLOW(String.format(" if Var[%d]<%d goto %d (%s)",cfvar,d1,a0-acodeptr,d0<d1 ? "Yes":"No"));
	}

    private void ifgtct() {
		int d0=workspace.vartable[getvar()]&0xffff;
		int d1=getcon();
		int a0=getaddr();
		if (d0>d1) codeptr=a0;
		CODEFOLLOW(String.format(" if Var[%d]>%d goto %d (%s)",cfvar,d1,a0-acodeptr,d0>d1 ? "Yes":"No"));
	}

    private int scalex(int x) {
		return (gfx_mode != GFX_V3C) ? (x>>6) : (x>>5);
	}

    private int scaley(int y) {
		return (gfx_mode == GFX_V2) ? 127 - (y>>7) : 95 - (((y>>5)+(y>>6))>>3);
	}

    private void detect_gfx_mode() {
		if (l9GameType == L9GameType.L9_V3) {
			/* These V3 games use graphics logic similar to the V2 games */
			if (strstr(FirstLine,"price of magik") != 0)
				gfx_mode = GFX_V3A;
			else if (strstr(FirstLine,"the archers") != 0)
				gfx_mode = GFX_V3A;
			else if (strstr(FirstLine,"secret diary of adrian mole") != 0)
				gfx_mode = GFX_V3A;
			else if ((strstr(FirstLine,"worm in paradise") != 0)
				&& (strstr(FirstLine,"silicon dreams") == 0))
				gfx_mode = GFX_V3A;
			else if (strstr(FirstLine,"growing pains of adrian mole") != 0)
				gfx_mode = GFX_V3B;
			else if ((strstr(FirstLine,"jewels of darkness") != 0) && (picturesize < 11000))
				gfx_mode = GFX_V3B;
			else if (strstr(FirstLine,"silicon dreams") != 0) {
				if ((picturesize > 11000)
					|| ((l9memory[startdata] == 0x14) && (l9memory[startdata+1] == 0x7d))  /* Return to Eden /SD (PC) */
					|| ((l9memory[startdata] == 0xd7) && (l9memory[startdata+1] == 0x7c))) /* Worm in Paradise /SD (PC) */
					gfx_mode = GFX_V3C;
				else
					gfx_mode = GFX_V3B;
			} 
			else
				gfx_mode = GFX_V3C;
		}
		else
			gfx_mode = GFX_V2;
	}

    private void _screen() {
		int mode = 0;
		
		//if (L9GameType == L9_V3 && strlen(FirstLine) == 0)
		if ((l9GameType == L9GameType.L9_V3) && (FirstLine[0]==0)) {
			if ((l9memory[codeptr++]&0xff)!=0)
				codeptr++;
			return;
		}

		detect_gfx_mode();

		l9textmode = l9memory[codeptr++]&0xff;
		if (l9textmode!=0) {
			if (l9GameType ==L9GameType.L9_V4)
				mode = 2;
			else if (picturedata>=0)
				mode = 1;
		}
		os_graphics(mode);

		screencalled = 1;

		L9DEBUG ("screen ",l9textmode!=0 ? "graphics" : "text");


		if (l9textmode!=0) {
			codeptr++;
			os_cleargraphics();

			/* title pic */
			if (showtitle==1 && mode==2) {
				showtitle = 0;
				os_show_bitmap(0,0,0);
			}
		}
	}

    private void cleartg() {
		int d0 = l9memory[codeptr++]&0xff;
		L9DEBUG ("cleartg %s\r",d0!=0 ? "graphics" : "text");

		if (d0!=0)
		{
			if (l9textmode!=0)
				os_cleargraphics();
		}
	}

    private boolean validgfxptr(int a5)	{
		return ((a5 >= picturedata) && (a5 < picturedata+picturesize));
	}

    private boolean findsub(int d0,int[] a5) {
		int d1,d2,d3,d4;

		d1=d0 << 4;
		d2=d1 >> 8;
		a5[0]=picturedata;
	/* findsubloop */
		while (true) {
			d3=l9memory[a5[0]++]&0xff;
			if (!validgfxptr(a5[0]))
				return false;
			if ((d3&0x80)!=0) 
				return false;
			if (d2==d3) {
				if ((d1&0xff)==(l9memory[a5[0]] & 0xf0)) {
					a5[0]+=2;
					return true;
				}
			}

			d3=l9memory[a5[0]++] & 0x0f;
			if (!validgfxptr(a5[0]))
				return false;

			d4=l9memory[a5[0]]&0xff;
			if ((d3|d4)==0)
				return false;

			a5[0]+=(d3<<8) + d4 - 2;
			if (!validgfxptr(a5[0]))
				return false;
		}
	}

    private void gosubd0(int d0, int[] a5) {
		if (GfxA5StackPos < GFXSTACKSIZE) {
			GfxA5Stack[GfxA5StackPos] = a5[0];
			GfxA5StackPos++;
			GfxScaleStack[GfxScaleStackPos] = scale;
			GfxScaleStackPos++;
	
			if (findsub(d0,a5) == false) {
				//GfxStackPos--;
				//a5[0] = GfxStack[GfxStackPos].a5;
				//scale = GfxStack[GfxStackPos].scale;
				GfxA5StackPos--;
				a5[0] = GfxA5Stack[GfxA5StackPos];
				GfxScaleStackPos--;
				scale = GfxScaleStack[GfxScaleStackPos];
			}
		}
	}

    private void newxy(int x, int y) {
		drawx += (x*scale)&~7;
		drawy += (y*scale)&~7;
	}
	
	/* sdraw instruction plus arguments are stored in an 8 bit word.
	       76543210
	       iixxxyyy
	   where i is instruction code
	         x is x argument, high bit is sign
	         y is y argument, high bit is sign
	*/
    private void sdraw(int d7) {
		int x,y,x1,y1;

	/* getxy1 */
		x = (d7&0x18)>>3;
		if ((d7&0x20)!=0)
			x = (x|0xfc) - 0x100;
		y = (d7&0x3)<<2;
		if ((d7&0x4)!=0)
			y = (y|0xf0) - 0x100;

		if ((reflectflag&2)!=0)
			x = -x;
		if ((reflectflag&1)!=0)
			y = -y;

	/* gintline */
		x1 = drawx;
		y1 = drawy;
		newxy(x,y);

		L9DEBUG(String.format("gfx - sdraw (%d,%d) (%d,%d) colours %d,%d",x1,y1,drawx,drawy,gintcolour&3,option&3));

		os_drawline(scalex(x1),scaley(y1),scalex(drawx),scaley(drawy),gintcolour&3,option&3);
	}

	/* smove instruction plus arguments are stored in an 8 bit word.
	       76543210
	       iixxxyyy
	   where i is instruction code
	         x is x argument, high bit is sign
	         y is y argument, high bit is sign
	*/
    private void smove(int d7) {
		int x,y;

	/* getxy1 */
		x = (d7&0x18)>>3;
		if ((d7&0x20)!=0)
			x = (x|0xfc) - 0x100;
		y = (d7&0x3)<<2;
		if ((d7&0x4)!=0)
			y = (y|0xf0) - 0x100;

		if ((reflectflag&2)!=0)
			x = -x;
		if ((reflectflag&1)!=0)
			y = -y;
		newxy(x,y);
	}

    private void sgosub(int d7, int a5[]) {
		int d0 = d7&0x3f;
		L9DEBUG("gfx - sgosub %d",d0);
		gosubd0(d0,a5);
	}

	/* draw instruction plus arguments are stored in a 16 bit word.
	    FEDCBA9876543210
	    iiiiixxxxxxyyyyy
	where i is instruction code
	      x is x argument, high bit is sign
	      y is y argument, high bit is sign
	*/
    private void draw(int d7, int[] a5) {
		int xy,x,y,x1,y1;
	
	/* getxy2 */
		xy = (d7<<8)+(l9memory[a5[0]++]&0xff);
		x = (xy&0x3e0)>>5;
		if ((xy&0x400)!=0)
			x = (x|0xe0) - 0x100;
		y = (xy&0xf)<<2;
		if ((xy&0x10)!=0)
			y = (y|0xc0) - 0x100;
	
		if ((reflectflag&2)!=0)
			x = -x;
		if ((reflectflag&1)!=0)
			y = -y;
	
	/* gintline */
		x1 = drawx;
		y1 = drawy;
		newxy(x,y);
	
		L9DEBUG(String.format("gfx - draw (%d,%d) (%d,%d) colours %d,%d",x1,y1,drawx,drawy,gintcolour&3,option&3));

		os_drawline(scalex(x1),scaley(y1),scalex(drawx),scaley(drawy),gintcolour&3,option&3);
	}
	
	// move instruction plus arguments are stored in a 16 bit word.
	//       FEDCBA9876543210
	//       iiiiixxxxxxyyyyy
	//   where i is instruction code
	//         x is x argument, high bit is sign
	//         y is y argument, high bit is sign
	//
    private void _move(int d7, int[] a5) {
		int xy,x,y;

	/* getxy2 */
		xy = (d7<<8)+(l9memory[a5[0]++]&0xff);
		x = (xy&0x3e0)>>5;
		if ((xy&0x400)!=0)
			x = (x|0xe0) - 0x100;
		y = (xy&0xf)<<2;
		if ((xy&0x10)!=0)
			y = (y|0xc0) - 0x100;

		if ((reflectflag&2)!=0)
			x = -x;
		if ((reflectflag&1)!=0)
			y = -y;
		newxy(x,y);
	}

    private void icolour(int d7) {
		gintcolour = d7&3;
		L9DEBUG("gfx - icolour 0x%d",gintcolour);
	}

    private void size(int d7) {
        //TODO: нужна ли эта табличка именно здесь?
		final int sizetable[] = { 0x02,0x04,0x06,0x07,0x09,0x0c,0x10 };
	
		d7 &= 7;
		if (d7!=0)
		{
			int d0 = (scale*sizetable[d7-1])>>3;
			scale = (d0 < 0x100) ? d0 : 0xff;
		}
		else {
	/* sizereset */
			scale = 0x80;
			if (gfx_mode == GFX_V2 || gfx_mode == GFX_V3A)
				GfxScaleStackPos = 0;
		}
		L9DEBUG("gfx - size 0x%d",scale);
	}

    private void gintfill(int d7) {
		if ((d7&7) == 0)
	/* filla */
			d7 = gintcolour;
		else
			d7 &= 3;
	/* fillb */

		L9DEBUG(String.format("gfx - gintfill (%d,%d) colours %d,%d",drawx,drawy,d7&3,option&3));

		os_fill(scalex(drawx),scaley(drawy),d7&3,option&3);
	}

    private void gosub(int d7, int[] a5) {
		int d0 = ((d7&7)<<8)+(l9memory[a5[0]++]&0xff);
		L9DEBUG("gfx - gosub %d",d0);
		gosubd0(d0,a5);
	}

    private void reflect(int d7) {
		L9DEBUG("gfx - reflect 0x%d",d7);
	
		if ((d7&4)!=0)
		{
			d7 &= 3;
			d7 ^= reflectflag;
		}
	// reflect1 
		reflectflag = d7;
	}

    private void notimp() {
		L9DEBUG("gfx - notimp");
	}

    private void gintchgcol(int[] a5) {
		int d0 = l9memory[a5[0]++]&0xff;
		L9DEBUG("gfx - gintchgcol %d %d",(d0>>3)&3,d0&7);
		os_setcolour((d0>>3)&3,d0&7);
	}

    private void amove(int[] a5) {
		drawx = 0x40*(l9memory[a5[0]++]&0xff);
		drawy = 0x40*(l9memory[a5[0]++]&0xff);
		L9DEBUG ("gfx - amove (%d,%d)",drawx,drawy);
	}

    private void opt(int[] a5) {
		int d0 = l9memory[a5[0]++]&0xff;
		L9DEBUG ("gfx - opt %d",d0);
		if (d0!=0)
			d0 = (d0&3)|0x80;
	/* optend */
		option = d0;
	}

    private void restorescale() {
		L9DEBUG ("gfx - restorescale");
		if (GfxScaleStackPos > 0)
			scale = GfxScaleStack[GfxScaleStackPos-1];
	}

    private boolean rts(int[] a5) {
		if (GfxA5StackPos > 0) {
			GfxA5StackPos--;
			a5[0] = GfxA5Stack[GfxA5StackPos];
			if (GfxScaleStackPos > 0) {
				GfxScaleStackPos--;
				scale = GfxScaleStack[GfxScaleStackPos];
			}
			return true;
		}
		return false;
	}

    private boolean getinstruction(int[] a5) {
		int d7 = l9memory[a5[0]++]&0xff;
		if ((d7&0xc0) != 0xc0) {
			switch ((d7>>6)&3) {
                case 0: sdraw(d7); break;
                case 1: smove(d7); break;
                case 2: sgosub(d7,a5); break;
			}
		} else if ((d7&0x38) != 0x38) {
			switch ((d7>>3)&7) {
                case 0: draw(d7,a5); break;
                case 1: _move(d7,a5); break;
                case 2: icolour(d7); break;
                case 3: size(d7); break;
                case 4: gintfill(d7); break;
                case 5: gosub(d7,a5); break;
                case 6: reflect(d7); break;
			}
		} else {
			switch (d7&7) {
                case 0: notimp(); break;
                case 1: gintchgcol(a5); break;
                case 2: notimp(); break;
                case 3: amove(a5); break;
                case 4: opt(a5); break;
                case 5: restorescale(); break;
                case 6: notimp(); break;
                case 7: return rts(a5);
			}
		}
		return true;
	}

    private void absrunsub(int d0) {
		int[] a5={0};
		if (!findsub(d0,a5))
			return;
		while (getinstruction(a5)) {}
	}

    private void show_picture(int pic) {
		if ((l9GameType == L9GameType.L9_V3) && (FirstLine[0] == 0)) {
			FirstPicture = pic;
			return;
		}

		if (picturedata>=0) {
			// Some games don't call the screen() opcode before drawing
			// graphics, so here graphics are enabled if necessary.
			if ((screencalled == 0) && (l9textmode == 0))
			{
				detect_gfx_mode();
				l9textmode = 1;
				os_graphics(1);
			}

			L9DEBUG("picture %d",pic);

			os_cleargraphics();
	// gintinit 
			gintcolour = 3;
			option = 0x80;
			reflectflag = 0;
			drawx = 0x1400;
			drawy = 0x1400;
	// sizereset 
			scale = 0x80;

			GfxA5StackPos=0;
			GfxScaleStackPos=0;
			absrunsub(0);
			if (!findsub(pic,gfxa5))
				gfxa5[0] = -1;
            else os_start_drawing(pic);
		}
	}

    private void picture() {
		L9DEBUG("picture\r");
		show_picture(workspace.vartable[getvar()]&0xffff);
	}

    public PictureSize getPictureSize() {
		int width = 0;
        int height = 0;
        if (l9GameType != L9GameType.L9_V4) {
			width = (gfx_mode != GFX_V3C) ? 160 : 320;
			height = (gfx_mode == GFX_V2) ? 128 : 96;
		}
        return new PictureSize(width, height);
	}

    public boolean runGraphics() {
		if (gfxa5[0]>0) {
			if (!getinstruction(gfxa5))
				gfxa5[0] = -1;
			return true;
		}
		return false;
	}

    private void initgetobj() {
		int i;
		numobjectfound=0;
		object=0;
		for (i=0;i<32;i++) gnoscratch[i]=0;
	}

    private void getnextobject() {
		short d2,d3,d4;
		int hisearchposvar,searchposvar;

		L9DEBUG ("getnextobject\r");

		d2=workspace.vartable[getvar()];
		hisearchposvar=getvar();
		searchposvar=getvar();
		d3=workspace.vartable[hisearchposvar];
		d4=workspace.vartable[searchposvar];

	// gnoabs 
		do {
			if ((d3 | d4)==0) {
				// initgetobjsp
				gnosp=128;
				searchdepth=0;
				initgetobj();
				break;
			}

			if (numobjectfound==0) inithisearchpos=d3;

		// gnonext 
			do {
				if (d4==(l9memory[list2ptr+(++object)]&0xff)) {
					// gnomaybefound 
					int d6=l9memory[list3ptr+object]&0x1f;
					if (d6!=d3) {
						if (d6==0 || d3==0) continue;
						if (d3!=0x1f) {
							gnoscratch[d6]=(short)d6;
							continue;
						}
						d3=(short)d6;
					}
					// gnofound 
					numobjectfound++;
					gnostack[--gnosp]=object;
					gnostack[--gnosp]=0x1f;

					workspace.vartable[hisearchposvar]=d3;
					workspace.vartable[searchposvar]=d4;
					workspace.vartable[getvar()]=object;
					workspace.vartable[getvar()]=numobjectfound;
					workspace.vartable[getvar()]=searchdepth;
					return;
				}
			} while (object<=d2);

			if (inithisearchpos==0x1f) {
				gnoscratch[d3]=0;
				d3=0;

			// gnoloop 
				do {
					if (gnoscratch[d3]!=0) {
						gnostack[--gnosp]=d4;
						gnostack[--gnosp]=d3;
					}
				} while (++d3<0x1f);
			}
		// gnonewlevel 
			if (gnosp!=128) {
				d3=(short)(gnostack[gnosp++]&0xffff);
				d4=(short)(gnostack[gnosp++]&0xffff);
			} else d3=d4=0;

			numobjectfound=0;
			if (d3==0x1f) searchdepth++;

			initgetobj();
		} while (d4!=0);

	// gnofinish 
	// gnoreturnargs 
		workspace.vartable[hisearchposvar]=0;
		workspace.vartable[searchposvar]=0;
		workspace.vartable[getvar()]=object=0;
		workspace.vartable[getvar()]=numobjectfound;
		workspace.vartable[getvar()]=searchdepth;
	}

    private void printinput() {
		int ptr=0;//(L9BYTE*) obuff;
		char c;
		while ((c=obuff[ptr++])!=' ') printchar(c);
		L9DEBUG ("printinput\r");
	}

    private void listhandler() {
		int a4, MinAccess, MaxAccess;
		short val;
		int var;
		int offset; //for CODEFOLLOW

		if ((code&0x1f)>0xa) {
			error("\rillegal list access %d\r",code&0x1f);
			L9State=L9StateStopped;
			return;
		}
		a4=L9Pointers[1+code&0x1f];

		if (a4>=listarea && a4<listarea+LISTAREASIZE) {
			MinAccess=listarea;
			MaxAccess=listarea+LISTAREASIZE;
		} else {
			MinAccess=startdata;
			MaxAccess=startdata+datasize;
		}

		if (code>=0xe0)	{		// listvv
			a4+=(offset=(workspace.vartable[getvar()]&0xffff));
			val=workspace.vartable[var=getvar()];
			CODEFOLLOW(" list %d [%d]=Var[%d] (=%d)",code&0x1f,offset,var,val);

			if (a4>=MinAccess && a4<MaxAccess) l9memory[a4]=(byte)(val&0xff);
			else {
				L9DEBUG("Out of range list access\r");
			};
		}
		else if (code>=0xc0) {	// listv1c
			a4+=(offset=l9memory[codeptr++]&0xff);
			var=getvar();
			CODEFOLLOW(" Var[%d]= list %d [%d])",var,code&0x1f,offset);
			if (a4>=MinAccess && a4<MaxAccess) CODEFOLLOW(" (=%d)",l9memory[a4]&0xff);

			if (a4>=MinAccess && a4<MaxAccess) workspace.vartable[var]=(short)(l9memory[a4]&0xff);
			else {
				workspace.vartable[var]=0;
				L9DEBUG("Out of range list access\r");
			}
		} else if (code>=0xa0) {	// listv1v
			a4+=(offset=workspace.vartable[getvar()]&0xffff);
			var=getvar();
			CODEFOLLOW(" Var[%d] =list %d [%d]",var,code&0x1f,offset);
			if (a4>=MinAccess && a4<MaxAccess) CODEFOLLOW(" (=%d)",l9memory[a4]&0xff);

			if (a4>=MinAccess && a4<MaxAccess) workspace.vartable[var]=(short)(l9memory[a4]&0xff);
			else {
				workspace.vartable[var]=0;
				L9DEBUG("Out of range list access\r");
			}
		} else {
			a4+=(offset=l9memory[codeptr++]&0xff);
			val=workspace.vartable[var=getvar()];
			CODEFOLLOW(" list %d [%d]=Var[%d] (=%d)",code&0x1f,offset,var,val);

			if (a4>=MinAccess && a4<MaxAccess) l9memory[a4]=(byte) (val&0xff);
			else {
				L9DEBUG("Out of range list access\r");
			};
		}
	}

    private void executeinstruction() {
		CODEFOLLOW("%d (s:%d) %x",(codeptr-acodeptr)-1,workspace.stackptr,code);
		if (!((code&0x80)!=0)) CODEFOLLOW(" = ",CODEFOLLOW_codes[code&0x1f]);

		if ((code & 0x80)!=0) listhandler();
		else switch (code & 0x1f) {
			case 0:		Goto();break;
			case 1: 	intgosub();break;
			case 2:		intreturn();break;
			case 3:		printnumber();break;
			case 4:		messagev();break;
			case 5:		messagec();break;
			case 6:		function();break; //some functions, with os_readchar
			case 7:		input();break; //wait for a command from user
			case 8:		varcon();break;
			case 9:		varvar();break;
			case 10:	_add();break;
			case 11:	_sub();break;
			case 12:	ilins(code & 0x1f);break;
			case 13:	ilins(code & 0x1f);break;
			case 14:	jump();break;
			case 15:	Exit();break;
			case 16:	ifeqvt();break;
			case 17:	ifnevt();break;
			case 18:	ifltvt();break;
			case 19:	ifgtvt();break;
			case 20:	_screen();break;
			case 21:	cleartg();break;
			case 22:	picture();break;
			case 23:	getnextobject();break;
			case 24:	ifeqct();break;
			case 25:	ifnect();break;
			case 26:	ifltct();break;
			case 27:	ifgtct();break;
			case 28:	printinput();break;
			case 29:	ilins(code & 0x1f);break;
			case 30:	ilins(code & 0x1f);break;
			case 31:	ilins(code & 0x1f);break;
		}
		CODEFOLLOW(); //out string
        //раскомментировать (и сам метод тоже) для разницы в памяти:
        //L9MemoryDiff();
        //раскомментировать (и сам метод тоже) для разницы в переменных:
        //VarDiff();
	}

    private int LoadGame2(String filename, String picname) {
		// may be already running a game, maybe in input routine
		L9State=L9StateStopped;
		ibuffptr=-1; //��������� �� ����� � ������ ������� ��� V3,4
		if (!intinitialise(filename,picname)) return L9StateStopped;
		codeptr=acodeptr;
		if (constseed > 0)
			randomseed=constseed;
		else
			randomseed = (short)(Math.random()*32767);
		LastGame=filename;
		L9State=L9StateRunning;
		return L9State;
	}

    public boolean LoadGame(String fileName, String picName) {
		int ret=LoadGame2(fileName, picName);
		if (ret!=L9StateRunning) return false;
		showtitle=1;
		clearworkspace();
		workspace.stackptr=0;
		/* need to clear listarea as well */
        //TODO: поискать более красивое решение - метод memset (как и clearworkspace)
        //TODO: вообще перенести очистку в класс GameState
		for (int i=0;i<LISTAREASIZE;i++) l9memory[listarea+i]=0;
		return ret==L9StateRunning; //true - L9StateRunning, false - otherway
	}

	/* can be called from input to cause fall through for exit */
	public void StopGame () {
		L9State=L9StateStopped;
	}

	public int RunGame() {
		//TODO: возможно нужна строка:
		//TODO: if (L9State!=L9StateRunning && L9State!=L9StateCommandReady) return L9State;
		code=l9memory[codeptr++]&0xff;
		executeinstruction();
		return L9State;
	}

	public void RestoreGame(String inFile) {
		int Bytes;
		GameState temp;
		//TODO: ЧЕМ ОТЛИЧАЕТСЯ ОТ NORMALRESTORE?
		/*TODO:
		FILE* f = NULL;

		if ((f = fopen(filename, "rb")) != NULL)
		{
			Bytes = fread(&temp, 1, sizeof(GameState), f);
			if (Bytes==V1FILESIZE)
			{
				printstring("\rGame restored.\r");
				// only copy in workspace 
				memset(workspace.listarea,0,LISTAREASIZE);
				memmove(workspace.vartable,&temp,V1FILESIZE);
			}
			else if (CheckFile(&temp))
			{
				printstring("\rGame restored.\r");
				// full restore
				memmove(&workspace,&temp,sizeof(GameState));
				codeptr=acodeptr+workspace.codeptr;
			}
			else
				printstring("\rSorry, unrecognised format. Unable to restore\r");
		}
		else
			printstring("\rUnable to restore game.\r");
		*/
	}

	////////////////////////////////////////////////////////////////////////

    public abstract  void os_printchar(char c);
	//TODO: KILL os_input()
	public String os_input(int size) {return InputString;};
    public abstract char os_readchar(int millis);
	boolean os_stoplist() {return false;};
    public abstract void os_flush();
	//L9BOOL os_save_file(L9BYTE* Ptr, int Bytes)
    public abstract boolean os_save_file(byte[] buff);
	//L9BOOL os_load_file(L9BYTE* Ptr, int* Bytes, int Max)
    public abstract byte[] os_load_file();
    public abstract void os_graphics(int mode);
    public abstract void os_cleargraphics();
    public abstract void os_start_drawing(int pic);
    public abstract void os_setcolour(int colour, int index);
    public abstract void os_drawline(int x1, int y1, int x2, int y2, int colour1, int colour2);
    public abstract void os_fill(int x, int y, int colour1, int colour2);
    public abstract void os_show_bitmap(int pic, int x, int y);
    public abstract byte[] os_open_script_file();
    public abstract String os_get_game_file(String NewName);
    public abstract String os_set_filenumber(String NewName, int num);
	
	//added by tsap
	public abstract byte[] os_load(String filename);
	public abstract void os_debug(String str);
	public abstract void os_verbose(String str);

	///////////////////// New (tsap) implementations ////////////////////

	public void InputCommand(String str) {
		if (str==null) return; 
		InputString=str;
		L9State=L9StateCommandReady;
	}
	
	//#define L9WORD(x) (*(x) + ((*(x+1))<<8))
	//!! возвращаю int, чтобы не заморачиваться с +/-
    private int L9WORD(int x) {
		return (l9memory[x]&255)+((l9memory[x+1]&255)<<8); //&255 для конверсии в int без учета знака.
	}
	
	//#define L9SETWORD(x,val) *(x)=(L9BYTE) val; *(x+1)=(L9BYTE)(val>>8);
    private void L9SETWORD(int x, int val) {
		l9memory[x]=(byte)(val & 0xff);
		l9memory[x+1]=(byte)((val & 0xff00)>>8);
	}
	
	//#define L9SETDWORD(x,val) *(x)=(L9BYTE)val; *(x+1)=(L9BYTE)(val>>8); *(x+2)=(L9BYTE)(val>>16); *(x+3)=(L9BYTE)(val>>24);
    private void L9SETDWORD(int x, int val) {
		l9memory[x]=(byte)(val & 0xff);
		l9memory[x+1]=(byte)((val & 0xff00)>>8);
		l9memory[x+2]=(byte)((val & 0xff0000)>>16);
		l9memory[x+3]=(byte)((val & 0xff000000)>>24);
	}
	
	//L9DEBUG
    private void L9DEBUG(String txt) {
		os_debug(txt);
	}

    private void L9DEBUG(String txt1, String txt2) {
		L9DEBUG(txt1+txt2);
	}

    private void L9DEBUG(String txt, int val) {
		L9DEBUG(String.format(txt, val));
	}

    private void L9DEBUG(String txt, int val1, int val2) {
		L9DEBUG(String.format(txt, val1, val2));
	}

	//CODEFOLLOW
    private String CODEFOLLOWSTRING;

    private void CODEFOLLOW() {
		if (CODEFOLLOWSTRING!=null)
			os_verbose(CODEFOLLOWSTRING);
		CODEFOLLOWSTRING=null;
	}

    private void CODEFOLLOW(String txt) {
//uncomment for CODEFOLLOW feature
		if (CODEFOLLOWSTRING==null) CODEFOLLOWSTRING="";
		CODEFOLLOWSTRING+=txt;
	}

    private void CODEFOLLOW(String txt1, String txt2) {
		CODEFOLLOW(txt1+txt2);
	}

    private void CODEFOLLOW(String txt, int val) {
		CODEFOLLOW(String.format(txt, val));
	}

    private void CODEFOLLOW(String txt, int val1, int val2) {
		CODEFOLLOW(String.format(txt, val1, val2));
	}

    private void CODEFOLLOW(String txt, int val1, int val2, int val3) {
		CODEFOLLOW(String.format(txt, val1, val2, val3));
	}

    private void CODEFOLLOW(String txt, int val1, int val2, int val3, int val4) {
		CODEFOLLOW(String.format(txt, val1, val2, val3, val4));
	}

    private void CODEFOLLOW(String txt, int val1, int val2, int val3, int val4, int val5) {
		CODEFOLLOW(String.format(txt, val1, val2, val3, val4, val5));
	}
	
	/* дебажные функции
	byte l9clonememory[];
	void L9MemoryDiff() {
		if (l9clonememory!=null) {
			for (int i=0;i<l9memory.length;i++)
				if (l9memory[i]!=l9clonememory[i])
					L9DEBUG(String.format("memdiff: %d: %d<-%d\r",i,l9memory[i]&0xff,l9clonememory[i]&0xff));
		}
		l9clonememory=l9memory.clone();
	}
	
	short vartableclone[];
	void VarDiff() {
		if (vartableclone!=null) {
			for (int i=0;i<workspace.vartable.length;i++)
				if (workspace.vartable[i]!=vartableclone[i])
					L9DEBUG(String.format("vardiff: var[%d]: %d<-%d\r",i,workspace.vartable[i]&0xff,vartableclone[i]&0xff));
		};
		vartableclone=workspace.vartable.clone();
	}
	*/
}