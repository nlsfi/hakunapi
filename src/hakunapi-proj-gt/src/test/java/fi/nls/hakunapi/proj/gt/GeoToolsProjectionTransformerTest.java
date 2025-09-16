package fi.nls.hakunapi.proj.gt;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import fi.nls.hakunapi.core.projection.ProjectionTransformer;
import fi.nls.hakunapi.core.projection.ProjectionTransformerFactory;

public class GeoToolsProjectionTransformerTest {

    @Test
    public void testCRS84to3067() throws Exception {
        ProjectionTransformerFactory fact = new GeoToolsProjectionTransformerFactory();
        ProjectionTransformer t = fact.fromCRS84(3067);
        double[] coords = { 27.0, 60.0 };
        t.transformInPlace(coords, 0, 1);
        assertEquals(500000, coords[0], 1e-8);
        assertEquals(6651411.193370228, coords[1], 1e-8);
    }

    @Test
    @Ignore("Issue with Java asserts with 4258 (lon,lat) -> 10690 (lon,lat) step being NO-OP")
    public void test4258to3067() throws Exception {
        ProjectionTransformerFactory fact = new GeoToolsProjectionTransformerFactory();
        ProjectionTransformer t = fact.getTransformer(4258, 3067);
        double[] coords = { 27.0, 60.0 };
        t.transformInPlace(coords, 0, 1);
        assertEquals(500000, coords[0], 0.0);
        assertEquals(6651411.190242998, coords[1], 1e-8);
    }

    @Test
    public void test10690to3067() throws Exception {
        ProjectionTransformerFactory fact = new GeoToolsProjectionTransformerFactory();
        ProjectionTransformer t = fact.getTransformer(10690, 3067);
        double[] coords = { 27.0, 60.0 };
        t.transformInPlace(coords, 0, 1);
        assertEquals(500000, coords[0], 0.0);
        assertEquals(6651411.190242998, coords[1], 1e-8);
    }

    @Test
    public void test3067to3879() throws Exception {
        ProjectionTransformerFactory fact = new GeoToolsProjectionTransformerFactory();
        ProjectionTransformer t = fact.getTransformer(3067, 3879);
        double[] coords = { 500000, 6670000 };
        t.transformInPlace(coords, 0, 1);
        assertEquals(25611025.9845, coords[0], 1e-4);
        assertEquals(6674350.2959, coords[1], 1e-4);
    }

    @Test
    public void testThreePoints3067to3879() throws Exception {
        ProjectionTransformerFactory fact = new GeoToolsProjectionTransformerFactory();
        ProjectionTransformer t = fact.getTransformer(3067, 3879);
        double[] coords = {
                500000, 6670000,
                550000, 6680000,
                450000, 6675000
        };
        t.transformInPlace(coords, 0, 3);
        assertEquals(25611025.9845, coords[0], 1e-4);
        assertEquals(6674350.2959, coords[1], 1e-4);
        assertEquals(25660730.8175, coords[2], 1e-4);
        assertEquals(6685867.5819, coords[3], 1e-4);
        assertEquals(25560873.2833, coords[4], 1e-4);
        assertEquals(6677835.4764, coords[5], 1e-4);
    }

    @Test
    public void test3903toCRS84() throws Exception {
        ProjectionTransformerFactory fact = new GeoToolsProjectionTransformerFactory();
        ProjectionTransformer t = fact.getTransformer(3903, 84);
        double[] coords = {
                500000, 6651411.193370228,
                500000, 6651411.193370228,
        };
        t.transformInPlace(coords, 0, 2);
        assertEquals(27.0, coords[0], 1e-4);
        assertEquals(60.0, coords[1], 1e-4);
        assertEquals(27.0, coords[2], 1e-4);
        assertEquals(60.0, coords[3], 1e-4);
    }

    @Test
    public void testThreePoints3879toCRS84() throws Exception {
        ProjectionTransformerFactory fact = new GeoToolsProjectionTransformerFactory();
        ProjectionTransformer t = fact.toCRS84(3879);
        double[] coords = {
                25611025.9845, 6674350.2959,
                25660730.8175, 6685867.5819,
                25560873.2833, 6677835.4764
        };
        t.transformInPlace(coords, 0, 3);
        assertEquals(27.0, coords[0], 1e-4);
        assertEquals(60.1669116, coords[1], 1e-4);
        assertEquals(27.9033582, coords[2], 1e-4);
        assertEquals(60.2536285, coords[3], 1e-4);
        assertEquals(26.0978763, coords[4], 1e-4);
        assertEquals(60.2087393, coords[5], 1e-4);
    }

    /*
    @Test
    public void performanceTest() throws Exception {
        ProjectionTransformerFactory fact = new GeoToolsProjectionTransformerFactory();
        ProjectionTransformer t1 = fact.getTransformer(3067, 4258);
        ProjectionTransformer t2 = fact.toCRS84(3067);
        ProjectionTransformer t3 = fact.getTransformer(3067, 4326);
        int n = 200000;
        ThreadLocalRandom r = ThreadLocalRandom.current();
        double[] arr = new double[2 * n];
        for (int i = 0; i < arr.length;) {
            arr[i++] = r.nextDouble(300000, 700000);
            arr[i++] = r.nextDouble(6660000, 7600000);
        }

        double[] copy = new double[arr.length];

        int runs = 10;

        test(t1, "t1", arr, copy, runs);
        test(t2, "t2", arr, copy, runs);
        test(t3, "t3", arr, copy, runs);
        test(t1, "t1", arr, copy, runs);
        test(t2, "t2", arr, copy, runs);
        test(t3, "t3", arr, copy, runs);
    }

    private void test(ProjectionTransformer t, String testName, double[] arr, double[] copy, int runs) throws Exception {
        System.out.println(testName);
        int n = arr.length  / 2;
        for (int i = 0; i < runs; i++) {
            System.arraycopy(arr, 0, copy, 0, arr.length);
            long start = System.nanoTime();
            t.transformInPlace(arr, 0, n);
            long end = System.nanoTime();
            System.out.println(TimeUnit.NANOSECONDS.toMillis(end-start));
        }
        System.out.println("---");
    }
     */

}
