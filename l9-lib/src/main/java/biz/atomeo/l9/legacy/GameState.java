package biz.atomeo.l9.legacy;

/*-------------
 typedef struct
{
	L9UINT32 Id;
	L9UINT16 codeptr,stackptr,listsize,stacksize,filenamesize,checksum;
	L9UINT16 vartable[256];
	L9BYTE listarea[LISTAREASIZE];
	L9UINT16 stack[STACKSIZE];
	char filename[MAX_PATH];
} GameState;

*/
public class GameState {

    private static final int VARSIZE = 256;
    private static final int L9_ID=0x4c393031;

    int Id;
    short codeptr,stackptr,listsize,stacksize,filenamesize,checksum;
    short[] vartable;
    //byte listarea[];
    short[] stack;
    String filename;

    GameState() {
        vartable=new short[VARSIZE];
        //listarea=new byte[LISTAREASIZE];
        stack=new short[L9.STACKSIZE];
    }

    public GameState clone() {
        GameState gs=new GameState();
        gs.codeptr=this.codeptr;
        gs.stackptr=this.stackptr;
        gs.listsize=this.listsize;
        gs.stacksize=this.stacksize;
        gs.filenamesize=this.filenamesize;
        gs.checksum=this.checksum;
        gs.vartable=this.vartable.clone();
        gs.stack=this.stack.clone();
        gs.filename=this.filename+"";
        return gs;
    }

	/*

	void save(void)
	{
		L9UINT16 checksum;
		int i;
	#ifdef L9DEBUG
		printf("function - save\n");
	#endif
	// does a full save, workpace, stack, codeptr, stackptr, game name, checksum

		workspace.Id=L9_ID;
		workspace.codeptr=codeptr-acodeptr;
		workspace.listsize=LISTAREASIZE;
		workspace.stacksize=STACKSIZE;
		workspace.filenamesize=MAX_PATH;
		workspace.checksum=0;
		strcpy(workspace.filename,LastGame);

		checksum=0;
		for (i=0;i<sizeof(GameState);i++) checksum+=((L9BYTE*) &workspace)[i];
		workspace.checksum=checksum;

		if (os_save_file((L9BYTE*) &workspace,sizeof(workspace))) printstring("\rGame saved.\r");
		else printstring("\rUnable to save game.\r");
	}

	typedef struct
	{
		L9UINT32 Id;
		L9UINT16 codeptr,stackptr,listsize,stacksize,filenamesize,checksum;
		L9UINT16 vartable[256];
		L9BYTE listarea[LISTAREASIZE];
		L9UINT16 stack[STACKSIZE];
		char filename[MAX_PATH];
	} GameState;

	размер отгрузки: 4+6*2+256*2+0x800+1024*2+256 = 4+12+512+2048+2048+256 = 4880

	 */

    //TODO: см.ниже - +3 why???
    public byte[] getCloneInBytes(byte[] mem, int startmem) {
        short[] buff=new short[2+6+VARSIZE+(listsize/2)+L9.STACKSIZE+(256/2)];
        int i=0;
        int j;
        int i_checksum;
        int c_checksum;
        //Id
        buff[i++]=L9_ID&0xffff;
        buff[i++]=L9_ID>>16;
        //L9UINT16 codeptr
        buff[i++]=codeptr;
        //L9UINT16 stackptr
        buff[i++]=stackptr;
        //L9UINT16 listsize
        buff[i++]=listsize; //count in bytes. listsize
        //L9UINT16 stacksize
        buff[i++]=stacksize;
        //L9UINT16 filenamesize
        buff[i++]=(short)filename.length();
        //L9UINT16 checksum;
        i_checksum=i;
        i++;
        //L9UINT16 vartable[256];
        for (j=0;j<VARSIZE;j++) buff[i++]=vartable[j];
        //L9BYTE listarea[LISTAREASIZE];
        for (j=0;j<listsize/2;j++) buff[i++]=(short)((mem[startmem+j*2]&0xff)|((mem[startmem+j*2+1]&0xff)<<8));
        //L9UINT16 stack[STACKSIZE];
        for (j=0;j<L9.STACKSIZE;j++) buff[i++]=stack[j];
        //char filename[MAX_PATH];
        j=0;
        short sym;
        while (j<filename.length()) {
            sym=(short)(filename.charAt(j++)&0xff);
            if (j<filename.length()) {
                sym = (short) (sym | (((filename.charAt(j++))&0xff)<<8));
            };
            if (i<buff.length) buff[i++] = sym;
        }

        c_checksum=0;
        for (j=0;j<buff.length;j++) {
            c_checksum+=(buff[j]&0xff)+((buff[j]&0xff00)>>8);
        };
        checksum=(short)(c_checksum&0xffff);
        buff[i_checksum]=checksum;

        byte[] bytebuff=new byte[buff.length*2];
        for (j=0;j<i;j++) {
            bytebuff[j*2]=(byte)(buff[j]&0xff); bytebuff[j*2+1]=(byte)(buff[j]>>8);
        };
        return bytebuff;
    }

