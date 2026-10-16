package fi.nls.hakunapi.core.util;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

import org.junit.Test;

import fi.nls.hakunapi.core.util.DToA;

public class DToATest {
    
    @Test
    public void test() {
        int minDecimals = 1;
        int maxDecimals = 5;

        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        df.applyPattern("##0.#####");

        float f = 0.1257812f;
        double d = 0.1257812;
        assertEquals(df.format(f), format(f, minDecimals, maxDecimals));
        assertEquals(df.format(d), format(d, minDecimals, maxDecimals));
        assertEquals("0.12578", df.format(f));
        assertEquals("0.12578", df.format(d));
        
        maxDecimals = 2;
        df.applyPattern("###.0#");
        
        f = 4.00f;
        d = 4.00;
        assertEquals(df.format(f), format(f, minDecimals, maxDecimals));
        assertEquals(df.format(d), format(d, minDecimals, maxDecimals));
        assertEquals("4.0", df.format(f));
        assertEquals("4.0", df.format(d));

        f = 4.0001f;
        d = 4.0001;
        assertEquals(df.format(f), format(f, minDecimals, maxDecimals));
        assertEquals(df.format(d), format(d, minDecimals, maxDecimals));
        assertEquals("4.0", df.format(f));
        assertEquals("4.0", df.format(d));
        
        f = 3.99f;
        d = 3.99;
        assertEquals(df.format(f), format(f, minDecimals, maxDecimals));
        assertEquals(df.format(d), format(d, minDecimals, maxDecimals));
        assertEquals("3.99", df.format(f));
        assertEquals("3.99", df.format(d));
        
        f = 3.995f;
        d = 3.995;
        assertEquals("3.99", format(f, minDecimals, maxDecimals));
        assertEquals("4.0", format(d, minDecimals, maxDecimals));
        
        minDecimals = 3;
        maxDecimals = 3;
        f = 4.32f;
        d = 4.32;
        assertEquals("4.320", format(f, minDecimals, maxDecimals));
        assertEquals("4.320", format(d, minDecimals, maxDecimals));
    }
    
    @Test
    public void testTrimsDownToMinDecimals() {
        assertEquals("0.5", format(0.5, 1, 2));
        assertEquals("0.5", formatBytes(0.5, 1, 2));
        assertEquals("0.5", format(0.5f, 1, 2));
        assertEquals("1.25", formatBytes(1.25, 2, 4));
        assertEquals("1.50", formatBytes(1.5, 2, 4));
        assertEquals("1.000", formatBytes(1.0, 3, 5));
        // Above the SWAR limit, through the loop path
        assertEquals("123456789.5", formatBytes(123456789.5, 1, 2));
        assertEquals("0.5", formatBytes(0.5, 1, 12));
    }

    @Test
    public void testMatchesReference() {
        Random r = new Random(1);
        double[] edges = {
                0.0, -0.0, 0.5, -0.5, 0.05, 1e-9, -1e-9, 0.99999999, -0.99999999,
                99999999.4, 99999999.5, 99999999.99999999, 1e8, -1e8, 123456789.123, 1e15, 1e19, -1e19,
                6822000.125, 25499999.9995, 180.0, -180.0, 0.0005, 2.0005,
                Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.MAX_VALUE, Double.MIN_VALUE
        };
        for (int maxDecimals = 0; maxDecimals <= 15; maxDecimals++) {
            for (int minDecimals = 0; minDecimals <= maxDecimals; minDecimals++) {
                for (double d : edges) {
                    assertEquals(reference(d, minDecimals, maxDecimals), formatBytes(d, minDecimals, maxDecimals));
                    assertEquals(reference((float) d, minDecimals, maxDecimals), formatBytes((float) d, minDecimals, maxDecimals));
                }
                for (int i = 0; i < 20_000; i++) {
                    double d = randomOrdinate(r, maxDecimals);
                    assertEquals(reference(d, minDecimals, maxDecimals), formatBytes(d, minDecimals, maxDecimals));
                    float f = (float) d;
                    assertEquals(reference(f, minDecimals, maxDecimals), formatBytes(f, minDecimals, maxDecimals));
                }
            }
        }
    }

