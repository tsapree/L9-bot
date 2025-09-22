package biz.atomeo.l9.legacy;

public class SaveStruct {
    short[] vartable;
    byte[] listarea;
    SaveStruct() {
        vartable=new short[256];
        listarea=new byte[L9.LISTAREASIZE];
    }
}
