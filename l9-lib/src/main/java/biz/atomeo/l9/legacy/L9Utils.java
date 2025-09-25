package biz.atomeo.l9.legacy;

public class L9Utils {
    private L9Utils() {}

    public static char toupper(char c) {
        if (c>='a' && c<='z') c=(char)(c-32);
        return c;
    }

    public static char tolower(char c) {
        if (c>='A' && c<='Z') c=(char)(c+32);
        return c;
    }

    public static boolean isdigit(char c) {
        return (c>='0' && c<='9');
    };

    public static boolean isupper(char c) {
        return (c>='A' && c<='Z');
    };

    //compare buff to lowercase string, true if least #len# of chars equals.
    public static boolean stricmp(char[] buff,String str, int len) {
        if (len>buff.length) return false;
        for (int i=0;i<len;i++) {
            if (tolower(buff[i])!=str.charAt(i)) return false;
        }
        return true;
    };
    public static boolean stricmp(char[] buff,String str) {
        int len=str.length();
        return stricmp(buff,str,len);
    }

    public static boolean isalnum(char c) {
        return ((c>='a' && c<='z') || (c>='A' && c<='Z') || (c>='0' && c<='9'));
    }

    //returns NUM from array of chars, begins from index, or -1 if wrong rezult
    public static int sscanf(char[] ibuff, int index) {
        int num = -1;
        int n;
        for (int i=index;i<ibuff.length;i++) {
            n=ibuff[i]-'0';
            if (n>=0 && n<=9) num=(num<0?0:(num*10))+n;
            else {
                if (num>=0) break;
            };
        };
        return num;
    };

    //for now returns 1 if string s found in array c, 0 otherwise
    public static int strstr(char[] c, String s) {
        int i=0;
        int j;
        int sl=s.length();
        int cl=c.length;
        int rez=0;
        while (i<cl-sl) {
            rez=1;
            for (j=0;j<sl;j++)
                if (toupper(c[i+j])!=s.charAt(j)) {
                    rez=0;
                    break;
                };
            if (rez==1) break;
            i++;
        }
        return rez;
    }
}
