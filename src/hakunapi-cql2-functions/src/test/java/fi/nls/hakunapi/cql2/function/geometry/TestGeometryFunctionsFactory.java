package fi.nls.hakunapi.cql2.function.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import fi.nls.hakunapi.cql2.function.Function;
import fi.nls.hakunapi.cql2.function.FunctionTable;

public class TestGeometryFunctionsFactory {

    @Test
    public void testGeometryFunctionsBuffer() {
        GeometryFactory geomFactory = new GeometryFactory();
        GeometryFunctionsFactory factory = new GeometryFunctionsFactory();

        List<FunctionTable> functionTables = factory.createFunctionTables();

        assertEquals(functionTables.size(), 1);
        Function buffer = functionTables.get(0).getFunction("Buffer");
        assertNotNull(buffer);

        LineString geomFrom = geomFactory
                .createLineString(new Coordinate[] { new Coordinate(0, 0), new Coordinate(1, 1) });

        Object rv = buffer.invoke(List.of(geomFrom, 1), null);

        assertNotNull(rv);
        assertTrue(rv instanceof Polygon);
    }

    @Test
    public void testGeometryFunctionsSTBuffer() {
        GeometryFactory geomFactory = new GeometryFactory();
        GeometryFunctionsFactory factory = new GeometryFunctionsFactory();

        List<FunctionTable> functionTables = factory.createFunctionTables();

        assertEquals(functionTables.size(), 1);
        Function st_buffer = functionTables.get(0).getFunction("ST_Buffer");
        assertNotNull(st_buffer);

        LineString geomFrom = geomFactory
                .createLineString(new Coordinate[] { new Coordinate(0, 0), new Coordinate(1, 1) });

        Object rv = st_buffer.invoke(List.of(geomFrom, 1, ""), null);

        assertNotNull(rv);
        assertTrue(rv instanceof Polygon);
    }
    
    @Test
    public void testGeometryFunctionsSTBufferMiter() {
        GeometryFactory geomFactory = new GeometryFactory();
        GeometryFunctionsFactory factory = new GeometryFunctionsFactory();

        List<FunctionTable> functionTables = factory.createFunctionTables();

        assertEquals(functionTables.size(), 1);
        Function st_buffer = functionTables.get(0).getFunction("ST_Buffer");
        assertNotNull(st_buffer);

        LineString geomFrom = geomFactory
                .createLineString(new Coordinate[] { new Coordinate(0, 0), new Coordinate(1, 1) });

        Object rv = st_buffer.invoke(List.of(geomFrom, 1, "join=miter"), null);

        assertNotNull(rv);
        assertTrue(rv instanceof Polygon);
    }

}
