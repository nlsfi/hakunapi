package fi.nls.hakunapi.tiles.generator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.junit.Test;

import fi.nls.hakunapi.tiles.source.vectortile.mvt.MvtLayerEncoder;
import fi.nls.hakunapi.tiles.source.vectortile.mvt.MvtTileWriter;

public class MvtTileTest {

    private static byte[] tile(MvtLayerEncoder... layers) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MvtTileWriter.write(out, layers);
        return out.toByteArray();
    }

    private static MvtLayerEncoder emptyLayer(String name) {
        MvtLayerEncoder layer = new MvtLayerEncoder();
        layer.reset(name);
        layer.setExtent(4096);
        layer.finish();
        return layer;
    }

    private static MvtLayerEncoder pointLayer(String name) {
        MvtLayerEncoder layer = new MvtLayerEncoder();
        layer.reset(name);
        layer.setExtent(4096);
        int key = layer.keyIndex("kohdeluokka");
        layer.beginFeature();
        layer.tagLong(key, 12132);
        int[] geometry = new int[] { (1 << 3) | 1, 200, 400 };
        layer.endFeature(1, MvtLayerEncoder.GEOM_POINT, geometry, geometry.length);
        layer.finish();
        return layer;
    }

    @Test
    public void aFeatureInAnyLayerCounts() throws Exception {
        assertTrue(MvtTile.hasFeatures(tile(pointLayer("a"))));
        assertTrue(MvtTile.hasFeatures(tile(emptyLayer("a"), pointLayer("b"))));
        assertTrue(MvtTile.hasFeatures(tile(pointLayer("a"), emptyLayer("b"))));
    }

    @Test
    public void emptyLayersHaveNoFeaturesEvenThoughTheTileIsNotZeroBytes() throws Exception {
        assertTrue(tile(emptyLayer("tieviiva")).length > 0);
        assertFalse(MvtTile.hasFeatures(tile(emptyLayer("a"), emptyLayer("b"))));
        assertFalse(MvtTile.hasFeatures(tile()));
        assertFalse(MvtTile.hasFeatures(new byte[0]));
        assertFalse(MvtTile.hasFeatures(null));
    }

}
