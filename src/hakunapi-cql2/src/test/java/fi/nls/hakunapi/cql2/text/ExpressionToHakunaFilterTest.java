package fi.nls.hakunapi.cql2.text;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.locationtech.jts.algorithm.Centroid;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.util.List;

import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.filter.FilterOp;
import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyWriters;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.schemas.FunctionArgumentInfo.FunctionArgumentType;
import fi.nls.hakunapi.core.schemas.FunctionReturnsInfo.FunctionReturnsType;
import fi.nls.hakunapi.cql2.function.CQL2Functions;
import fi.nls.hakunapi.cql2.function.Function;
import fi.nls.hakunapi.cql2.function.FunctionTable;
import fi.nls.hakunapi.cql2.model.Expression;
import fi.nls.hakunapi.cql2.model.ExpressionToHakunaFilter;
import fi.nls.hakunapi.cql2.model.FilterContext;

public class ExpressionToHakunaFilterTest {

    @BeforeClass
    public static void init() {
        Function buffer = Function.of("buffer", (fn, args, ctx) -> {
            Geometry g = (Geometry) args.get(0);
            double distance = ((Number) args.get(1)).doubleValue();
            return g.buffer(distance);
        })
        .argument("geom", FunctionArgumentType.geometry)
        .argument("distance", FunctionArgumentType.number)
        .returns(FunctionReturnsType.geometry);

        Function centroid = Function.of("centroid", (fn, args, ctx) -> {
            Geometry g = (Geometry) args.get(0);
            return g.getFactory().createPoint(Centroid.getCentroid(g));
        })
        .argument("geom", FunctionArgumentType.geometry)
        .returns(FunctionReturnsType.geometry);

        CQL2Functions.INSTANCE.init(List.of(FunctionTable.of("geometryTests", buffer, centroid)));
    }

    @Test
    public void testNestedFunctionCall() {
        int[] srid = new int[] { 4326 };
        int storageSrid = srid[0];
        HakunaPropertyGeometry footprint = new HakunaPropertyGeometry("footprint", "any", "any", true,
                HakunaGeometryType.POLYGON, srid, storageSrid, 2, HakunaPropertyWriters.HIDDEN);
        List<HakunaProperty> queryables = List.of(footprint);

        String filter = "S_Intersects(footprint, BUFFER(cenTroid( lineString (0 0, 100 100)), 10))";
        SRIDCode filterSrid = new SRIDCode(4326, true, true, HakunaGeometryDimension.XY);

        FilterContext ctx = new FilterContext(queryables, filterSrid);

        Expression expr = CQL2Text.parse(filter);
        Filter f = (Filter) new ExpressionToHakunaFilter().visit(expr, ctx);

        assertEquals("footprint", f.getProp().getName());
        assertEquals(FilterOp.INTERSECTS, f.getOp());

        Geometry bufferedCentroid = (Geometry) f.getValue();
        Envelope actual = bufferedCentroid.getEnvelopeInternal();
        Envelope expected = new Envelope(40, 60, 40, 60);
        assertEquals(expected, actual);
    }

}
