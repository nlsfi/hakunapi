package fi.nls.hakunapi.core.util;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

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
    
    private String format(float f, int minDecimals, int maxDecimals) {
        byte[] buf = new byte[32];
        return new String(buf, 0, DToA.ftoa(f, buf, 0, minDecimals, maxDecimals), StandardCharsets.UTF_8);
    }

    private String format(double d, int minDecimals, int maxDecimals) {
        char[] buf = new char[32];
        return new String(buf, 0, DToA.dtoa(d, buf, 0, minDecimals, maxDecimals));
    }
    
}
