package fi.nls.hakunapi.tiles.source.vectortile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import fi.nls.hakunapi.core.DatetimeProperty;
import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.FeatureStream;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.OrderBy;
import fi.nls.hakunapi.core.PaginationStrategy;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.geom.HakunaGeometryJTS;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.core.param.GetFeatureParam;
import fi.nls.hakunapi.core.projection.ProjectionTransformerFactory;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.tiles.source.vectortile.mvt.MvtDecoder;
import fi.nls.hakunapi.tiles.source.vectortile.mvt.MvtDecoder.Layer;

public class JavaVectorTileGeneratorTest {
    private static final int SRID = 3857;
    private static final int EXTENT = 4096;
    private static final GeometryFactory GF = new GeometryFactory();

    private TileContext ctx() {
        double span = 1000.0;
        return new TileContext(SRID, 0, 0, span, span, EXTENT, EXTENT, span / EXTENT);
    }

    @Test
    public void encodesPolygonLayerWithAttributes() throws Exception {
        Geometry poly = GF.createPolygon(new Coordinate[] {
                new Coordinate(250, 250), new Coordinate(750, 250),
                new Coordinate(750, 750), new Coordinate(250, 750),
                new Coordinate(250, 250)});
        StubFeatureType ft = new StubFeatureType("buildings", HakunaGeometryType.POLYGON,
                Arrays.asList(prop("name", HakunaPropertyType.STRING),
                              prop("height", HakunaPropertyType.DOUBLE)));
        ft.addFeature(7L, new HakunaGeometryJTS(poly), "Town Hall", 42.0);

        byte[] tile = generate(ft);
        List<Layer> layers = MvtDecoder.decodeTile(tile);
        assertEquals(1, layers.size());
        Layer layer = layers.get(0);
        assertEquals("buildings", layer.name);
        assertEquals(EXTENT, layer.extent);
        assertEquals(1, layer.features.size());
        assertEquals(7, layer.features.get(0).id);
        assertEquals(3, layer.features.get(0).type);
        assertTrue(layer.keys.contains("name"));
        assertTrue(layer.keys.contains("height"));
        assertTrue(layer.values.contains("Town Hall"));
        assertTrue(layer.values.contains(42.0));
    }

    @Test
    public void encodesEveryAttributeType() throws Exception {
        Geometry poly = GF.createPolygon(new Coordinate[] {
                new Coordinate(250, 250), new Coordinate(750, 250),
                new Coordinate(750, 750), new Coordinate(250, 750),
                new Coordinate(250, 250)});
        StubFeatureType ft = new StubFeatureType("mixed", HakunaGeometryType.POLYGON,
                Arrays.asList(prop("s", HakunaPropertyType.STRING),
                              prop("b", HakunaPropertyType.BOOLEAN),
                              prop("i", HakunaPropertyType.INT),
                              prop("l", HakunaPropertyType.LONG),
                              prop("f", HakunaPropertyType.FLOAT),
                              prop("d", HakunaPropertyType.DOUBLE)));
        ft.addFeature(1L, new HakunaGeometryJTS(poly),
                "text", Boolean.TRUE, Integer.valueOf(696), Long.valueOf(9_000_000_000L),
                Float.valueOf(1.5f), Double.valueOf(2.5));

        Layer layer = MvtDecoder.decodeTile(generate(ft)).get(0);
        assertEquals(1, layer.features.size());
        for (String key : new String[] {"s", "b", "i", "l", "f", "d"}) {
            assertTrue("key " + key, layer.keys.contains(key));
        }
        assertTrue("string", layer.values.contains("text"));
        assertTrue("boolean", layer.values.contains(Boolean.TRUE));
        assertTrue("int as long", layer.values.contains(696L));
        assertTrue("long", layer.values.contains(9_000_000_000L));
        assertTrue("float", layer.values.contains(1.5f));
        assertTrue("double", layer.values.contains(2.5));
    }

    @Test
    public void emptyLayersAreLeftOutOfAMultiLayerTile() throws Exception {
        Geometry inside = GF.createPoint(new Coordinate(500, 500));
        Geometry outside = GF.createPoint(new Coordinate(5000, 5000));

        StubFeatureType hit = new StubFeatureType("hit", HakunaGeometryType.POINT,
                Arrays.asList(prop("kohdeluokka", HakunaPropertyType.INT)));
        hit.addFeature(1L, new HakunaGeometryJTS(inside), 12132);

        StubFeatureType miss = new StubFeatureType("miss", HakunaGeometryType.POINT,
                Arrays.asList(prop("a", HakunaPropertyType.STRING),
                        prop("b", HakunaPropertyType.STRING),
                        prop("c", HakunaPropertyType.STRING)));
        miss.addFeature(2L, new HakunaGeometryJTS(outside), "x", "y", "z");

        List<FeatureType> both = new ArrayList<>();
        both.add(miss);
        both.add(hit);
        both.add(miss);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new JavaVectorTileGenerator().generate(ctx(), both, "application/vnd.mapbox-vector-tile",
                Collections.emptyMap()).get().writeTo(baos);

        List<Layer> layers = MvtDecoder.decodeTile(baos.toByteArray());
        assertEquals(1, layers.size());
        assertEquals("hit", layers.get(0).name);
        assertEquals(1, layers.get(0).features.size());
    }

