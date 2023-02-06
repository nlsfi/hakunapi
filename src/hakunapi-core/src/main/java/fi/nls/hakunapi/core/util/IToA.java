package fi.nls.hakunapi.core.util;

import java.nio.charset.StandardCharsets;

/**
 * Modified version of Jackson NumberFormat
 * see https://github.com/FasterXML/jackson-core/blob/master/src/main/java/com/fasterxml/jackson/core/io/NumberInput.java
 */
public class IToA {

    private static final int[] CACHE;

    private static final int MILLION = 1000 * 1000;
    private static final int BILLION = 1000 * MILLION;

    private static final byte[] MIN_INT = Integer.toString(Integer.MIN_VALUE).getBytes(StandardCharsets.US_ASCII);
    private static final char[] MIN_INT_CH = Integer.toString(Integer.MIN_VALUE).toCharArray();
    private static final byte[] MIN_LONG = Long.toString(Long.MIN_VALUE).getBytes(StandardCharsets.US_ASCII);
    private static final char[] MIN_LONG_CH = Long.toString(Long.MIN_VALUE).toCharArray();

    static {
        CACHE = new int[1000];
        int off = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                for (int k = 0; k < 10; k++) {
                    CACHE[off++] = ('0' + i) << 16 | ('0' + j) << 8 | ('0' + k);
                }
            }
        }
    }

    public static int itoa(int v, byte[] b, int off) {
        if (v < 0) {
            if (v == Integer.MIN_VALUE) {
                int len = MIN_INT.length;
                System.arraycopy(MIN_INT, 0, b, off, len);
                return off + len;
            }
            b[off++] = '-';
            v = -v;
        }
        if (v < BILLION) {
            return uptoBillion(v, b, off);
        }
        v -= BILLION;
        if (v >= BILLION) {
            v -= BILLION;
            b[off++] = '2';
        } else {
            b[off++] = '1';
        }
        return overBillion(v, b, off);
    }

    public static int itoa(int v, char[] b, int off) {
        if (v < 0) {
            if (v == Integer.MIN_VALUE) {
                int len = MIN_INT_CH.length;
                System.arraycopy(MIN_INT_CH, 0, b, off, len);
                return off + len;
            }
            b[off++] = '-';
            v = -v;
        }
        if (v < BILLION) {
            return uptoBillion(v, b, off);
        }
        v -= BILLION;
        if (v >= BILLION) {
            v -= BILLION;
            b[off++] = '2';
        } else {
            b[off++] = '1';
        }
        return overBillion(v, b, off);
    }

    public static int ltoa(long v, byte[] b, int off) {
        if (v < 0L) {
            if (v >= Integer.MIN_VALUE) {
                return itoa((int) v, b, off);
            }
            if (v == Long.MIN_VALUE) {
                int len = MIN_LONG.length;
                System.arraycopy(MIN_LONG, 0, b, off, len);
                return off + len;
            }
            b[off++] = '-';
            v = -v;
        } else {
            if (v <= Integer.MAX_VALUE) {
                return itoa((int) v, b, off);
            }
        }

        long upper = v / BILLION;
        v -= upper * BILLION;

        if (upper < BILLION) {
            off = uptoBillion((int) upper, b, off);
        } else {
            long hi = upper / BILLION;
            upper -= hi * BILLION;
            off = noLeadingZeroes((int) hi, b, off);
            off = overBillion((int) upper, b, off);
        }
        return overBillion((int) v, b, off);
    }

    public static int ltoa(long v, char[] b, int off) {
        if (v < 0L) {
            if (v >= Integer.MIN_VALUE) {
                return itoa((int) v, b, off);
            }
            if (v == Long.MIN_VALUE) {
                int len = MIN_LONG_CH.length;
                System.arraycopy(MIN_LONG_CH, 0, b, off, len);
                return off + len;
            }
            b[off++] = '-';
            v = -v;
        } else {
            if (v <= Integer.MAX_VALUE) {
                return itoa((int) v, b, off);
            }
        }

        long upper = v / BILLION;
        v -= upper * BILLION;

        if (upper < BILLION) {
            off = uptoBillion((int) upper, b, off);
        } else {
            long hi = upper / BILLION;
            upper -= hi * BILLION;
            off = noLeadingZeroes((int) hi, b, off);
            off = overBillion((int) upper, b, off);
        }
        return overBillion((int) v, b, off);
    }

    private static int uptoBillion(int v, byte[] b, int off) {
        if (v < MILLION) { // at most 2 triplets...
            if (v < 1000) {
                return noLeadingZeroes(v, b, off);
            }
            int thousands = v / 1000;
            int remaining = v - thousands * 1000;
            off = noLeadingZeroes(thousands, b, off);
            return withLeadingZeroes(remaining, b, off);
        }
        int thousands = v / 1000;
        int remaining = v - thousands * 1000;
        int millions = thousands / 1000;
        thousands -= millions * 1000;
        off = noLeadingZeroes(millions, b, off); 
        off = withLeadingZeroes(thousands, b, off);
        return withLeadingZeroes(remaining, b, off);
    }

    private static int uptoBillion(int v, char[] b, int off) {
        if (v < MILLION) { // at most 2 triplets...
            if (v < 1000) {
                return noLeadingZeroes(v, b, off);
            }
            int thousands = v / 1000;
            int remaining = v - thousands * 1000;
            off = noLeadingZeroes(thousands, b, off);
            return withLeadingZeroes(remaining, b, off);
        }
        int thousands = v / 1000;
        int remaining = v - thousands * 1000;
        int millions = thousands / 1000;
        thousands -= millions * 1000;
        off = noLeadingZeroes(millions, b, off); 
        off = withLeadingZeroes(thousands, b, off);
        return withLeadingZeroes(remaining, b, off);
    }

    private static int overBillion(int v, byte[] b, int off) {
        int thousands = v / 1000;
        int remaining = v - thousands * 1000;
        int millions = thousands / 1000;
        thousands -= millions * 1000;
        off = withLeadingZeroes(millions, b, off); 
        off = withLeadingZeroes(thousands, b, off);
        return withLeadingZeroes(remaining, b, off);
    }

    private static int overBillion(int v, char[] b, int off) {
        int thousands = v / 1000;
        int remaining = v - thousands * 1000;
        int millions = thousands / 1000;
        thousands -= millions * 1000;
        off = withLeadingZeroes(millions, b, off); 
        off = withLeadingZeroes(thousands, b, off);
        return withLeadingZeroes(remaining, b, off);
    }

    private static int noLeadingZeroes(int v, byte[] b, int off) {
        int t = CACHE[v];
        if (v > 9) {
            if (v > 99) {
                b[off++] = (byte) (t >> 16); 
            }
            b[off++] = (byte) (t >> 8);
        }
        b[off++] = (byte) t;
        return off;
    }

    private static int noLeadingZeroes(int v, char[] b, int off) {
        int t = CACHE[v];
        if (v > 9) {
            if (v > 99) {
                b[off++] = (char) ((t >> 16) & 0xFF); 
            }
            b[off++] = (char) ((t >> 8) & 0xFF);
        }
        b[off++] = (char) (t & 0xFF);
        return off;
    }

    private static int withLeadingZeroes(int v, byte[] b, int off) {
        int t = CACHE[v];
        b[off++] = (byte) (t >> 16); 
        b[off++] = (byte) (t >> 8);
        b[off++] = (byte) t;
        return off;
    }

    private static int withLeadingZeroes(int v, char[] b, int off) {
        int t = CACHE[v];
        b[off++] = (char) ((t >> 16) & 0xFF); 
        b[off++] = (char) ((t >> 8) & 0xFF);
        b[off++] = (char) (t & 0xFF);
        return off;
    }

}
