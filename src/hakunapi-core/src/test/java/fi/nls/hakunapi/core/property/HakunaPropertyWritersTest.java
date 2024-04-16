package fi.nls.hakunapi.core.property;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HakunaPropertyWritersTest {

    @Test
    public void testDoubleAsID() {

        assertEquals("1.7976931348623157E308", HakunaPropertyWriters.doubleAsID(Double.MAX_VALUE));
        assertEquals("9223372036854775807", HakunaPropertyWriters.doubleAsID(Double.valueOf(Long.MAX_VALUE)));
        
        assertEquals("17263226", HakunaPropertyWriters.doubleAsID(1.7263226E7));
        assertEquals("2010474", HakunaPropertyWriters.doubleAsID(2010474.0));
    }
}
