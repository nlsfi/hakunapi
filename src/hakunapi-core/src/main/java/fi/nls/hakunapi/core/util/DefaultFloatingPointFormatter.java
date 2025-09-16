package fi.nls.hakunapi.core.util;

import java.nio.charset.StandardCharsets;

import fi.nls.hakunapi.core.FloatingPointFormatter;

public class DefaultFloatingPointFormatter implements FloatingPointFormatter {
    
    public static final DefaultFloatingPointFormatter DEFAULT_DEGREES = new DefaultFloatingPointFormatter(0, 5, 0, 8, 0, 7);
    public static final DefaultFloatingPointFormatter DEFAULT_METERS = new DefaultFloatingPointFormatter(0, 5, 0, 8, 0, 3);

    private final int minDecimalsFloat;
    private final int maxDecimalsFloat;
    private final int minDecimalsDouble;
    private final int maxDecimalsDouble;
    private final int minDecimalsOrdinate;
    private final int maxDecimalsOrdinate;
    private final byte[] buf;
    
    public DefaultFloatingPointFormatter(int minDecimalsFloat, int maxDecimalsFloat,
            int minDecimalsDouble, int maxDecimalsDouble,
            int minDecimalsOrdinate, int maxDecimalsOrdinate) {
        this.minDecimalsFloat = minDecimalsFloat;
        this.maxDecimalsFloat = maxDecimalsFloat;
        this.minDecimalsDouble = minDecimalsDouble;
        this.maxDecimalsDouble = maxDecimalsDouble;
        this.minDecimalsOrdinate = minDecimalsOrdinate;
        this.maxDecimalsOrdinate = maxDecimalsOrdinate;
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
        return maxDecimalsOrdinate;
    }

    @Override
    public int writeFloat(float f, byte[] b, int off) {
        return DToA.ftoa(f, b, off, minDecimalsFloat, maxDecimalsFloat);
    }

    @Override
    public int writeDouble(double v, byte[] b, int off) {
        return DToA.dtoa(v, b, off, minDecimalsDouble, maxDecimalsDouble);
    }

    @Override
    public int writeOrdinate(double x, byte[] b, int off) {
        return DToA.dtoa(x, b, off, minDecimalsOrdinate, maxDecimalsOrdinate);
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
        return DToA.dtoa(x, arr, off, minDecimalsOrdinate, maxDecimalsOrdinate);
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
