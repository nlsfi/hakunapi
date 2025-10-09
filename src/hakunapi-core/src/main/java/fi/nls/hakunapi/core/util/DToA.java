package fi.nls.hakunapi.core.util;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.io.NumberOutput;

public class DToA {

    private static final byte[] NAN = { 'N', 'a', 'N' };
    private static final byte[] INF = { 'I', 'n', 'f', 'i', 'n', 'i', 't', 'y' };
    private static final char[] NAN_CH = { 'N', 'a', 'N' };
    private static final char[] INF_CH = { 'I', 'n', 'f', 'i', 'n', 'i', 't', 'y' };

    private static final long[] powerOfTen = {
            1L,
            10L,
            100L,
            1000L,
            10000L,
            100000L,
            1000000L,
            10000000L,
            100000000L,
            1000000000L,
            10000000000L,
            100000000000L,
            1000000000000L,
            10000000000000L,
            100000000000000L,
            1000000000000000L
    };

    public static int ftoa(float v, byte[] b, int off, int minDecimals, int maxDecimals) {
        if (Float.isNaN(v)) {
            System.arraycopy(NAN, 0, b, off, NAN.length);
            return off + NAN.length;
        }
        if (Float.isInfinite(v)) {
            if (v < 0) {
                b[off++] = '-';
            }
            System.arraycopy(INF, 0, b, off, INF.length);
            return off + INF.length;
        }
        if (v < 0) {
            b[off++] = '-';
            v = -v;
        }
        if (v > Long.MAX_VALUE) {
            // This method won't work with really big numbers
            byte[] tmp = Float.toString(v).getBytes(StandardCharsets.US_ASCII);
            System.arraycopy(tmp, 0, b, off, tmp.length);
            return off + tmp.length;
        }
        long l = (long) v;
        long exp = powerOfTen[maxDecimals];
        long decimal = (long) ((v - l) * exp + 0.5);
        if (decimal == exp) {
            decimal = 0;
            l++;
        }
        off = NumberOutput.outputLong(l, b, off);
        if (decimal == 0) {
            if (minDecimals > 0) {
                b[off++] = '.';
                for (int j = 0; j < minDecimals; j++) {
                    b[off++] = '0';
                }
            }
            return off;
        } else {
            b[off++] = '.';
            return addDecimalPart(decimal, b, off, minDecimals, maxDecimals);
        }
    }

    public static int ftoa(float v, char[] b, int off, int minDecimals, int maxDecimals) {
        if (Float.isNaN(v)) {
            System.arraycopy(NAN, 0, b, off, NAN.length);
            return off + NAN.length;
        }
        if (Float.isInfinite(v)) {
            if (v < 0) {
                b[off++] = '-';
            }
            System.arraycopy(INF, 0, b, off, INF.length);
            return off + INF.length;
        }
        if (v < 0) {
            b[off++] = '-';
            v = -v;
        }
        if (v > Long.MAX_VALUE) {
            // This method won't work with really big numbers
            byte[] tmp = Float.toString(v).getBytes(StandardCharsets.US_ASCII);
            System.arraycopy(tmp, 0, b, off, tmp.length);
            return off + tmp.length;
        }
        long l = (long) v;
        long exp = powerOfTen[maxDecimals];
        long decimal = (long) ((v - l) * exp + 0.5);
        if (decimal == exp) {
            decimal = 0;
            l++;
        }
        off = NumberOutput.outputLong(l, b, off);
        if (decimal == 0) {
            if (minDecimals > 0) {
                b[off++] = '.';
                for (int j = 0; j < minDecimals; j++) {
                    b[off++] = '0';
                }
            }
            return off;
        } else {
            b[off++] = '.';
            return addDecimalPart(decimal, b, off, minDecimals, maxDecimals);
        }
    }

