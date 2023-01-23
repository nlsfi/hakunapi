package fi.nls.hakunapi.core.transformer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyTransformed;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.HakunaPropertyWriters;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyLong;

public class TransformChainTest {

    @Test
    public void test() throws Exception {
        HakunaProperty foo = new HakunaPropertyLong("foo", "bar", "baz", false, false, HakunaPropertyWriters.HIDDEN);
        TransformChain chain = new TransformChain();
        HakunaProperty t = new HakunaPropertyTransformed(foo, chain);
        chain.init(t, "fi.nls.hakunapi.core.transformer.LongToString() | fi.nls.hakunapi.core.transformer.PrefixTransformer(foobar)");

        assertEquals(HakunaPropertyType.STRING, chain.getPublicType());
        assertEquals("foobar12345", chain.toPublic(12345L));
        assertEquals(1234L, chain.toInner("foobar1234"));

        assertEquals(HakunaProperty.UNKNOWN, chain.toInner("bazqux5432"));
        
        chain.init(t, "fi.nls.hakunapi.core.transformer.LongToString() | fi.nls.hakunapi.core.transformer.StringManipulator(%d[0:1],'kissa')");
        
        assertEquals(HakunaPropertyType.STRING, chain.getPublicType());
        assertEquals("1kissa", chain.toPublic(12345L));
        assertEquals(3L, chain.toInner("3kissa"));

    }

}