    @Test
    public void testPairMatchesTwoOrdinates() {
        Random r = new Random(2);
        byte[] buf = new byte[128];
        for (int maxDecimals = 0; maxDecimals <= 10; maxDecimals++) {
            for (int minDecimals = 0; minDecimals <= maxDecimals; minDecimals++) {
                for (int i = 0; i < 20_000; i++) {
                    double x = randomOrdinate(r, maxDecimals);
                    double y = randomOrdinate(r, maxDecimals);
                    String expected = formatBytes(x, minDecimals, maxDecimals) + "," + formatBytes(y, minDecimals, maxDecimals);
                    int n = DToA.dtoa(x, y, (byte) ',', buf, 0, minDecimals, maxDecimals);
                    assertEquals(expected, new String(buf, 0, n, StandardCharsets.US_ASCII));
                }
            }
        }
    }

    @Test
    public void testSpecialisedPairsMatchGeneral() {
        Random r = new Random(4);
        byte[] expected = new byte[128];
        byte[] actual = new byte[128];
        double[] edges = {
                0.0, -0.0, 0.0005, -0.0005, 0.9995, 0.99999995, 1.0005, 9998.99999995, 9999.0, -9999.0, 10_000.0,
                99999999.9995, 1e8, -1e8, 6822000.125, 180.0, -180.0,
                Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 1e19
        };
        for (double x : edges) {
            for (double y : edges) {
                assertPair(x, y, expected, actual);
            }
        }
        for (int i = 0; i < 1_000_000; i++) {
            double x = randomOrdinate(r, 3 + (i & 4));
            double y = randomOrdinate(r, 3 + (i & 4));
            assertPair(x, y, expected, actual);
        }
    }

    @Test
    public void testFitsDocumentedRoom() {
        // Whole-word stores must stay within 18 bytes (37 for x, y), however
        // short the written number - an exactly sized array throws otherwise
        Random r = new Random(3);
        byte[] one = new byte[18];
        byte[] two = new byte[37];
        for (int maxDecimals = 0; maxDecimals <= 8; maxDecimals++) {
            for (int i = 0; i < 20_000; i++) {
                double x = randomOrdinate(r, maxDecimals);
                double y = randomOrdinate(r, maxDecimals);
                if (Math.abs(x) < 1e8 && Math.abs(y) < 1e8) {
                    DToA.dtoa(x, one, 0, 0, maxDecimals);
                    DToA.ftoa((float) x, one, 0, 0, maxDecimals);
                    DToA.dtoa(x, y, (byte) ',', two, 0, 0, maxDecimals);
                }
            }
        }
        DToA.dtoa(-99999999.99999999, one, 0, 8, 8);
        DToA.dtoa(-0.00000001, one, 0, 0, 8);
        DToA.dtoa(-99999999.99999999, -0.00000001, (byte) ',', two, 0, 0, 8);
        DToA.dtoaMax3(-99999999.9994, -0.001, (byte) ',', two, 0);
        DToA.dtoaMax3(-1, -99999999.9994, (byte) ',', two, 0);
        DToA.dtoaMax3(-99999999.9994, one, 0);
        DToA.dtoaMax7(-9998.9999999, -0.0000001, (byte) ',', two, 0);
    }

    private String format(float f, int minDecimals, int maxDecimals) {
        byte[] buf = new byte[32];
        return new String(buf, 0, DToA.ftoa(f, buf, 0, minDecimals, maxDecimals), StandardCharsets.UTF_8);
    }

    private String format(double d, int minDecimals, int maxDecimals) {
        char[] buf = new char[32];
        return new String(buf, 0, DToA.dtoa(d, buf, 0, minDecimals, maxDecimals));
    }

    private String formatBytes(double d, int minDecimals, int maxDecimals) {
        byte[] buf = new byte[64];
        return new String(buf, 0, DToA.dtoa(d, buf, 0, minDecimals, maxDecimals), StandardCharsets.US_ASCII);
    }

