package fi.nls.hakunapi.tiles.config;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Properties;

import org.junit.Test;

import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.tiles.DefaultTileMatrixSetRegistry;
import fi.nls.hakunapi.tiles.TileMatrix;
import fi.nls.hakunapi.tiles.TileMatrixSet;

public class TilesConfigParserTest {

    private static final double EPS = 1e-9;

    private TilesConfigParser parser(Properties p) {
        return new TilesConfigParser(new HakunaConfigParser(p));
    }

    private Properties webMercatorLike(String id) {
        Properties p = new Properties();
        String pre = "tileMatrixSets." + id + ".";
        p.setProperty("tileMatrixSets", id);
        p.setProperty(pre + "srid", "3857");
        p.setProperty(pre + "bbox",
                "-20037508.342789244,-20037508.342789244,20037508.342789244,20037508.342789244");
        p.setProperty(pre + "resolutions",
                "156543.03392804097,78271.51696402048,39135.75848201024");
        return p;
    }

    @Test
    public void derivesCrsUriFromSrid() {
        TileMatrixSet tms = parser(webMercatorLike("WMQ")).readTileMatrixSet("WMQ");
        assertEquals(3857, tms.getSrid());
        assertEquals("http://www.opengis.net/def/crs/EPSG/0/3857", tms.getCrs());
    }

    @Test
    public void crs84SridMapsToOgcUri() {
        Properties p = webMercatorLike("G");
        p.setProperty("tileMatrixSets.G.srid", "84");
        p.setProperty("tileMatrixSets.G.bbox", "-180,-90,180,90");
        p.setProperty("tileMatrixSets.G.resolutions", Double.toString(180.0 / 256.0));
        TileMatrixSet tms = parser(p).readTileMatrixSet("G");
        assertEquals("http://www.opengis.net/def/crs/OGC/1.3/CRS84", tms.getCrs());
    }

    @Test
    public void originIsExtentTopLeft() {
        TileMatrixSet tms = parser(webMercatorLike("WMQ")).readTileMatrixSet("WMQ");
        TileMatrix m0 = tms.getTileMatrices().get(0);
        assertEquals(-20037508.342789244, m0.getPointOfOriginX(), EPS);
        assertEquals(20037508.342789244, m0.getPointOfOriginY(), EPS);
    }

    @Test
    public void derivesMatrixSizePerLevel() {
        TileMatrixSet tms = parser(webMercatorLike("WMQ")).readTileMatrixSet("WMQ");
        List<TileMatrix> ms = tms.getTileMatrices();
        assertEquals(3, ms.size());
        assertEquals(1, ms.get(0).getMatrixWidth());
        assertEquals(1, ms.get(0).getMatrixHeight());
        assertEquals(2, ms.get(1).getMatrixWidth());
        assertEquals(2, ms.get(1).getMatrixHeight());
        assertEquals(4, ms.get(2).getMatrixWidth());
        assertEquals(4, ms.get(2).getMatrixHeight());
        assertEquals(256, ms.get(0).getTileWidth());
        assertEquals(156543.03392804097, ms.get(0).getCellSize(), EPS);
    }

    @Test
    public void scaleDenominatorFromResolution() {
        TileMatrixSet tms = parser(webMercatorLike("WMQ")).readTileMatrixSet("WMQ");
        TileMatrix m0 = tms.getTileMatrices().get(0);
        assertEquals(156543.03392804097 / TilesConfigParser.STANDARDIZED_PIXEL_SIZE,
                m0.getScaleDenominator(), 1e-3);
    }

    @Test
    public void matrixIdsOverrideDefaultIndices() {
        Properties p = webMercatorLike("WMQ");
        p.setProperty("tileMatrixSets.WMQ.matrixIds", "L0,L1,L2");
        TileMatrixSet tms = parser(p).readTileMatrixSet("WMQ");
        assertEquals("L0", tms.getTileMatrices().get(0).getId());
        assertEquals("L2", tms.getTileMatrices().get(2).getId());
    }

    @Test
    public void defaultMatrixIdsAreLevelIndices() {
        TileMatrixSet tms = parser(webMercatorLike("WMQ")).readTileMatrixSet("WMQ");
        assertEquals("0", tms.getTileMatrices().get(0).getId());
        assertEquals("2", tms.getTileMatrices().get(2).getId());
    }

