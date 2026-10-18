package fi.nls.hakunapi.tiles.generator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FilterParser;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.config.HakunaConfigParser;
import fi.nls.hakunapi.cql2.text.CQL2Text;

public class GeneratorControlTest {

    private static final int SRID = 3067;
    private static final List<FilterParser> FILTERS = List.of(CQL2Text.INSTANCE);

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private static GeneratorServiceConfig service(String... collectionIds) {
        Map<String, FeatureType> byId = new LinkedHashMap<>();
        for (String id : collectionIds) {
            byId.put(id, new StubCollection(id, SRID));
        }
        GeneratorServiceConfig service = new GeneratorServiceConfig(byId);
        service.setKnownSrids(List.of(new SRIDCode(SRID, false, false,
                fi.nls.hakunapi.core.geom.HakunaGeometryDimension.XY)));
        return service;
    }

    private GeneratorControl read(String content, String... collectionIds) throws Exception {
        Path path = tmp.newFolder().toPath().resolve("control.properties");
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        HakunaConfigParser cfg = GeneratorControl.load(path);
        return GeneratorControl.readRanges(cfg, service(collectionIds), FILTERS,
                new SRIDCode(SRID, false, false, fi.nls.hakunapi.core.geom.HakunaGeometryDimension.XY));
    }

    private void assertRejected(String message, String content) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> read(content, "maastoalue"));
        assertTrue(e.getMessage(), e.getMessage().toLowerCase().contains(message));
    }

    @Test
    public void rangesAreSortedAndCarryTheirCollectionsAndFilters() throws Exception {
        GeneratorControl control = read("""
                tileMatrixSet = ETRS-TM35FIN
                ranges = detail, overview
                detail.zoom = 8-14
                detail.collections = maastoalue, tieviiva
                detail.tieviiva.filter = kohdeluokka <= 12132
                overview.zoom = 0-7
                overview.collections = maastoalue
                """, "maastoalue", "tieviiva");

        assertEquals("ETRS-TM35FIN", control.getTileMatrixSetId());
        assertEquals("overview", control.getRanges().get(0).name());
        assertEquals(0, control.getMinZoom());
        assertEquals(14, control.getMaxZoom());
        assertEquals(List.of("maastoalue", "tieviiva"), control.getRange(10).collectionIds());
        assertNotNull(control.getRange(10).filters().get("tieviiva"));
        assertNull(control.getRange(10).filters().get("maastoalue"));
        assertThrows(IllegalArgumentException.class, () -> control.getRange(15));
    }

    @Test
    public void invalidControlFilesAreRejectedWhenRead() {
        assertRejected("overlap", """
                tileMatrixSet = T
                ranges = a, b
                a.zoom = 0-8
                a.collections = maastoalue
                b.zoom = 7-14
                b.collections = maastoalue
                """);
        assertRejected("zoom 6", """
                tileMatrixSet = T
                ranges = a, b
                a.zoom = 0-5
                a.collections = maastoalue
                b.zoom = 8-14
                b.collections = maastoalue
                """);
        assertRejected("eiolemassa", """
                tileMatrixSet = T
                ranges = a
                a.zoom = 0-2
                a.collections = eiolemassa
                """);
        assertRejected("maastoalue", """
                tileMatrixSet = T
                ranges = a
                a.zoom = 0-2
                a.collections = maastoalue
                a.maastoalue.filter = this is not CQL2 (((
                """);
        assertRejected("functions", """
                tileMatrixSet = T
                ranges = a
                a.zoom = 0-2
                a.collections = maastoalue
                a.maastoalue.filter = nosuchfunction(kohdeluokka)
                """);
        assertRejected("inverted", """
                tileMatrixSet = T
                ranges = a
                a.zoom = 9-2
                a.collections = maastoalue
                """);
        assertRejected("zoom", """
                tileMatrixSet = T
                ranges = a
                a.zoom = kaikki
                a.collections = maastoalue
                """);
        assertRejected("collections", """
                tileMatrixSet = T
                ranges = a
                a.zoom = 0-2
                """);
        assertRejected("ranges", """
                tileMatrixSet = T
                """);
        assertRejected("tilematrixset", """
                ranges = a
                a.zoom = 0-2
                a.collections = maastoalue
                """);
    }

}
