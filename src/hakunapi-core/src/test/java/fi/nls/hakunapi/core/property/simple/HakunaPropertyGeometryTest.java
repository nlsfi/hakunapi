package fi.nls.hakunapi.core.property.simple;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.function.BiConsumer;

import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ByteOrderValues;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;

import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.ObjectArrayValueContainer;
import fi.nls.hakunapi.core.QueryContext;
import fi.nls.hakunapi.core.SimpleFeatureType;
import fi.nls.hakunapi.core.ValueContainer;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.geom.HakunaGeometryEWKB;
import fi.nls.hakunapi.core.projection.JTSTransformer;
import fi.nls.hakunapi.core.projection.ProjectionTransformer;
import fi.nls.hakunapi.core.projection.ProjectionTransformerFactory;
import fi.nls.hakunapi.core.property.HakunaPropertyWriters;

public class HakunaPropertyGeometryTest {

    private static final WKTReader WKT = new WKTReader();

    private static final ProjectionTransformer SHIFT = new ProjectionTransformer() {
        @Override
        public int getDimension() {
            return 2;
        }

        @Override
        public void transformInPlace(double[] coords, int off, int n) {
            for (int i = off; i < off + 2 * n; i += 2) {
                coords[i] = coords[i] * 2 + 1000;
                coords[i + 1] = coords[i + 1] * 3 - 500;
            }
        }

        @Override
        public int getFromSRID() {
            return 3067;
        }

        @Override
        public int getToSRID() {
            return 3857;
        }
    };

    @Test
    public void testAddDefaultSridsWith84And4326Missing() {
        int[] input = { 1234, 5432, 1337 };
        int[] actuals = HakunaPropertyGeometry.addDefaultSrids(input);
        int[] expecteds = { 84, 4326, 1234, 5432, 1337 };
        assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void testAddDefaultSridsWithBoth84And4326() {
        int[] input = { 1234, 5432, 4326, 84, 1337 };
        int[] actuals = HakunaPropertyGeometry.addDefaultSrids(input);
        int[] expecteds = { 84, 4326, 1234, 5432, 1337 };
        assertArrayEquals(expecteds, actuals);
        assertTrue(actuals != expecteds); // Make sure it's still defensively copied
    }

    @Test
    public void testAddDefaultSridsWith84MissingButWith4326Present() {
        int[] input = { 4326, 1234, 5432, 1337 };
        int[] actuals = HakunaPropertyGeometry.addDefaultSrids(input);
        int[] expecteds = { 84, 4326, 1234, 5432, 1337 };
        assertArrayEquals(expecteds, actuals);
        assertTrue(actuals != expecteds); // Make sure it's still defensively copied
    }

    @Test
    public void testAddDefaultSridsWithDuplicates() {
        int[] input = { 4326, 1234, 4326, 1234, 5432, 1337, 4326 };
        int[] actuals = HakunaPropertyGeometry.addDefaultSrids(input);
        int[] expecteds = { 84, 4326, 1234, 5432, 1337 };
        assertArrayEquals(expecteds, actuals);
        assertTrue(actuals != expecteds); // Make sure it's still defensively copied
    }

    @Test
    public void geometryCollectionTakesJtsRouteAndSimpleGeometriesStayInPlace() throws Exception {
        SimpleFeatureType ft = new SimpleFeatureType() {
            @Override
            public FeatureProducer getFeatureProducer() {
                return null;
            }
        };
        ft.setProjectionTransformerFactory(new ProjectionTransformerFactory() {
            @Override
            public ProjectionTransformer getTransformer(int fromSRID, int toSRID) {
                return SHIFT;
            }

            @Override
            public ProjectionTransformer toCRS84(int fromSRID) {
                throw new UnsupportedOperationException();
            }

            @Override
            public ProjectionTransformer fromCRS84(int toSRID) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getWKT(int srid) {
                throw new UnsupportedOperationException();
            }
        });
        HakunaPropertyGeometry prop = new HakunaPropertyGeometry("geometry", "table", "geom", false, null,
                new int[0], 3067, 2, HakunaPropertyWriters.HIDDEN);
        prop.setFeatureType(ft);

        QueryContext ctx = new QueryContext();
        ctx.setSRID(3857);
        BiConsumer<ValueProvider, ValueContainer> mapper = prop.getMapperFunction(0, 0, ctx);

        String collection = "GEOMETRYCOLLECTION(POINT(30 10),LINESTRING(30 10,10 30,40 40))";
        HakunaGeometryEWKB gc = ewkb(collection);
        Geometry gcExpected = WKT.read(collection);
        gcExpected.apply(new JTSTransformer(SHIFT));
        HakunaGeometry gcOut = reproject(mapper, gc);
        assertNotSame("GEOMETRYCOLLECTION takes the JTS route", gc, gcOut);
        assertTrue("GEOMETRYCOLLECTION coordinates", gcExpected.equalsExact(gcOut.toJTSGeometry()));

        for (String wkt : new String[] { "POINT(30 10)", "LINESTRING(30 10,10 30,40 40)" }) {
            Geometry expected = WKT.read(wkt);
            expected.apply(new JTSTransformer(SHIFT));
            HakunaGeometryEWKB geom = ewkb(wkt);
            HakunaGeometry out = reproject(mapper, geom);
            assertSame(wkt + " reprojected in place", geom, out);
            assertTrue(wkt + " coordinates", expected.equalsExact(out.toJTSGeometry()));
        }
    }

    private static HakunaGeometryEWKB ewkb(String wkt) throws ParseException {
        return new HakunaGeometryEWKB(new WKBWriter(2, ByteOrderValues.LITTLE_ENDIAN).write(WKT.read(wkt)));
    }

    private static HakunaGeometry reproject(BiConsumer<ValueProvider, ValueContainer> mapper, HakunaGeometry hg) {
        ObjectArrayValueContainer in = ObjectArrayValueContainer.wrap(new Object[] { hg });
        ObjectArrayValueContainer out = new ObjectArrayValueContainer(1);
        mapper.accept(in, out);
        return out.getHakunaGeometry(0);
    }

}