    public static int dtoa(double v, byte[] b, int off, int minDecimals, int maxDecimals) {
        if (Double.isNaN(v)) {
            System.arraycopy(NAN, 0, b, off, NAN.length);
            return off + NAN.length;
        }
        if (Double.isInfinite(v)) {
            if (v < 0) {
                b[off++] = '-';
            }
            System.arraycopy(INF, 0, b, off, INF.length);
            return off + INF.length;
        }
        if (v < 0) {
            b[off++] = '-';
            v = -v;
        }
        if (v > Long.MAX_VALUE) {
            // This method won't work with really big numbers
            byte[] tmp = Double.toString(v).getBytes(StandardCharsets.US_ASCII);
            System.arraycopy(tmp, 0, b, off, tmp.length);
            return off + tmp.length;
        }
        long l = (long) v;
        long exp = powerOfTen[maxDecimals];
        long decimal = (long) ((v - l) * exp + 0.5);
        if (decimal == exp) {
            decimal = 0;
            l++;
        }
        off = NumberOutput.outputLong(l, b, off);
        if (decimal == 0) {
            if (minDecimals > 0) {
                b[off++] = '.';
                for (int j = 0; j < minDecimals; j++) {
                    b[off++] = '0';
                }
            }
            return off;
        } else {
            b[off++] = '.';
            return addDecimalPart(decimal, b, off, minDecimals, maxDecimals);
        }
    }

    public static int dtoa(double v, char[] b, int off, int minDecimals, int maxDecimals) {
        if (Double.isNaN(v)) {
            System.arraycopy(NAN_CH, 0, b, off, NAN_CH.length);
            return off + NAN_CH.length;
        }
        if (Double.isInfinite(v)) {
            if (v < 0) {
                b[off++] = '-';
            }
            System.arraycopy(INF_CH, 0, b, off, INF_CH.length);
            return off + INF_CH.length;
        }
        if (v < 0) {
            b[off++] = '-';
            v = -v;
        }
        if (v > Long.MAX_VALUE) {
            // This method won't work with really big numbers
            byte[] tmp = Double.toString(v).getBytes(StandardCharsets.US_ASCII);
            System.arraycopy(tmp, 0, b, off, tmp.length);
            return off + tmp.length;
        }
        long l = (long) v;
        long exp = powerOfTen[maxDecimals];
        long decimal = (long) ((v - l) * exp + 0.5);
        if (decimal == exp) {
            decimal = 0;
            l++;
        }
        off = NumberOutput.outputLong(l, b, off);
        if (decimal == 0) {
            if (minDecimals > 0) {
                b[off++] = '.';
                for (int j = 0; j < minDecimals; j++) {
                    b[off++] = '0';
                }
            }
            return off;
        } else {
            b[off++] = '.';
            return addDecimalPart(decimal, b, off, minDecimals, maxDecimals);
        }
    }

    private static int addDecimalPart(final long value, final byte[] b, int off, final int minDecimals, final int maxDecimals) {
        int start = off;

        // Leading zeroes
        for (int d = maxDecimals - 1; d > 0 && value < powerOfTen[d]; d--) {
            b[off++] = '0';
        }

        off = NumberOutput.outputLong(value, b, off);

        int decimals = off - start;

        // Trailing zeroes
        for (; decimals < minDecimals; decimals++) {
            b[off++] = '0';
        }
        for (int i = maxDecimals - 1; i > minDecimals; i--) {
            if (b[start + i] != '0') {
                break;
            }
            off--;
        }
        return off;
    }

    private static int addDecimalPart(final long value, final char[] b, int off, final int minDecimals, final int maxDecimals) {
        int start = off;

        // Leading zeroes
        for (int d = maxDecimals - 1; d > 0 && value < powerOfTen[d]; d--) {
            b[off++] = '0';
        }

        off = NumberOutput.outputLong(value, b, off);

        int decimals = off - start;

        // Trailing zeroes
        for (; decimals < minDecimals; decimals++) {
            b[off++] = '0';
        }
        for (int i = maxDecimals - 1; i > minDecimals; i--) {
            if (b[start + i] != '0') {
                break;
            }
            off--;
        }
        return off;
    }

}
