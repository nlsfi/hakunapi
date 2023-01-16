package fi.nls.hakunapi.filter;

import static fi.nls.hakunapi.core.filter.Filter.DENY;
import static fi.nls.hakunapi.core.filter.Filter.PASS;
import static fi.nls.hakunapi.core.filter.Filter.and;
import static fi.nls.hakunapi.core.filter.Filter.equalTo;
import static fi.nls.hakunapi.core.filter.Filter.greaterThan;
import static fi.nls.hakunapi.core.filter.Filter.greaterThanOrEqualTo;
import static fi.nls.hakunapi.core.filter.Filter.isNotNull;
import static fi.nls.hakunapi.core.filter.Filter.isNull;
import static fi.nls.hakunapi.core.filter.Filter.lessThan;
import static fi.nls.hakunapi.core.filter.Filter.lessThanThanOrEqualTo;
import static fi.nls.hakunapi.core.filter.Filter.notEqualTo;
import static fi.nls.hakunapi.core.filter.Filter.or;
import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;

import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.SimpleFeatureType;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.core.property.HakunaPropertyWriters;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyDate;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyInt;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyString;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyTimestamptz;
import fi.nls.hakunapi.core.util.EmptyFeatureProducer;

public class GeoToolsFilter2HakunaFilterTest {

    private static HakunaPropertyInt foo;
    private static HakunaPropertyString bar;
    private static HakunaPropertyGeometry baz;
    private static HakunaPropertyTimestamptz qux;
    private static HakunaPropertyDate buu;
    private static GeoToolsFilter2HakunaFilter mapper;


    @BeforeClass
    public static void init() {
        SimpleFeatureType ft = new SimpleFeatureType() {
            @Override
            public FeatureProducer getFeatureProducer() {
                return new EmptyFeatureProducer();
            }
        };
        foo = new HakunaPropertyInt("foo", "table", "foo", true, false, HakunaPropertyWriters.HIDDEN);
        bar = new HakunaPropertyString("bar", "table", "bar", true, false, HakunaPropertyWriters.HIDDEN);
        baz = new HakunaPropertyGeometry("baz", "table", "baz", true, HakunaGeometryType.LINESTRING, new int[] { 84 }, 84, 2, HakunaPropertyWriters.HIDDEN);
        qux = new HakunaPropertyTimestamptz("qux", "table", "qux", true, false, HakunaPropertyWriters.HIDDEN);
        buu = new HakunaPropertyDate("buu", "table", "buu", true, false, HakunaPropertyWriters.HIDDEN, GeoToolsFilter2HakunaFilterTest::parseLocalDate);
        ft.setQueryableProperties(Arrays.asList(foo, bar, baz, qux, buu));
        mapper = new GeoToolsFilter2HakunaFilter(ft, 84);
    }

