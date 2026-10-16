package fi.nls.hakunapi.tiles.source.vectortile.mvt;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayOutputStream;

import org.junit.Test;

import fi.nls.hakunapi.tiles.mvt.MvtLayerExtract;

public class MvtLayerExtractTest {
    private static MvtLayerEncoder layer(String name, int id, int kohdeluokka) {
        MvtLayerEncoder layer = new MvtLayerEncoder();
        layer.reset(name);
        layer.setExtent(4096);
        int key = layer.keyIndex("kohdeluokka");
        layer.beginFeature();
        layer.tagLong(key, kohdeluokka);
        int[] geometry = new int[] { (1 << 3) | 1, 200 + id, 400 + id };
        layer.endFeature(id, MvtLayerEncoder.GEOM_POINT, geometry, geometry.length);
        layer.finish();
        return layer;
    }

    private static byte[] tile(MvtLayerEncoder... layers) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MvtTileWriter.write(out, layers);
        return out.toByteArray();
    }

    @Test
    public void extractionIsByteExact() throws Exception {
        byte[] alone = tile(layer("tieviiva", 1, 12132));
        byte[] multi = tile(layer("tieviiva", 1, 12132), layer("rakennus", 2, 42210));

        assertArrayEquals(alone, MvtLayerExtract.extract(multi, "tieviiva"));
    }

    @Test
    public void anUnknownLayerYieldsNull() throws Exception {
        byte[] multi = tile(layer("tieviiva", 1, 12132), layer("rakennus", 2, 42210));
        assertNull(MvtLayerExtract.extract(multi, "eiolemassa"));
    }

}