    //for save() - old version, not compatible with l9.net and level 9
    public byte[] getCloneInBytesV04(byte[] mem, int startmem) {
        short[] buff = new short[2+1+filename.length()+3+VARSIZE+1+L9.STACKSIZE+1+listsize/2+1];
        int i=0,j;
        buff[i++]=L9_ID>>16;
        buff[i++]=L9_ID&0xffff;
        buff[i++]=(short)filename.length();
        for (j=0; j<filename.length();j++)
            buff[i++]=(short)filename.charAt(j);
        buff[i++]=codeptr;
        buff[i++]=stackptr;
        buff[i++]=VARSIZE;
        for (j=0;j<VARSIZE;j++) buff[i++]=vartable[j];
        buff[i++]=stacksize;
        for (j=0;j<L9.STACKSIZE;j++) buff[i++]=stack[j];
        buff[i++]=listsize; //count in bytes. listsize
        for (j=0;j<listsize/2;j++) buff[i++]=(short)((mem[startmem+j*2]&0xff)|((mem[startmem+j*2+1]&0xff)<<8));
        checksum=0;
        for (j=0;j<i;j++) checksum+=buff[j];
        buff[i++]=checksum;
        byte bytebuff[]=new byte[buff.length*2];
        for (j=0;j<i;j++) {
            bytebuff[j*2]=(byte)(buff[j]&0xff); bytebuff[j*2+1]=(byte)(buff[j]>>8);
        };
        return bytebuff;
    }

    //for restore()
    public boolean setFromCloneInBytes(byte[] bytebuff, byte[] mem, int startmem) {
        int i=0,j=0,s,b;
        s=bytebuff.length;
        short[] buff = new short[s/2];
        while (j<s) {
            buff[i++]=(short)(bytebuff[j]&0xff|((bytebuff[j+1]&0xff)<<8));
            j+=2;
        }

        i=0;
        if (buff[i]==(L9_ID>>16) && (buff[i+1]==(L9_ID&0xffff))) {
            //
            //  old gamefile version (up to l9droid v0.4)
            //
            i+=2; //id
            s=buff[i++];
            filename="";
            for (j=0;j<s;j++) filename+=(char)buff[i++];
            //if (!name.equalsIgnoreCase(filename)) return false;
            codeptr=buff[i++];
            stackptr=buff[i++];
            if (buff[i++]!=VARSIZE) return false;
            for (j=0;j<VARSIZE;j++) vartable[j]=buff[i++];
            stacksize=buff[i++];
            for (j=0;j<L9.STACKSIZE;j++) stack[j]=buff[i++];
            listsize=buff[i++]; //count in bytes. listsize
            checksum=0;
            for (j=0;j<i+listsize/2;j++) checksum+=buff[j];
            if (buff[i+listsize/2]!=checksum) return false;
            for (j=0;j<listsize/2;j++) {
                b=buff[i++]&0xffff;
                mem[startmem+j*2]=(byte)(b&0xff);
                mem[startmem+j*2+1]=(byte)((b>>8)&0xff);
            };
            return true;
        } else {
            //
            //  l9.net and level9 compatible version
            //
            //Id
            if (buff[i++]!=(L9_ID&0xffff)) return false;
            if (buff[i++]!=(L9_ID>>16)) return false;
            //L9UINT16 codeptr
            codeptr=buff[i++];
            //L9UINT16 stackptr
            stackptr=buff[i++];
            //L9UINT16 listsize
            listsize=buff[i++];
            //L9UINT16 stacksize
            stacksize=buff[i++];
            //L9UINT16 filenamesize
            short filenamesize = buff[i++];
            //L9UINT16 checksum;
            short buff_checksum=buff[i];
            buff[i++]=0;
            checksum=0;
            for (j=0;j<buff.length;j++) checksum+=buff[j];

            int c_checksum=0;
            for (j=0;j<buff.length;j++) {
                c_checksum+=(buff[j]&0xff)+((buff[j]&0xff00)>>8);
            };
            checksum=(short)(c_checksum&0xffff);

            if (buff_checksum!=checksum) return false;

            //L9UINT16 vartable[256];
            for (j=0;j<VARSIZE;j++) vartable[j]=buff[i++];
            //L9BYTE listarea[LISTAREASIZE];
            for (j=0;j<listsize/2;j++) {
                b=buff[i++]&0xffff;
                mem[startmem+j*2]=(byte)(b&0xff);
                mem[startmem+j*2+1]=(byte)((b>>8)&0xff);
            };
            //L9UINT16 stack[STACKSIZE];
            for (j=0;j<L9.STACKSIZE;j++) stack[j]=buff[i++];
            //char filename[MAX_PATH];
            filename="";
            while (filenamesize>0) {
                b=buff[i++]&0xffff; 	if ((b&0xff)<32) break;
                filename+=(char)(b&0xff);
                b=(b>>8)&0xff;			if ((b&0xff)<32) break;
                if (--filenamesize>0) filename+=(char)b;
                filenamesize--;
            };
            //if (!name.equalsIgnoreCase(filename)) return false;

            return true;
        }
    }
}
