package fi.nls.hakunapi.core.util;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import org.junit.Test;

public class FixedFloatingPoint3FormatterIntTest {

    private final FixedFloatingPoint3FormatterInt formatter = FixedFloatingPoint3FormatterInt.INSTANCE;

    @Test
    public void testAlwaysThreeDecimals() {
        assertEquals("0.000", ordinate(0.0));
        assertEquals("1.000", ordinate(1.0));
        assertEquals("123.400", ordinate(123.4));
        assertEquals("123.450", ordinate(123.45));
        assertEquals("123.456", ordinate(123.456));
        assertEquals("500000.000", ordinate(500000.0));
        assertEquals("6822000.125", ordinate(6822000.125));
    }

    @Test
    public void testBelowOne() {
        // The variant this replaces got these wrong: too few integer digits to
        // shift a decimal point back into
        assertEquals("0.001", ordinate(0.001));
        assertEquals("0.010", ordinate(0.01));
        assertEquals("0.100", ordinate(0.1));
        assertEquals("0.500", ordinate(0.5));
        assertEquals("0.999", ordinate(0.999));
        assertEquals("-0.001", ordinate(-0.001));
        assertEquals("-0.500", ordinate(-0.5));
    }

    @Test
    public void testNegative() {
        assertEquals("-1.000", ordinate(-1.0));
        assertEquals("-123.456", ordinate(-123.456));
        assertEquals("-6822000.125", ordinate(-6822000.125));
    }

    @Test
    public void testNegativeZeroLosesItsSign() {
        // -0.0 < 0 is false, so the sign is dropped - same as DToA, and "-0.000"
        // would be noise in a coordinate
        assertEquals("0.000", ordinate(-0.0));
    }

    @Test
    public void testCarryIntoIntegerPart() {
        // 0.9999 rounds to 1.000, not 0.1000
        assertEquals("1.000", ordinate(0.9999));
        assertEquals("124.000", ordinate(123.9999));
        assertEquals("-1.000", ordinate(-0.9999));
    }

    @Test
    public void testRoundsHalfUpLikeBigDecimal() {
        Random random = new Random(42);
        for (int i = 0; i < 100_000; i++) {
            // Coordinate-shaped magnitudes: metres in a projected CRS
            double x = random.nextDouble(-10_000_000.0, 10_000_000.0);
            String expected = BigDecimal.valueOf(x).setScale(3, RoundingMode.HALF_UP).toPlainString();
            assertEquals(expected, ordinate(x));
        }
    }

    @Test
    public void testCharAndByteOutputAgree() {
        Random random = new Random(7);
        for (int i = 0; i < 10_000; i++) {
            double x = random.nextDouble(-10_000_000.0, 10_000_000.0);
            char[] arr = new char[32];
            int len = formatter.writeOrdinate(x, arr, 0);
            assertEquals(ordinate(x), new String(arr, 0, len));
        }
    }

    @Test
    public void testMaxDecimalsOrdinateCoversWorstCase() {
        assertEquals(3, formatter.maxDecimalsOrdinate());
    }

    @Test
    public void testNonFiniteFallsBackToDToA() {
        assertEquals("NaN", ordinate(Double.NaN));
        assertEquals("Infinity", ordinate(Double.POSITIVE_INFINITY));
        assertEquals("-Infinity", ordinate(Double.NEGATIVE_INFINITY));
    }

    @Test
    public void testFloatAndDoubleUnchangedFromDefault() {
        // Only writeOrdinate differs from DefaultFloatingPointFormatter
        DefaultFloatingPointFormatter reference = new DefaultFloatingPointFormatter(0, 5, 0, 8, 0, 3);
        assertEquals(reference.writeDouble(0.1257812), formatter.writeDouble(0.1257812));
        assertEquals(reference.writeFloat(0.1257812f), formatter.writeFloat(0.1257812f));
    }

    private String ordinate(double x) {
        byte[] buf = new byte[32];
        int len = formatter.writeOrdinate(x, buf, 0);
        return new String(buf, 0, len, StandardCharsets.US_ASCII);
    }

}
