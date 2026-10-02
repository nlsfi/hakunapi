package fi.nls.hakunapi.core.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import tools.jackson.core.io.NumberOutput;

/**
 * The byte[] variants store whole 8-byte words, so they may write up to 18
 * bytes from off whatever offset they return. The
 * caller must have that much room, or the full written length if longer.
 */
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

    // Values below SWAR_LIMIT with at most SWAR_MAX_DECIMALS decimals are written
    // eight digits per 64-bit word (see digits8) instead of a digit at a time.
    // Every real coordinate qualifies - TM35FIN, ETRS-GKn, EPSG:3857, degrees.
    // NaN, Infinity and larger values fail the comparison and take the loop path
    private static final double SWAR_LIMIT = 1e8;
    private static final int SWAR_MAX_DECIMALS = 8;

    private static final VarHandle LONG_LE = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN);
    private static final long ASCII_ZEROS = 0x3030303030303030L;

    public static int ftoa(float v, byte[] b, int off, int minDecimals, int maxDecimals) {
        if (!(Math.abs(v) < SWAR_LIMIT) || maxDecimals > SWAR_MAX_DECIMALS || minDecimals > maxDecimals) {
            return ftoaLoop(v, b, off, minDecimals, maxDecimals);
        }
        if (v < 0) {
            b[off++] = '-';
            v = -v;
        }
        long l = (long) v;
        long exp = powerOfTen[maxDecimals];
        long decimal = (long) ((v - l) * exp + 0.5);
        if (decimal == exp) {
            decimal = 0;
            l++;
        }
        off = writeIntegral(l, b, off);
        return writeFraction(decimal, b, off, minDecimals, maxDecimals);
    }

    public static int dtoa(double v, byte[] b, int off, int minDecimals, int maxDecimals) {
        if (!(Math.abs(v) < SWAR_LIMIT) || maxDecimals > SWAR_MAX_DECIMALS || minDecimals > maxDecimals) {
            return dtoaLoop(v, b, off, minDecimals, maxDecimals);
        }
        if (v < 0) {
            b[off++] = '-';
            v = -v;
        }
        long l = (long) v;
        long exp = powerOfTen[maxDecimals];
        long decimal = (long) ((v - l) * exp + 0.5);
        if (decimal == exp) {
            decimal = 0;
            l++;
        }
        off = writeIntegral(l, b, off);
        return writeFraction(decimal, b, off, minDecimals, maxDecimals);
    }

    private static int ftoaLoop(float v, byte[] b, int off, int minDecimals, int maxDecimals) {
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

    private static int dtoaLoop(double v, byte[] b, int off, int minDecimals, int maxDecimals) {
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
        for (int i = maxDecimals - 1; i >= minDecimals; i--) {
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
        for (int i = maxDecimals - 1; i >= minDecimals; i--) {
            if (b[start + i] != '0') {
                break;
            }
            off--;
        }
        return off;
    }

    private static int writeIntegral(long l, byte[] b, int off) {
        if (l >= 100_000_000L) {
            // Only when the fraction rounds 99999999.9... up to the next integer
            return NumberOutput.outputLong(l, b, off);
        }
        return writeIntegral8(digits8(l), b, off);
    }

    // d holds the eight digits of an integer, most significant in the lowest byte
    private static int writeIntegral8(long d, byte[] b, int off) {
        // Leading zero digits are the low zero bytes, keep at least one digit for 0
        int lz = Math.min(Long.numberOfTrailingZeros(d) >>> 3, 7);
        LONG_LE.set(b, off, (d + ASCII_ZEROS) >>> (lz << 3));
        return off + 8 - lz;
    }

    private static int writeFraction(long decimal, byte[] b, int off, int minDecimals, int maxDecimals) {
        if (decimal == 0) {
            return writeZeroFraction(b, off, minDecimals);
        }
        return writeFraction8(digits8(decimal * powerOfTen[SWAR_MAX_DECIMALS - maxDecimals]), b, off, minDecimals);
    }

    private static int writeZeroFraction(byte[] b, int off, int minDecimals) {
        if (minDecimals > 0) {
            b[off++] = '.';
            for (int j = 0; j < minDecimals; j++) {
                b[off++] = '0';
            }
        }
        return off;
    }

    // d holds a non-zero fraction scaled to eight decimals, the first in the lowest byte
    private static int writeFraction8(long d, byte[] b, int off, int minDecimals) {
        b[off++] = '.';
        // Trailing zero decimals are the high zero bytes
        int decimals = Math.max(8 - (Long.numberOfLeadingZeros(d) >>> 3), minDecimals);
        LONG_LE.set(b, off, d + ASCII_ZEROS);
        return off + decimals;
    }

    private static long digits8(long n) {
        return digits8(n / 10_000, n % 10_000);
    }

    // The eight decimal digits of hi4 * 10^4 + lo4 (both below 10^4), one per
    // byte, the most significant in the lowest byte, '0' not yet added.
    // SWAR: the halves go into 32-bit lanes, each lane splits into 16-bit lanes
    // of /100 and %100, those into bytes of /10 and %10 - each division by a
    // constant done as a multiply and shift, in every lane at once
    private static long digits8(long hi4, long lo4) {
        long merged = hi4 | (lo4 << 32);
        long top = ((merged * 10486L) >>> 20) & ((0x7FL << 32) | 0x7FL);
        long bot = merged - 100L * top;
        long hundreds = (bot << 16) + top;
        long tens = (hundreds * 103L) >>> 10;
        tens &= (0xFL << 48) | (0xFL << 32) | (0xFL << 16) | 0xFL;
        tens += (hundreds - 10L * tens) << 8;
        return tens;
    }

}
