package fi.nls.hakunapi.core.property.simple;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyWriters;

public class HakunaPropertyLongTest {

    @Test
    public void testToInnerWithLargeLongValue() {
        HakunaProperty prop = new HakunaPropertyLong("test", "test",  "test", true, false, HakunaPropertyWriters.HIDDEN);
        assertEquals(261254150482831638L, prop.toInner("261254150482831638"));
        assertEquals(-261254150482831638L, prop.toInner("-261254150482831638"));
    }

    @Test
    public void testToInnerWorksWithTrailingZeroes() {
        HakunaProperty prop = new HakunaPropertyLong("test", "test",  "test", true, false, HakunaPropertyWriters.HIDDEN);
        assertEquals(1337L, prop.toInner("1337.000"));
        assertEquals(555L, prop.toInner("555."));
        assertEquals(555L, prop.toInner("555.0"));
        assertEquals(0L, prop.toInner(".0"));
        assertEquals(HakunaProperty.UNKNOWN, prop.toInner("5.0001"));
    }

}