    @Test
    public void nonSquareTileSizeRespected() {
        Properties p = webMercatorLike("WMQ");
        p.setProperty("tileMatrixSets.WMQ.bbox",
                "-20037508.342789244,5009377.085697312,-15028131.257091932,20037508.342789244");
        p.setProperty("tileMatrixSets.WMQ.resolutions", "5565.974539663679");
        p.setProperty("tileMatrixSets.WMQ.tileWidth", "450");
        p.setProperty("tileMatrixSets.WMQ.tileHeight", "450");
        TileMatrixSet tms = parser(p).readTileMatrixSet("WMQ");
        TileMatrix m = tms.getTileMatrices().get(0);
        assertEquals(2, m.getMatrixWidth());
        assertEquals(6, m.getMatrixHeight());
    }

    @Test(expected = IllegalArgumentException.class)
    public void extentNotWholeTilesFails() {
        Properties p = webMercatorLike("WMQ");
        p.setProperty("tileMatrixSets.WMQ.bbox",
                "-20037508.342789244,-20037508.342789244,20037508.342789244,19000000.0");
        parser(p).readTileMatrixSet("WMQ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void badBboxLengthFails() {
        Properties p = webMercatorLike("WMQ");
        p.setProperty("tileMatrixSets.WMQ.bbox", "0,0,100");
        parser(p).readTileMatrixSet("WMQ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void matrixIdsLengthMismatchFails() {
        Properties p = webMercatorLike("WMQ");
        p.setProperty("tileMatrixSets.WMQ.matrixIds", "only,two");
        parser(p).readTileMatrixSet("WMQ");
    }

    @Test
    public void finnishEtrsTm35FinGrid() {
        Properties p = new Properties();
        p.setProperty("tileMatrixSets", "ETRS-TM35FIN");
        p.setProperty("tileMatrixSets.ETRS-TM35FIN.srid", "3067");
        p.setProperty("tileMatrixSets.ETRS-TM35FIN.bbox",
                "-548576.0,6291456.0,1548576.0,8388608.0");
        p.setProperty("tileMatrixSets.ETRS-TM35FIN.resolutions",
                "8192,4096,2048,1024,512,256,128,64,32,16,8,4,2,1,0.5,0.25");

        TileMatrixSet tms = parser(p).readTileMatrixSet("ETRS-TM35FIN");

        assertEquals(3067, tms.getSrid());
        assertEquals("http://www.opengis.net/def/crs/EPSG/0/3067", tms.getCrs());

        List<TileMatrix> ms = tms.getTileMatrices();
        assertEquals(16, ms.size());

        assertEquals(-548576.0, ms.get(0).getPointOfOriginX(), EPS);
        assertEquals(8388608.0, ms.get(0).getPointOfOriginY(), EPS);

        for (int z = 0; z < ms.size(); z++) {
            TileMatrix m = ms.get(z);
            int expected = 1 << z;
            assertEquals("level " + z + " width", expected, m.getMatrixWidth());
            assertEquals("level " + z + " height", expected, m.getMatrixHeight());
        }

        assertEquals(8192.0, ms.get(0).getCellSize(), EPS);
        assertEquals(0.25, ms.get(15).getCellSize(), EPS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void missingSridFails() {
        Properties p = webMercatorLike("WMQ");
        p.remove("tileMatrixSets.WMQ.srid");
        parser(p).readTileMatrixSet("WMQ");
    }

    @Test
    public void tileMatrixSetIdsDefaultToWebMercatorQuad() {
        List<String> ids = parser(new Properties()).readTileMatrixSetIds("layer");
        assertEquals(List.of(DefaultTileMatrixSetRegistry.WEB_MERCATOR_QUAD), ids);
    }

    @Test
    public void serviceWideDefaultTileMatrixSetIds() {
        Properties p = new Properties();
        p.setProperty("default.tiles.layers.tileMatrixSets", "ETRS-TM35FIN,WebMercatorQuad");
        assertEquals(List.of("ETRS-TM35FIN", "WebMercatorQuad"),
                parser(p).readTileMatrixSetIds("layer"));
    }

    @Test
    public void perLayerTileMatrixSetIdsWinOverServiceWideDefault() {
        Properties p = new Properties();
        p.setProperty("default.tiles.layers.tileMatrixSets", "ETRS-TM35FIN");
        p.setProperty("tiles.layers.layer.tileMatrixSets", "WebMercatorQuad");
        assertEquals(List.of("WebMercatorQuad"), parser(p).readTileMatrixSetIds("layer"));
        assertEquals(List.of("ETRS-TM35FIN"), parser(p).readTileMatrixSetIds("other"));
    }

}
