package fi.nls.hakunapi.tiles.source.pmtiles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;

import org.junit.Test;

public class DirectoryTest {
    @Test
    public void rejectsEntryCountThatCannotFitTheBytes() {
        byte[] bytes = { (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, 0x20 };
        assertThrows(IOException.class, () -> Directory.parse(bytes));
    }

    @Test
    public void parsesOneEntryWhenTheBytesHoldIt() throws IOException {
        Directory dir = Directory.parse(new byte[] { 1, 0, 1, 1, 1 });
        assertEquals(1, dir.size());
        assertEquals(1, dir.get(0).runLength());
    }
}
