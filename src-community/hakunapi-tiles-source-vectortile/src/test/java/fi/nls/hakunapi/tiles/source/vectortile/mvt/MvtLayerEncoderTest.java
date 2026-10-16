package fi.nls.hakunapi.tiles.source.vectortile.mvt;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;

import fi.nls.hakunapi.tiles.source.vectortile.mvt.MvtDecoder.Feature;
import fi.nls.hakunapi.tiles.source.vectortile.mvt.MvtDecoder.Layer;

public class MvtLayerEncoderTest {

    private static byte[] tile(MvtLayerEncoder... layers) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MvtTileWriter.write(bos, layers);
        return bos.toByteArray();
    }

    private static void encodeDates(MvtLayerEncoder l, LocalDate[] dates, boolean asDate) {
        l.reset("t");
        int k = l.keyIndex("alkupvm");
        for (int i = 0; i < dates.length; i++) {
            l.beginFeature();
            if (asDate) {
                l.tagDate(k, dates[i]);
            } else {
                l.tagString(k, dates[i].toString());
            }
            int[] g = {9, 2, 2};
            l.endFeature(i + 1, MvtLayerEncoder.GEOM_POINT, g, g.length);
        }
        l.finish();
    }

    @Test
    public void singlePointFeatureRoundTrips() throws Exception {
        MvtLayerEncoder l = new MvtLayerEncoder();
        l.reset("points");
        int kName = l.keyIndex("name");
        l.beginFeature();
        l.tagString(kName, "A");
        int[] geom = {9, 10, 14};
        l.endFeature(42, MvtLayerEncoder.GEOM_POINT, geom, geom.length);
        l.finish();

        List<Layer> layers = MvtDecoder.decodeTile(tile(l));
        assertEquals(1, layers.size());
        Layer layer = layers.get(0);
        assertEquals("points", layer.name);
        assertEquals(4096, layer.extent);
        assertEquals("A", layer.values.get(0));
        Feature f = layer.features.get(0);
        assertEquals(42, f.id);
        assertArrayEquals(new int[] {0, 0}, f.tags);
        assertArrayEquals(geom, f.geometry);
    }

    @Test
    public void valuesAreDedupedAcrossFeatures() throws Exception {
        MvtLayerEncoder l = new MvtLayerEncoder();
        l.reset("t");
        int kCat = l.keyIndex("cat");
        for (int i = 0; i < 3; i++) {
            l.beginFeature();
            l.tagString(kCat, "road");
            int[] g = {9, 2, 2};
            l.endFeature(i + 1, MvtLayerEncoder.GEOM_POINT, g, g.length);
        }
        l.finish();

        Layer layer = MvtDecoder.decodeTile(tile(l)).get(0);
        assertEquals(1, layer.values.size());
        for (Feature f : layer.features) {
            assertArrayEquals(new int[] {0, 0}, f.tags);
        }
    }

    @Test
    public void dateIsIdenticalToItsToStringOnTheWire() throws Exception {
        LocalDate[] dates = {
            LocalDate.of(2025, 3, 25),
            LocalDate.of(1, 1, 1),
            LocalDate.of(10000, 6, 15),
            LocalDate.of(-44, 3, 15),
            LocalDate.MIN,
            LocalDate.MAX,
        };
        MvtLayerEncoder asDate = new MvtLayerEncoder();
        MvtLayerEncoder asString = new MvtLayerEncoder();
        encodeDates(asDate, dates, true);
        encodeDates(asString, dates, false);

        assertArrayEquals(tile(asString), tile(asDate));
    }

    @Test
    public void everyVarintWidthRoundTrips() throws Exception {
        int[] boundaries = {
            0, 1, 0x7f, 0x80, 0x3fff, 0x4000, 0x1fffff, 0x200000,
            0xfffffff, 0x10000000, 0x7fffffff, 0x80000000, 0xffffffff
        };
        int count = boundaries.length;
        int[] geom = new int[4 + count * 2];
        geom[0] = 1 | (1 << 3);
        geom[3] = 2 | (count << 3);
        for (int i = 0; i < count; i++) {
            geom[4 + i * 2] = boundaries[i];
            geom[5 + i * 2] = boundaries[count - 1 - i];
        }

        MvtLayerEncoder l = new MvtLayerEncoder();
        l.reset("l");
        l.beginFeature();
        l.endFeature(1, MvtLayerEncoder.GEOM_LINE, geom, geom.length);
        l.finish();

        assertArrayEquals(geom, MvtDecoder.decodeTile(tile(l)).get(0).features.get(0).geometry);
    }

    @Test
    public void reuseAfterTrimProducesIdenticalBytes() throws Exception {
        LocalDate[] dates = {LocalDate.of(2001, 1, 1), LocalDate.of(2002, 2, 2)};
        MvtLayerEncoder fresh = new MvtLayerEncoder();
        encodeDates(fresh, dates, true);
        byte[] expected = tile(fresh);

        MvtLayerEncoder reused = new MvtLayerEncoder();
        encodeDates(reused, new LocalDate[] {LocalDate.of(1999, 9, 9)}, true);
        tile(reused);
        reused.trim();
        encodeDates(reused, dates, true);

        assertArrayEquals(expected, tile(reused));
    }
}