    private String formatBytes(float f, int minDecimals, int maxDecimals) {
        byte[] buf = new byte[64];
        return new String(buf, 0, DToA.ftoa(f, buf, 0, minDecimals, maxDecimals), StandardCharsets.US_ASCII);
    }

    private void assertPair(double x, double y, byte[] expected, byte[] actual) {
        int n = DToA.dtoa(x, y, (byte) ',', expected, 0, 0, 3);
        assertEquals(new String(expected, 0, n, StandardCharsets.US_ASCII),
                new String(actual, 0, DToA.dtoaMax3(x, y, (byte) ',', actual, 0), StandardCharsets.US_ASCII));
        n = DToA.dtoa(x, expected, 0, 0, 3);
        assertEquals(new String(expected, 0, n, StandardCharsets.US_ASCII),
                new String(actual, 0, DToA.dtoaMax3(x, actual, 0), StandardCharsets.US_ASCII));
        n = DToA.dtoa(x, y, (byte) ',', expected, 0, 0, 7);
        assertEquals(new String(expected, 0, n, StandardCharsets.US_ASCII),
                new String(actual, 0, DToA.dtoaMax7(x, y, (byte) ',', actual, 0), StandardCharsets.US_ASCII));
    }

    private double randomOrdinate(Random r, int maxDecimals) {
        double sign = r.nextBoolean() ? 1 : -1;
        switch (r.nextInt(6)) {
        case 0: return r.nextDouble(-10_000_000, 10_000_000);
        case 1: return r.nextDouble(-180, 180);
        case 2: return r.nextDouble(-35_000_000, 35_000_000);
        case 3: return sign * Math.scalb(r.nextDouble(), r.nextInt(-40, 60));
        case 4: return Math.round(r.nextDouble(-1e7, 1e7) * Math.pow(10, Math.min(maxDecimals, 8))) / Math.pow(10, Math.min(maxDecimals, 8));
        default: return r.nextInt(-1000, 1000);
        }
    }

    // Independent of DToA: same rounding, digits via the JDK
    private static String reference(double v, int minDecimals, int maxDecimals) {
        if (Double.isNaN(v)) {
            return "NaN";
        }
        StringBuilder sb = new StringBuilder();
        if (v < 0) {
            sb.append('-');
            v = -v;
        }
        if (Double.isInfinite(v)) {
            return sb.append("Infinity").toString();
        }
        if (v > Long.MAX_VALUE) {
            return sb.append(Double.toString(v)).toString();
        }
        long l = (long) v;
        long exp = (long) Math.pow(10, maxDecimals);
        long decimal = (long) ((v - l) * exp + 0.5);
        if (decimal == exp) {
            decimal = 0;
            l++;
        }
        return appendFraction(sb.append(l), decimal, minDecimals, maxDecimals);
    }

    private static String reference(float v, int minDecimals, int maxDecimals) {
        if (Float.isNaN(v)) {
            return "NaN";
        }
        StringBuilder sb = new StringBuilder();
        if (v < 0) {
            sb.append('-');
            v = -v;
        }
        if (Float.isInfinite(v)) {
            return sb.append("Infinity").toString();
        }
        if (v > Long.MAX_VALUE) {
            return sb.append(Float.toString(v)).toString();
        }
        long l = (long) v;
        long exp = (long) Math.pow(10, maxDecimals);
        long decimal = (long) ((v - l) * exp + 0.5);
        if (decimal == exp) {
            decimal = 0;
            l++;
        }
        return appendFraction(sb.append(l), decimal, minDecimals, maxDecimals);
    }

    private static String appendFraction(StringBuilder sb, long decimal, int minDecimals, int maxDecimals) {
        String fraction = decimal == 0 ? "" : String.format("%0" + maxDecimals + "d", decimal);
        int end = fraction.length();
        while (end > minDecimals && fraction.charAt(end - 1) == '0') {
            end--;
        }
        fraction = fraction.substring(0, end);
        while (fraction.length() < minDecimals) {
            fraction += "0";
        }
        return fraction.isEmpty() ? sb.toString() : sb.append('.').append(fraction).toString();
    }

}