    private static Geometry square(double minX, double minY, double maxX, double maxY) {
        return GF.createPolygon(new Coordinate[] {
                new Coordinate(minX, minY), new Coordinate(maxX, minY),
                new Coordinate(maxX, maxY), new Coordinate(minX, maxY),
                new Coordinate(minX, minY)});
    }

    @Test
    public void blockRendersEachTileAsASingleTileRenderWould() throws Exception {
        StubFeatureType ft = new StubFeatureType("across", HakunaGeometryType.POLYGON,
                Arrays.asList(prop("name", HakunaPropertyType.STRING),
                              prop("height", HakunaPropertyType.DOUBLE)));
        ft.addFeature(1L, new HakunaGeometryJTS(square(200, 200, 700, 700)), "inside", 1.0);
        ft.addFeature(2L, new HakunaGeometryJTS(square(800, 400, 1400, 900)), "seam", 2.0);
        ft.addFeature(3L, new HakunaGeometryJTS(square(-500, -500, 2500, 2500)), "cover", 3.0);
        ft.addFeature(4L, new HakunaGeometryJTS(square(5000, 5000, 5500, 5500)), "away", 4.0);

        assertBlockMatchesPerTile(ft);
    }

    private void assertBlockMatchesPerTile(FeatureType ft) throws Exception {
        List<TileContext> ctxs = blockCtxs();
        JavaVectorTileGenerator generator = new JavaVectorTileGenerator();
        List<FeatureType> one = Collections.singletonList(ft);

        double margin = JavaVectorTileGenerator.clipMargin(ctxs.get(0));
        double[] queryBox = { ctxs.get(0).minX() - margin, ctxs.get(0).minY() - margin,
                ctxs.get(0).maxX() + margin, ctxs.get(0).maxY() + margin };
        for (int t = 1; t < ctxs.size(); t++) {
            TileContext ctx = ctxs.get(t);
            double m = JavaVectorTileGenerator.clipMargin(ctx);
            queryBox[0] = Math.min(queryBox[0], ctx.minX() - m);
            queryBox[1] = Math.min(queryBox[1], ctx.minY() - m);
            queryBox[2] = Math.max(queryBox[2], ctx.maxX() + m);
            queryBox[3] = Math.max(queryBox[3], ctx.maxY() + m);
        }

        List<byte[]> block = generator.generateBlock(ctxs, one, queryBox);
        assertEquals("tiles in the block", ctxs.size(), block.size());
        for (int t = 0; t < ctxs.size(); t++) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            generator.generate(ctxs.get(t), one, "application/vnd.mapbox-vector-tile",
                    Collections.emptyMap()).get().writeTo(out);
            org.junit.Assert.assertArrayEquals("tile " + t, out.toByteArray(), block.get(t));
        }
    }

    private List<TileContext> blockCtxs() {
        double span = 1000.0;
        List<TileContext> ctxs = new ArrayList<>(4);
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                double minX = col * span;
                double maxY = span - row * span;
                ctxs.add(new TileContext(SRID, minX, maxY - span, minX + span, maxY,
                        EXTENT, EXTENT, span / EXTENT));
            }
        }
        return ctxs;
    }

    private byte[] generate(FeatureType ft) throws Exception {
        Optional<fi.nls.hakunapi.tiles.TileBody> r = new JavaVectorTileGenerator().generate(
                ctx(), Collections.singletonList(ft), "application/vnd.mapbox-vector-tile",
                Collections.emptyMap());
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        r.get().writeTo(out);
        return out.toByteArray();
    }

    private static final fi.nls.hakunapi.core.property.HakunaPropertyWriter NOOP_WRITER =
            new fi.nls.hakunapi.core.property.HakunaPropertyWriter() {
                public void write(ValueProvider p, int i, fi.nls.hakunapi.core.FeatureWriter w) {}
            };

    private static HakunaProperty prop(String name, HakunaPropertyType type) {
        return new StubProperty(name, type);
    }

    private static final class Row implements ValueProvider {
        final Object[] vals;
        Row(Object[] vals) { this.vals = vals; }
        public int size() { return vals.length; }
        public boolean isNull(int i) { return vals[i] == null; }
        public Boolean getBoolean(int i) { return (Boolean) vals[i]; }
        public Integer getInt(int i) { return vals[i] instanceof Integer ? (Integer) vals[i] : null; }
        public Long getLong(int i) {
            if (vals[i] instanceof Long) return (Long) vals[i];
            if (vals[i] instanceof Integer) return ((Integer) vals[i]).longValue();
            return null;
        }
        public Float getFloat(int i) { return (Float) vals[i]; }
        public Double getDouble(int i) { return (Double) vals[i]; }
        public String getString(int i) { return vals[i] == null ? null : vals[i].toString(); }
        public Instant getInstant(int i) { return (Instant) vals[i]; }
        public java.time.LocalDateTime getLocalDateTime(int i) {
            return vals[i] instanceof java.time.LocalDateTime ? (java.time.LocalDateTime) vals[i] : null;
        }
        public java.time.LocalDate getLocalDate(int i) {
            return vals[i] instanceof java.time.LocalDate ? (java.time.LocalDate) vals[i] : null;
        }
        public HakunaGeometry getHakunaGeometry(int i) { return (HakunaGeometry) vals[i]; }
        public Object[] getArray(int i) { return null; }
        public java.util.UUID getUUID(int i) { return null; }
        public Object getObject(int i) { return vals[i]; }
    }

    private static final class StubStream implements FeatureStream {
        final Iterator<Row> it;
        StubStream(List<Row> rows) { this.it = rows.iterator(); }
        public boolean hasNext() { return it.hasNext(); }
        public ValueProvider next() { return it.next(); }
        public void close() {}
    }

    private static final class StubProperty implements HakunaProperty {
        final String name; final HakunaPropertyType type;
        StubProperty(String name, HakunaPropertyType type) { this.name = name; this.type = type; }
        public String getName() { return name; }
        public String getTable() { return null; }
        public String getColumn() { return name; }
        public HakunaPropertyType getType() { return type; }
        public boolean nullable() { return true; }
        public boolean unique() { return false; }
        public io.swagger.v3.oas.models.media.Schema<?> getSchema() { return null; }
        public boolean isStatic() { return false; }
        public FeatureType getFeatureType() { return null; }
        public void setFeatureType(FeatureType ft) {}
        public Object getValue(ValueProvider p, int i) { return null; }
        public int getDimension() { return 0; }
        public fi.nls.hakunapi.core.property.HakunaPropertyWriter getPropertyWriter() { return null; }
        public fi.nls.hakunapi.core.ValueMapper getMapper(
                Map<fi.nls.hakunapi.core.util.StringPair, Integer> columnToIndex,
                int out, fi.nls.hakunapi.core.QueryContext ctx) { return null; }
        public Object toInner(String value) { return value; }
    }

    private static final class StubFeatureType implements FeatureType {
        final String name;
        final HakunaPropertyGeometry geom;
        final HakunaProperty id;
        final List<HakunaProperty> props;
        final List<Row> rows = new ArrayList<>();
        int queries;

        StubFeatureType(String name, HakunaGeometryType geomType, List<HakunaProperty> attrs) {
            this.name = name;
            this.id = new StubProperty("id", HakunaPropertyType.LONG);
            this.geom = new HakunaPropertyGeometry("geom", "t", "geom", true,
                    geomType, new int[] {SRID}, SRID, 2, NOOP_WRITER);
            this.props = new ArrayList<>();
            this.props.add(this.geom);
            this.props.addAll(attrs);
        }

        void addFeature(Long fid, HakunaGeometry g, Object... attrs) {
            Object[] vals = new Object[3 + attrs.length];
            vals[0] = fid;
            vals[1] = g;
            vals[2] = g;
            System.arraycopy(attrs, 0, vals, 3, attrs.length);
            rows.add(new Row(vals));
        }

        public String getName() { return name; }
        public String getNS() { return null; }
        public String getSchemaLocation() { return null; }
        public String getTitle() { return name; }
        public String getDescription() { return null; }
        public Map<String, Object> getMetadata() { return Collections.emptyMap(); }
        public HakunaProperty getId() { return id; }
        public HakunaPropertyGeometry getGeom() { return geom; }
        public List<HakunaProperty> getProperties() { return props; }
        public List<HakunaProperty> getQueryableProperties() { return props; }
        public List<DatetimeProperty> getDatetimeProperties() { return Collections.emptyList(); }
        public double[] getSpatialExtent() { return null; }
        public Instant[] getTemporalExtent() { return null; }
        public List<GetFeatureParam> getParameters() { return Collections.emptyList(); }
        public List<Filter> getStaticFilters() { return Collections.emptyList(); }
        public ProjectionTransformerFactory getProjectionTransformerFactory() { return null; }
        public PaginationStrategy getPaginationStrategy() { return null; }
        public List<OrderBy> getDefaultOrderBy() { return Collections.emptyList(); }
        public FeatureProducer getFeatureProducer() {
            return new FeatureProducer() {
                public int getNumberMatched(GetFeatureRequest r, GetFeatureCollection c) { return rows.size(); }
                public FeatureStream getFeatures(GetFeatureRequest r, GetFeatureCollection c) {
                    queries++;
                    return new StubStream(rows);
                }
            };
        }
    }
}
