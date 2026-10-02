package fi.nls.hakunapi.core.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import tools.jackson.core.io.NumberOutput;

/**
 * The byte[] variants store whole 8-byte words, so they may write up to 18
 * bytes from off (37 for the x, y variant) whatever offset they return. The
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
    private static final VarHandle INT_LE = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN);
    private static final long ASCII_ZEROS = 0x3030303030303030L;
    private static final long LOW_LANE = 0xFFFFFFFFL;

    // Below CARRY_SAFE_LIMIT an integral part stays under 10^8 even after its
    // fraction rounds up into it; dtoaMax7 packs two into one word, so there
    // they must stay below 10^4
    private static final double CARRY_SAFE_LIMIT = 99_999_999;
    private static final double PACKED_INTEGRAL_LIMIT = 9999;

    // The fraction of a value with at most three decimals, indexed by the
    // rounded thousandths: '.' and the digits, trailing zeros trimmed, in the low
    // four bytes and how many of those bytes count in the high half. 0 -> nothing
    private static final long[] FRACTION3 = new long[1000];
    static {
        for (int i = 1; i < 1000; i++) {
            int decimals = 3;
            for (int v = i; v % 10 == 0; v /= 10) {
                decimals--;
            }
            long ascii = '.';
            for (int k = 0; k < decimals; k++) {
                long digit = '0' + i / (int) powerOfTen[2 - k] % 10;
                ascii |= digit << (8 * (k + 1));
            }
            FRACTION3[i] = ascii | ((long) (decimals + 1) << 32);
        }
    }

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

    /**
     * Writes x, separator and y - the same bytes as two dtoa calls, but two
     * digit groups short enough to share a 64-bit word share one conversion:
     * both integral parts when below 10^4 (degrees), both fractions when
     * maxDecimals is at most 4 (metres).
     */
    public static int dtoa(double x, double y, byte separator, byte[] b, int off, int minDecimals, int maxDecimals) {
        if (!(Math.abs(x) < SWAR_LIMIT) || !(Math.abs(y) < SWAR_LIMIT)
                || maxDecimals > SWAR_MAX_DECIMALS || minDecimals > maxDecimals) {
            off = dtoa(x, b, off, minDecimals, maxDecimals);
            b[off++] = separator;
            return dtoa(y, b, off, minDecimals, maxDecimals);
        }
        boolean negX = x < 0;
        boolean negY = y < 0;
        if (negX) {
            x = -x;
        }
        if (negY) {
            y = -y;
        }
        long exp = powerOfTen[maxDecimals];
        long lx = (long) x;
        long fx = (long) ((x - lx) * exp + 0.5);
        if (fx == exp) {
            fx = 0;
            lx++;
        }
        long ly = (long) y;
        long fy = (long) ((y - ly) * exp + 0.5);
        if (fy == exp) {
            fy = 0;
            ly++;
        }

        boolean packIntegrals = (lx | ly) < 10_000;
        long integrals = packIntegrals ? digits8(lx, ly) : 0;

        // Fraction words hold the first decimal in the lowest byte
        long fractionX;
        long fractionY;
        if (maxDecimals <= 4) {
            long scale = powerOfTen[4 - maxDecimals];
            long fractions = digits8(fx * scale, fy * scale);
            fractionX = fractions & LOW_LANE;
            fractionY = fractions >>> 32;
        } else {
            long scale = powerOfTen[8 - maxDecimals];
            fractionX = digits8(fx * scale);
            fractionY = digits8(fy * scale);
        }

        if (negX) {
            b[off++] = '-';
        }
        // A packed integral sits in one lane; moved to the high lane it reads as
        // eight digits with four leading zeros
        off = packIntegrals ? writeIntegral8(integrals << 32, b, off) : writeIntegral(lx, b, off);
        off = fx == 0 ? writeZeroFraction(b, off, minDecimals) : writeFraction8(fractionX, b, off, minDecimals);
        b[off++] = separator;
        if (negY) {
            b[off++] = '-';
        }
        off = packIntegrals ? writeIntegral8(integrals & ~LOW_LANE, b, off) : writeIntegral(ly, b, off);
        return fy == 0 ? writeZeroFraction(b, off, minDecimals) : writeFraction8(fractionY, b, off, minDecimals);
    }

    /**
     * dtoa(v, b, off, 0, 3) - the default metre formatter - with the decimals as
     * constants and the fraction read from a table.
     */
    public static int dtoaMax3(double v, byte[] b, int off) {
        if (!(Math.abs(v) < CARRY_SAFE_LIMIT)) {
            return dtoa(v, b, off, 0, 3);
        }
        return writeMax3(v, b, off);
    }

    /**
     * dtoa(x, y, separator, b, off, 0, 3); two short fractions gain nothing from
     * sharing a word once each is a single table read.
     */
    public static int dtoaMax3(double x, double y, byte separator, byte[] b, int off) {
        if (!(Math.abs(x) < CARRY_SAFE_LIMIT) | !(Math.abs(y) < CARRY_SAFE_LIMIT)) {
            return dtoa(x, y, separator, b, off, 0, 3);
        }
        off = writeMax3(x, b, off);
        b[off++] = separator;
        return writeMax3(y, b, off);
    }

    // |v| < CARRY_SAFE_LIMIT, checked by the caller. Kept free of rare branches
    // and calls: HakunaJsonWriter.writeCoordinate inlines two of these, and C2
    // only inlines that into a ring loop while its code stays small
    private static int writeMax3(double v, byte[] b, int off) {
        // Branch-free sign: '-' is always stored and kept only when the sign bit
        // is set. Adding 0.0 turns -0.0 into 0.0 first, so it writes "0" like
        // the general path (v < 0 is false for -0.0)
        v += 0.0;
        b[off] = '-';
        off += (int) (Double.doubleToRawLongBits(v) >>> 63);
        v = Math.abs(v);
        long l = (long) v;
        long decimal = (long) ((v - l) * 1000 + 0.5);
        if (decimal == 1000) {
            decimal = 0;
            l++;
        }
        off = writeIntegral8(digits8(l), b, off);
        long fraction = FRACTION3[(int) decimal];
        INT_LE.set(b, off, (int) fraction);
        return off + (int) (fraction >>> 32);
    }

    /**
     * dtoa(x, y, separator, b, off, 0, 7) - the default degree formatter - with
     * the decimals as constants; the integral parts always share one word, as
     * any value outside +-9999 takes the general path.
     */
    public static int dtoaMax7(double x, double y, byte separator, byte[] b, int off) {
        if (!(Math.abs(x) < PACKED_INTEGRAL_LIMIT) | !(Math.abs(y) < PACKED_INTEGRAL_LIMIT)) {
            return dtoa(x, y, separator, b, off, 0, 7);
        }
        boolean negX = x < 0;
        boolean negY = y < 0;
        if (negX) {
            x = -x;
        }
        if (negY) {
            y = -y;
        }
        long lx = (long) x;
        long fx = (long) ((x - lx) * 10_000_000 + 0.5);
        if (fx == 10_000_000) {
            fx = 0;
            lx++;
        }
        long ly = (long) y;
        long fy = (long) ((y - ly) * 10_000_000 + 0.5);
        if (fy == 10_000_000) {
            fy = 0;
            ly++;
        }
        long integrals = digits8(lx, ly);
        long fractionX = digits8(fx * 10);
        long fractionY = digits8(fy * 10);

        if (negX) {
            b[off++] = '-';
        }
        off = writeIntegral8(integrals << 32, b, off);
        if (fx != 0) {
            off = writeFraction8(fractionX, b, off, 0);
        }
        b[off++] = separator;
        if (negY) {
            b[off++] = '-';
        }
        off = writeIntegral8(integrals & ~LOW_LANE, b, off);
        return fy == 0 ? off : writeFraction8(fractionY, b, off, 0);
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
        // Leading zero digits are the low zero bytes; the bit set in the last
        // byte keeps at least one digit for 0
        int lz = Long.numberOfTrailingZeros(d | (1L << 56)) >>> 3;
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
        // n / 10_000 as a multiply and shift, exact for every n below 10^8
        long hi = (n * 109_951_163L) >>> 40;
        return digits8(hi, n - hi * 10_000);
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
