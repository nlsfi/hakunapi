package fi.nls.hakunapi.core.property.simple;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyWriters;

public class HakunaPropertyIntTest {

    @Test
    public void testToInnerWithTooLargeValue() {
        HakunaProperty prop = new HakunaPropertyInt("test", "test",  "test", true, false, HakunaPropertyWriters.HIDDEN);
        assertEquals(HakunaProperty.UNKNOWN, prop.toInner("261254150482831638"));
        assertEquals(HakunaProperty.UNKNOWN, prop.toInner("-261254150482831638"));
    }

    @Test
    public void testToInnerWorksWithTrailingZeroes() {
        HakunaProperty prop = new HakunaPropertyInt("test", "test",  "test", true, false, HakunaPropertyWriters.HIDDEN);
        assertEquals(1337, prop.toInner("1337.000"));
        assertEquals(555, prop.toInner("555."));
        assertEquals(555, prop.toInner("555.0"));
        assertEquals(0, prop.toInner(".0"));
        assertEquals(HakunaProperty.UNKNOWN, prop.toInner("5.0001"));
    }

}
