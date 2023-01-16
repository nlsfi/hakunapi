package fi.nls.hakunapi.core.property.simple;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HakunaPropertyGeometryTest {

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

}
