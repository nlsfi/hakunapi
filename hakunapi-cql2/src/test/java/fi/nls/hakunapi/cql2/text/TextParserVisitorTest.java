package fi.nls.hakunapi.cql2.text;

import static fi.nls.hakunapi.cql2.text.CQL2Text.parse;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import fi.nls.hakunapi.cql2.model.ExpressionToString;

public class TextParserVisitorTest {
    
    private ExpressionToString toString;
    
    @Before
    public void init() {
        toString = new ExpressionToString();
    }

    @Test
    public void testCombiningLogicalExpressions() {
        String input = "foo < +12. AND myprop IS Null OR dog = 'baf' AND ns1:bar.z_2 < 'dog <> bark' AnD (\"bAz\" = true oR qux = false) ANd foo1 <> -.5 AND baz IN ('dog', 'pants') AND foo2 < -2 AND street = 'Tarkk''ampujankatu'";
        parse(input).accept(toString);
        String expected = "(\"foo\" < 12.0 AND \"myprop\" IS NULL) OR (\"dog\" = 'baf' AND \"ns1:bar.z_2\" < 'dog <> bark' AND (\"bAz\" = true OR \"qux\" = false) AND \"foo1\" <> -0.5 AND (\"baz\" = 'dog' OR \"baz\" = 'pants') AND \"foo2\" < -2.0 AND \"street\" = 'Tarkk''ampujankatu')";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testTimestamp() {
        String timestamp = "foo < TIMESTAMP('2011-01-01T05:00:00.123456789Z')";
        parse(timestamp).accept(toString);
        String expected = "\"foo\" < '2011-01-01T05:00:00.123456789Z'";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testDate() {
        String date = "foo < dAtE('2011-01-01')";
        parse(date).accept(toString);
        String expected = "\"foo\" < '2011-01-01'";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testBetween() {
        String between = "foo BETWEEN 1 AND 3";
        parse(between).accept(toString);
        String expected = "\"foo\" >= 1.0 AND \"foo\" <= 3.0";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testNotBetween() {
        String notBetween = "foo NOT BETWEEN 1 AND 3";
        parse(notBetween).accept(toString);
        String expected = "\"foo\" < 1.0 OR \"foo\" > 3.0";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testIn() {
        String in = "\"baz\" in ('dog', 'pants')";
        parse(in).accept(toString);
        String expected = "\"baz\" = 'dog' OR \"baz\" = 'pants'";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testNotIn() {
        String notIn = "\"baz\" NOT in ('dog', 'pants')";
        parse(notIn).accept(toString);
        String expected = "\"baz\" <> 'dog' AND \"baz\" <> 'pants'";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testSIntersectsPoint() {
        String intersects_point = "s_intersects(footprint, point (43.5845 -79.5442))";
        parse(intersects_point).accept(toString);
        String expected = "S_INTERSECTS(\"footprint\", POINT (43.5845 -79.5442))";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testSIntersectsLinestring() {    
        String intersects_line = "s_intersects(footprint, lineString (43.5845 -79.5442, 43.6079 -79.4893))";
        parse(intersects_line).accept(toString);
        String expected = "S_INTERSECTS(\"footprint\", LINESTRING (43.5845 -79.5442, 43.6079 -79.4893))";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testSIntersectsPolygon() {
        String intersects_polygon = "s_INTERSECts(footprint, PolyGon ((43.5845 -79.5442, 43.6079 -79.4893, 43.5677 -79.4632, 43.6129 -79.3925, 43.6223 -79.3238, 43.6576 -79.3163, 43.7945 -79.1178, 43.8144 -79.1542, 43.8555 -79.1714, 43.7509 -79.6390, 43.5845 -79.5442)))";
        parse(intersects_polygon).accept(toString);
        String expected = "S_INTERSECTS(\"footprint\", POLYGON ((43.5845 -79.5442, 43.6079 -79.4893, 43.5677 -79.4632, 43.6129 -79.3925, 43.6223 -79.3238, 43.6576 -79.3163, 43.7945 -79.1178, 43.8144 -79.1542, 43.8555 -79.1714, 43.7509 -79.639, 43.5845 -79.5442)))";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testCrossesPolygon() {
        String crosses_polygon = "S_CROSSES(footprint, PolyGon ((43.5845 -79.5442, 43.6079 -79.4893, 43.5677 -79.4632, 43.6129 -79.3925, 43.6223 -79.3238, 43.6576 -79.3163, 43.7945 -79.1178, 43.8144 -79.1542, 43.8555 -79.1714, 43.7509 -79.6390, 43.5845 -79.5442)))";
        parse(crosses_polygon).accept(toString);
        String expected = "S_CROSSES(\"footprint\", POLYGON ((43.5845 -79.5442, 43.6079 -79.4893, 43.5677 -79.4632, 43.6129 -79.3925, 43.6223 -79.3238, 43.6576 -79.3163, 43.7945 -79.1178, 43.8144 -79.1542, 43.8555 -79.1714, 43.7509 -79.639, 43.5845 -79.5442)))";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testSIntersectsEnvelope() {        
        String intersects_envelope = "S_Intersects(footprint, envelope (43.5845 -79.5442, 43.6079 -79.7893))";
        parse(intersects_envelope).accept(toString);
        String expected = "S_INTERSECTS(\"footprint\", POLYGON ((43.5845 -79.5442, 43.6079 -79.5442, 43.6079 -79.7893, 43.5845 -79.7893, 43.5845 -79.5442)))";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testIntersectsBufferLine() {
        String intersects_buffer_line = "S_Intersects(footprint, BUFFER( lineString (43.5845 -79.5442, 43.6079 -79.4893), 10))";
        parse(intersects_buffer_line).accept(toString);
        String expected = "S_INTERSECTS(\"footprint\", BUFFER(LINESTRING (43.5845 -79.5442, 43.6079 -79.4893), 10.0))";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testRandomFunctionCall() {
        String function_call = "foo > foo(1, 2, 3)";
        parse(function_call).accept(toString);
        String expected = "\"foo\" > foo(1.0, 2.0, 3.0)";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }


}
