package fi.nls.hakunapi.core.util;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class UTF8Test {

    @Test
    public void test() {
        String kosme = "κόσμε";
        assertEquals(getLenJava(kosme), UTF8.getLength(kosme));

        String foo = "äöäÄÖÅ";
        assertEquals(getLenJava(foo), UTF8.getLength(foo));

        String bar = "©®";
        assertEquals(getLenJava(bar), UTF8.getLength(bar));

        String baz = "🤦🏼‍♂️";
        assertEquals(getLenJava(baz), UTF8.getLength(baz));
    }

    private int getLenJava(String s) {
        return s.getBytes(StandardCharsets.UTF_8).length;
    }

}
