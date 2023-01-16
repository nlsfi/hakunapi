package fi.nls.hakunapi.geojson.hakuna;

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class HakunaJsonWriterTest {

    @Test
    public void testWriteString() throws IOException {
        String kosme = "κόσμε";
        assertArrayEquals(kosme.getBytes(StandardCharsets.UTF_8), writeString(kosme));

        String foo = "äöäÄÖÅ";
        assertArrayEquals(foo.getBytes(StandardCharsets.UTF_8), writeString(foo));

        String bar = "©®";
        assertArrayEquals(bar.getBytes(StandardCharsets.UTF_8), writeString(bar));

        String baz = "🤦🏼‍♂️";
        assertArrayEquals(baz.getBytes(StandardCharsets.UTF_8), writeString(baz));
    }

    private byte[] writeString(String s) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(64);
        try (HakunaJsonWriter json = new HakunaJsonWriter(baos, null)) {
            json.writeUTF8(s);
        }
        return baos.toByteArray();
    }

}
