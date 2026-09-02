package fi.nls.hakunapi.core.util;

import java.nio.charset.StandardCharsets;

import fi.nls.hakunapi.core.FloatingPointFormatter;

import tools.jackson.core.io.NumberOutput;

/**
 * {@link FloatingPointFormatter} that writes ordinates with a fixed three
 * decimals, intended for projected coordinate reference systems where a
 * millimetre is already more precision than the data carries.
 *
 * Unlike {@link DefaultFloatingPointFormatter} the fraction is not produced by
 * walking the decimal expansion: the ordinate is split into its integer part
 * and its thousandths, and the three fraction digits are read from a
 * precomputed table. Trailing zeros are therefore never trimmed - 123.4 is
 * written as "123.400". Non-ordinate floats and doubles still go through
 * {@link DToA}.
 */
public class FixedFloatingPoint3FormatterInt implements FloatingPointFormatter {

    public static final FixedFloatingPoint3FormatterInt INSTANCE = new FixedFloatingPoint3FormatterInt(0, 5, 0, 8);

    private static final int DECIMALS_ORDINATE = 3;

    // Three ASCII digits of 000..999, packed one byte per digit
    private static final int[] DIGIT_TRIPLETS = new int[1000];
    static {
        for (int i = 0; i < DIGIT_TRIPLETS.length; i++) {
            DIGIT_TRIPLETS[i] = ('0' + i / 100) << 16 | ('0' + i / 10 % 10) << 8 | ('0' + i % 10);
        }
    }

    private final int minDecimalsFloat;
    private final int maxDecimalsFloat;
    private final int minDecimalsDouble;
    private final int maxDecimalsDouble;
    private final byte[] buf;

    public FixedFloatingPoint3FormatterInt(int minDecimalsFloat, int maxDecimalsFloat,
            int minDecimalsDouble, int maxDecimalsDouble) {
        this.minDecimalsFloat = minDecimalsFloat;
        this.maxDecimalsFloat = maxDecimalsFloat;
        this.minDecimalsDouble = minDecimalsDouble;
        this.maxDecimalsDouble = maxDecimalsDouble;
        this.buf = new byte[32];
    }

    @Override
    public int maxDecimalsFloat() {
        return maxDecimalsFloat;
    }

    @Override
    public int maxDecimalsDouble() {
        return maxDecimalsDouble;
    }

    @Override
    public int maxDecimalsOrdinate() {
        return DECIMALS_ORDINATE;
    }

    @Override
    public int writeFloat(float f, byte[] b, int off) {
        return DToA.ftoa(f, b, off, minDecimalsFloat, maxDecimalsFloat);
    }

    @Override
    public int writeDouble(double d, byte[] b, int off) {
        return DToA.dtoa(d, b, off, minDecimalsDouble, maxDecimalsDouble);
    }

    @Override
    public int writeOrdinate(double x, byte[] b, int off) {
        if (Double.isNaN(x) || Double.isInfinite(x)) {
            return DToA.dtoa(x, b, off, 0, DECIMALS_ORDINATE);
        }
        if (x < 0) {
            b[off++] = '-';
            x = -x;
        }
        long integral = (long) x;
        int thousandths = (int) ((x - integral) * 1000.0 + 0.5);
        if (thousandths >= 1000) {
            thousandths = 0;
            integral++;
        }
        off = NumberOutput.outputLong(integral, b, off);
        b[off++] = '.';
        int digits = DIGIT_TRIPLETS[thousandths];
        b[off++] = (byte) (digits >> 16);
        b[off++] = (byte) (digits >> 8);
        b[off++] = (byte) digits;
        return off;
    }

    @Override
    public int writeFloat(float f, char[] arr, int off) {
        return DToA.ftoa(f, arr, off, minDecimalsFloat, maxDecimalsFloat);
    }

    @Override
    public int writeDouble(double d, char[] arr, int off) {
        return DToA.dtoa(d, arr, off, minDecimalsDouble, maxDecimalsDouble);
    }

    @Override
    public int writeOrdinate(double x, char[] arr, int off) {
        if (Double.isNaN(x) || Double.isInfinite(x)) {
            return DToA.dtoa(x, arr, off, 0, DECIMALS_ORDINATE);
        }
        if (x < 0) {
            arr[off++] = '-';
            x = -x;
        }
        long integral = (long) x;
        int thousandths = (int) ((x - integral) * 1000.0 + 0.5);
        if (thousandths >= 1000) {
            thousandths = 0;
            integral++;
        }
        off = NumberOutput.outputLong(integral, arr, off);
        arr[off++] = '.';
        int digits = DIGIT_TRIPLETS[thousandths];
        arr[off++] = (char) (digits >> 16);
        arr[off++] = (char) ((digits >> 8) & 0xFF);
        arr[off++] = (char) (digits & 0xFF);
        return off;
    }

    @Override
    public String writeFloat(float f) {
        int len = writeFloat(f, buf, 0);
        return new String(buf, 0, len, StandardCharsets.US_ASCII);
    }

    @Override
    public String writeDouble(double d) {
        int len = writeDouble(d, buf, 0);
        return new String(buf, 0, len, StandardCharsets.US_ASCII);
    }

    @Override
    public String writeOrdinate(double x) {
        int len = writeOrdinate(x, buf, 0);
        return new String(buf, 0, len, StandardCharsets.US_ASCII);
    }

}
