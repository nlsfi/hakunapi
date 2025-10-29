package fi.nls.hakunapi.cql2.text;

import static fi.nls.hakunapi.cql2.text.CQL2Text.parse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.Before;
import org.junit.Test;

public class TextParserVisitorTest {

    private ExpressionToString toString;

    @Before
    public void init() {
        toString = new ExpressionToString();
    }

    @Test
    public void testCombiningLogicalExpressions() {
        String input = "foo < +12. AND myprop IS Null OR dog = 'baf' AND ns1:bar.z_2 < 'dog <> bark' AnD (bAz = true oR qux = false) ANd foo1 <> -.5 AND baz IN ('dog', 'pants') AND foo2 < -2 AND street = 'Tarkk''ampujankatu'";
        parse(input).accept(toString, null);
        String expected = "(foo < 12.0 AND myprop IS NULL) OR (dog = baf AND ns1:bar.z_2 < dog <> bark AND (bAz = true OR qux = false) AND foo1 <> -0.5 AND (baz = dog OR baz = pants) AND foo2 < -2.0 AND street = Tarkk'ampujankatu)";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testPrecedence() {
        String input = "foo < 0 OR (bar > 0 AND (baz > 0 OR qux < 0))";
        parse(input).accept(toString, null);
        String expected = "foo < 0.0 OR (bar > 0.0 AND (baz > 0.0 OR qux < 0.0))";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testEscapedQuoteWithDoubleSingleQuote() {
        String input = "name = 'Tarkk''ampujankatu'";
        parse(input).accept(toString, null);
        String expected = "name = Tarkk'ampujankatu";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testNoSpaceBetweenOperatorAndOperands() {
        String input = "component_AdminUnitName_4='MyTest'";
        parse(input).accept(toString, null);
        String expected = "component_AdminUnitName_4 = MyTest";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testEscapedQuoteWithBackslash() {
        String input = "name = 'Tarkk\\'ampujankatu'";
        parse(input).accept(toString, null);
        String expected = "name = Tarkk'ampujankatu";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testEscapedQuoteWithAdditionalBackslash() {
        String input = "name = 'Tarkk\\\\'ampujankatu'";
        parse(input).accept(toString, null);
        String expected = "name = Tarkk\\'ampujankatu";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testEscapedQuoteWithTripleSingleQuote() {
        String input = "name = 'Tarkk'''ampujankatu'";
        assertThrows(ParseCancellationException.class, () -> parse(input));
    }

    @Test
    public void testEscapedQuoteStartsWith() {
        String input = "greeting = '''ello there mate!'";
        parse(input).accept(toString, null);
        String expected = "greeting = 'ello there mate!";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testEscapedQuoteEndsWith() {
        String input = "possessive_apostrophe = 'yes, fairies'''";
        parse(input).accept(toString, null);
        String expected = "possessive_apostrophe = yes, fairies'";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testMultipleAnd() {
        String input = "foo < 0 AND bar > 0 AND baz > 0 AND qux < 0";
        parse(input).accept(toString, null);
        String expected = "foo < 0.0 AND bar > 0.0 AND baz > 0.0 AND qux < 0.0";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testTimestampMicros() {
        String timestamp = "foo < TIMESTAMP('2011-01-01T05:00:00.123456789Z')";
        parse(timestamp).accept(toString, null);
        String expected = "foo < 2011-01-01T05:00:00.123456789Z";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testTimestamp() {
        String timestamp = "foo < TIMESTAMP('2011-01-01T05:00:00Z')";
        parse(timestamp).accept(toString, null);
        String expected = "foo < 2011-01-01T05:00:00Z";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testDate() {
        String date = "foo < dAtE('2011-01-01')";
        parse(date).accept(toString, null);
        String expected = "foo < 2011-01-01";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testBetween() {
        String between = "foo BETWEEN 1 AND 3";
        parse(between).accept(toString, null);
        String expected = "foo >= 1.0 AND foo <= 3.0";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testNotBetween() {
        String notBetween = "foo NOT BETWEEN 1 AND 3";
        parse(notBetween).accept(toString, null);
        String expected = "foo < 1.0 OR foo > 3.0";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testLike() {
        String input = "baz LIKE 'my_pattern%'";
        parse(input).accept(toString, null);
        String expected = "baz LIKE my_pattern%";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testLikeCaseiValue() {
        String input = "baz LIKE casei('my_pattern%')";
        parse(input).accept(toString, null);
        String expected = "baz ILIKE my_pattern%";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testLikeCaseiProperty() {
        String input = "casei(baz) LIKE casei('my_pattern%')";
        parse(input).accept(toString, null);
        String expected = "baz ILIKE my_pattern%";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testLikeCaseiBoth() {
        String input = "casei(baz) LIKE casei('my_pattern%')";
        parse(input).accept(toString, null);
        String expected = "baz ILIKE my_pattern%";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testIn() {
        String in = "baz in ('dog', 'pants')";
        parse(in).accept(toString, null);
        String expected = "baz = dog OR baz = pants";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testNotIn() {
        String notIn = "baz NOT in ('dog', 'pants')";
        parse(notIn).accept(toString, null);
        String expected = "baz <> dog AND baz <> pants";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testCaseiIn() {
        String input = "CASEI(road_class) IN (CASEI('Οδος'),CASEI('Straße'))";
        parse(input).accept(toString, null);
        String expected = "road_class @= Οδος OR road_class @= Straße";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testSIntersectsPoint() {
        String intersects_point = "s_intersects(footprint, point (43.5845 -79.5442))";
        parse(intersects_point).accept(toString, null);
        String expected = "S_INTERSECTS(footprint, POINT (43.5845 -79.5442))";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testSIntersectsMultiPoint() {
        String input = "s_intersects(footprint, MULTIPOINT ((10 40), (40 30), (20 20), (30 10)))";
        parse(input).accept(toString, null);
        String expected = "S_INTERSECTS(footprint, MULTIPOINT ((10 40), (40 30), (20 20), (30 10)))";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testSIntersectsLinestring() {    
        String intersects_line = "s_intersects(footprint, lineString (43.5845 -79.5442, 43.6079 -79.4893))";
        parse(intersects_line).accept(toString, null);
        String expected = "S_INTERSECTS(footprint, LINESTRING (43.5845 -79.5442, 43.6079 -79.4893))";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testSIntersectsPolygon() {
        String intersects_polygon = "s_INTERSECts(footprint, PolyGon ((43.5845 -79.5442, 43.6079 -79.4893, 43.5677 -79.4632, 43.6129 -79.3925, 43.6223 -79.3238, 43.6576 -79.3163, 43.7945 -79.1178, 43.8144 -79.1542, 43.8555 -79.1714, 43.7509 -79.6390, 43.5845 -79.5442)))";
        parse(intersects_polygon).accept(toString, null);
        String expected = "S_INTERSECTS(footprint, POLYGON ((43.5845 -79.5442, 43.6079 -79.4893, 43.5677 -79.4632, 43.6129 -79.3925, 43.6223 -79.3238, 43.6576 -79.3163, 43.7945 -79.1178, 43.8144 -79.1542, 43.8555 -79.1714, 43.7509 -79.639, 43.5845 -79.5442)))";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testCrossesPolygon() {
        String crosses_polygon = "S_CROSSES(footprint, PolyGon ((43.5845 -79.5442, 43.6079 -79.4893, 43.5677 -79.4632, 43.6129 -79.3925, 43.6223 -79.3238, 43.6576 -79.3163, 43.7945 -79.1178, 43.8144 -79.1542, 43.8555 -79.1714, 43.7509 -79.6390, 43.5845 -79.5442)))";
        parse(crosses_polygon).accept(toString, null);
        String expected = "S_CROSSES(footprint, POLYGON ((43.5845 -79.5442, 43.6079 -79.4893, 43.5677 -79.4632, 43.6129 -79.3925, 43.6223 -79.3238, 43.6576 -79.3163, 43.7945 -79.1178, 43.8144 -79.1542, 43.8555 -79.1714, 43.7509 -79.639, 43.5845 -79.5442)))";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testSIntersectsBbox() {
        String intersects_envelope = "S_Intersects(footprint, bbox (43.5845, -79.5442, 43.6079, -79.7893))";
        parse(intersects_envelope).accept(toString, null);
        String expected = "S_INTERSECTS(footprint, POLYGON ((43.5845 -79.5442, 43.6079 -79.5442, 43.6079 -79.7893, 43.5845 -79.7893, 43.5845 -79.5442)))";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testSIntersectsGeometryCollection() {
        String intersects_line = "s_intersects(footprint, GeometryCollection (lineString (43.5845 -79.5442, 43.6079 -79.4893), point (43.5845 -79.5442)))";
        parse(intersects_line).accept(toString, null);
        String expected = "S_INTERSECTS(footprint, GEOMETRYCOLLECTION (LINESTRING (43.5845 -79.5442, 43.6079 -79.4893), POINT (43.5845 -79.5442)))";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testIntersectsBufferLine() {
        String intersects_buffer_line = "S_Intersects(footprint, BUFFER( lineString (43.5845 -79.5442, 43.6079 -79.4893), 10))";
        parse(intersects_buffer_line).accept(toString, null);
        String expected = "S_INTERSECTS(footprint, BUFFER(LINESTRING (43.5845 -79.5442, 43.6079 -79.4893), 10.0))";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testRandomFunctionCalls() {
        String function_call = "foo > foo(bar(1), 2, 3)";
        parse(function_call).accept(toString, null);
        String expected = "foo > foo(bar(1.0), 2.0, 3.0)";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

    @Test
    public void testEmptyString() {
        String function_call = " ";
        parse(function_call).accept(toString, null);
        String expected = "";
        String actual = toString.finish();
        assertEquals(expected, actual);
    }

}