    private static LocalDate parseLocalDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.ofInstant(Instant.parse(value), ZoneId.systemDefault()).toLocalDate();
            } catch (Exception e2) {
                throw e;
            }
        }
    }

    @Test
    public void testComparisonOperators() throws CQLException {
        assertEquals(equalTo(foo, "42"), mapper.map(ECQL.toFilter("foo = 42")));
        assertEquals(equalTo(foo, "42"), mapper.map(ECQL.toFilter("42 = foo")));

        assertEquals(notEqualTo(foo, "-99"), mapper.map(ECQL.toFilter("foo <> -99")));
        assertEquals(notEqualTo(foo, "-99"), mapper.map(ECQL.toFilter("-99 <> foo")));

        assertEquals(greaterThan(foo, "10"), mapper.map(ECQL.toFilter("foo > 10")));
        assertEquals(greaterThan(foo, "10"), mapper.map(ECQL.toFilter("10 < foo")));

        assertEquals(greaterThanOrEqualTo(foo, "1337"), mapper.map(ECQL.toFilter("foo >= 1337")));
        assertEquals(greaterThanOrEqualTo(foo, "1337"), mapper.map(ECQL.toFilter("1337 <= foo")));

        assertEquals(lessThanThanOrEqualTo(foo, "4321"), mapper.map(ECQL.toFilter("foo <= 4321")));
        assertEquals(lessThanThanOrEqualTo(foo, "4321"), mapper.map(ECQL.toFilter("4321 >= foo")));

        assertEquals(lessThan(foo, "1234"), mapper.map(ECQL.toFilter("foo < 1234")));
        assertEquals(lessThan(foo, "1234"), mapper.map(ECQL.toFilter("1234 > foo")));

        assertEquals(PASS, mapper.map(ECQL.toFilter("-1 < 1")));
        assertEquals(PASS, mapper.map(ECQL.toFilter("1 <> 10")));
        assertEquals(PASS, mapper.map(ECQL.toFilter("1 >= 1")));
        assertEquals(PASS, mapper.map(ECQL.toFilter("5 <= 10")));

        assertEquals(DENY, mapper.map(ECQL.toFilter("-1 > 1")));
        assertEquals(DENY, mapper.map(ECQL.toFilter("1 = 10")));
        assertEquals(DENY, mapper.map(ECQL.toFilter("1 >= 4")));
        assertEquals(DENY, mapper.map(ECQL.toFilter("10 <= 5")));

        assertEquals(equalTo(buu, "2000-01-15"), mapper.map(ECQL.toFilter("buu = 2000-01-15")));
        assertEquals(equalTo(buu, "2000-01-15"), mapper.map(ECQL.toFilter("buu = 2000-01-15T00:00:00Z")));
    }

    @Test
    public void testIsNull() throws CQLException {
        assertEquals(DENY, mapper.map(ECQL.toFilter("'foo' IS NULL")));
        assertEquals(PASS, mapper.map(ECQL.toFilter("'foo' IS NOT NULL")));

        assertEquals(isNull(foo), mapper.map(ECQL.toFilter("foo IS NULL")));
        assertEquals(isNull(foo), mapper.map(ECQL.toFilter("\"foo\" IS NULL")));

        assertEquals(isNotNull(foo), mapper.map(ECQL.toFilter("foo IS NOT NULL")));
        assertEquals(isNotNull(foo), mapper.map(ECQL.toFilter("\"foo\" IS NOT NULL")));
    }

    @Test
    public void testBetween() throws CQLException {
        assertEquals(and(greaterThanOrEqualTo(foo, "100"), lessThanThanOrEqualTo(foo, "200")), mapper.map(ECQL.toFilter("foo BETWEEN 100 AND 200")));
    }

    @Test
    public void testIn() throws CQLException {
        Filter expected = or(Arrays.asList(
                equalTo(foo, "1"),
                equalTo(foo, "2"),
                equalTo(foo, "3")
                ));
        assertEquals(expected, mapper.map(ECQL.toFilter("foo IN (1,2,3)")));
        assertEquals(expected, mapper.map(ECQL.toFilter("foo IN (1,2,3,2,3,2,3)")));
        assertEquals(equalTo(foo, "3"), mapper.map(ECQL.toFilter("foo IN (3)")));
        assertEquals(equalTo(foo, "3"), mapper.map(ECQL.toFilter("foo IN (3,3,3)")));
        assertEquals(equalTo(foo, "3"), mapper.map(ECQL.toFilter("foo IN (3,3,3)")));
    }

    @Test
    public void testLike() throws CQLException {
        Filter expected, actual;

        actual = mapper.map(ECQL.toFilter("bar LIKE 'baz'"));
        expected = Filter.like(bar, "baz", '%', '_', '\\', false);
        assertEquals(expected, actual);
    }

    @Test
    public void testArithmeticExpressions() throws CQLException {
        Filter expected, actual;

        actual = mapper.map(ECQL.toFilter("300 + 100 < 200"));
        expected = Filter.DENY;
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("20 * 200 > 300 - 100 + 300"));
        expected = Filter.PASS;
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("foo < 200 - 100"));
        expected = Filter.lessThan(foo, "100");
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("foo - 100 < 200"));
        expected = Filter.lessThan(foo, "300");
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("foo * 3 < 600"));
        expected = Filter.lessThan(foo, "200");
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("30 * 3 < 300 + foo"));
        // <=> 90 < 300 + foo
        // <=> -210 < foo
        // <=> foo > -210
        expected = Filter.greaterThan(foo, "-210");
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("100 - foo < 10"));
        // <=> 100 < 10 + foo
        // <=> 90 < foo
        // <=> foo > 90
        expected = Filter.greaterThan(foo, "90");
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("10 > 100 - foo"));
        // <=> 100 < 10 + foo
        // <=> 90 < foo
        // <=> foo > 90
        expected = Filter.greaterThan(foo, "90");
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("-100 + 200 + foo < 200 - 50"));
        expected = Filter.lessThan(foo, "50");
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("-100 + foo + 200 < 200 - 50"));
        expected = Filter.lessThan(foo, "50");
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("100 * foo + 200 < 300"));
        // <=> 100 * foo < 100
        // <=> foo < 1
        expected = Filter.lessThan(foo, "1");
        assertEquals(expected, actual);
    }

    @Test
    public void testSpatial() throws Exception {
        Filter expected, actual;
        String wkt = "POLYGON((0 0, 0 10, 10 10, 10 0, 0 0))";
        Geometry poly = new WKTReader().read(wkt);

        actual = mapper.map(ECQL.toFilter("INTERSECTS(baz, " + wkt + ")"));
        expected = Filter.intersects(baz, poly);
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("EQUALS(baz, " + wkt + ")"));
        expected = Filter.equals(baz, poly);
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("DISJOINT(baz, " + wkt + ")"));
        expected = Filter.disjoint(baz, poly);
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("TOUCHES(baz, " + wkt + ")"));
        expected = Filter.touches(baz, poly);
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("WITHIN(baz, " + wkt + ")"));
        expected = Filter.within(baz, poly);
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("OVERLAPS(baz, " + wkt + ")"));
        expected = Filter.overlaps(baz, poly);
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("CROSSES(baz, " + wkt + ")"));
        expected = Filter.crosses(baz, poly);
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("CONTAINS(baz, " + wkt + ")"));
        expected = Filter.contains(baz, poly);
        assertEquals(expected, actual);

        // Now reverse (b, a)

        actual = mapper.map(ECQL.toFilter("INTERSECTS(" + wkt + ", baz)"));
        expected = Filter.intersects(baz, poly);
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("EQUALS(" + wkt + ", baz)"));
        expected = Filter.equals(baz, poly);
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("DISJOINT(" + wkt + ", baz)"));
        expected = Filter.disjoint(baz, poly);
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("TOUCHES(" + wkt + ", baz)"));
        expected = Filter.touches(baz, poly);
        assertEquals(expected, actual);

        // within(b, a) <=> contains(a, b);
        actual = mapper.map(ECQL.toFilter("WITHIN(" + wkt + ", baz)"));
        expected = Filter.contains(baz, poly);
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("OVERLAPS(" + wkt + ", baz)"));
        expected = Filter.overlaps(baz, poly);
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("CROSSES(" + wkt + ", baz)"));
        expected = Filter.crosses(baz, poly);
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("CONTAINS(" + wkt + ", baz)"));
        expected = Filter.within(baz, poly);
        assertEquals(expected, actual);

        // Now with Buffer

        actual = mapper.map(ECQL.toFilter("INTERSECTS(baz, buffer(" + wkt + ", 10))"));
        expected = Filter.intersects(baz, poly.buffer(10));
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("EQUALS(baz, buffer(" + wkt + ", 0.5))"));
        expected = Filter.equals(baz, poly.buffer(0.5));
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("DISJOINT(baz, buffer(" + wkt + ", 1))"));
        expected = Filter.disjoint(baz, poly.buffer(1));
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("TOUCHES(baz, buffer(" + wkt + ", 5))"));
        expected = Filter.touches(baz, poly.buffer(5));
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("WITHIN(baz, buffer(" + wkt + ", 13))"));
        expected = Filter.within(baz, poly.buffer(13));
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("OVERLAPS(baz, buffer(" + wkt + ", 15))"));
        expected = Filter.overlaps(baz, poly.buffer(15));
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("CROSSES(baz, buffer(" + wkt + ", 40))"));
        expected = Filter.crosses(baz, poly.buffer(40));
        assertEquals(expected, actual);

        actual = mapper.map(ECQL.toFilter("CONTAINS(baz, buffer(" + wkt + ", 8))"));
        expected = Filter.contains(baz, poly.buffer(8));
        assertEquals(expected, actual);
    }

    @Test
    @Ignore("GeoTools CQL/ECQL parser doesn't support this")
    public void testAnyInteracts() throws CQLException {
        Filter expected, actual;

        actual = mapper.map(ECQL.toFilter("qux ANYINTERACTS 2020-01-01T00:00:00Z/2020-01-02T00:00:00Z"));
        expected = Filter.and(
                Filter.greaterThanOrEqualTo(qux, "2020-01-01T00:00:00Z"),
                Filter.lessThanThanOrEqualTo(qux, "2020-01-02T00:00:00Z")
        );
        assertEquals(expected, actual);
    }

}
