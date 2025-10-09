package fi.nls.hakunapi.core.util;

import com.fasterxml.jackson.core.io.NumberOutput;


/**
 * @deprecated call NumberOutput functions directly
 */
@Deprecated
public class IToA {
    
    public static int itoa(int v, byte[] b, int off) {
        return NumberOutput.outputInt(v, b, off);
    }

    public static int itoa(int v, char[] b, int off) {
        return NumberOutput.outputInt(v, b, off);
    }

    public static int ltoa(long v, byte[] b, int off) {
        return NumberOutput.outputLong(v, b, off);
    }

    public static int ltoa(long v, char[] b, int off) {
        return NumberOutput.outputLong(v, b, off);
    }

}
