package fi.nls.hakunapi.proj.jhe;

import static java.lang.Math.*;

/**
 * https://www.suomidigi.fi/ohjeet-ja-tuki/jhs-suositukset/jhs-197-euref-fin-koordinaattijarjestelmat-niihin-liittyvat-muunnokset-ja-karttalehtijako
 */
public class EUREFFIN {

    public static final double k0_TM = 0.9996;
    public static final double l0_TM34 = toRadians(21.0);
    public static final double l0_TM35 = toRadians(27.0);
    public static final double l0_TM36 = toRadians(33.0);
    public static final double E0_TM = 500000.0;

    private static final double a = 6378137.0;
    private static final double inv_a = 1.0 / 6378137.0;
    private static final double f = 1.0 / 298.257222101;
    private static final double e = sqrt(2 * f - (f * f));

    private static final double n = f / (2.0 - f);
    private static final double n2 = n * n;
    private static final double n3 = n * n * n;
    private static final double n4 = n * n * n * n;

    private static final double A1 = (a / (1.0 + n)) * (1.0 + (n2 / 4.0) + (n4 / 64.0));

    private static final double h1 = (n / 2.0) - (2.0 * n2 / 3.0) + (37.0 * n3 / 96.0) - (n4 / 360.0);
    private static final double h2 = (n2 / 48.0) + (n3 / 15.0) - (437.0 * n4 / 1440.0);
    private static final double h3 = (17.0 * n3 / 480.0) - (37.0 * n4 / 840.0);
    private static final double h4 = (4397.0 * n4 / 161280.0);

    private static final double h1_ = (n / 2.0) - (2.0 * n2 / 3.0) + (5.0 * n3 / 16.0) + (41.0 * n4 / 180.0);
    private static final double h2_ = (13.0 * n2 / 48.0) - (3.0 * n3 / 5.0) + (557.0 * n4 / 1440.0);
    private static final double h3_ = (61.0 * n3 / 240.0) - (103.0 * n4 / 140.0);
    private static final double h4_ = (49561.0 * n4 / 161280.0);

    private static final double epsilon = 1e-10;

    private static final double asinh(final double x) { return log(x + sqrt(x*x + 1.0)); }

    private static final double sech(final double x) { return 1.0 / cosh(x); }

    private static final double atanh(final double x) { return 0.5 * log((1.0 + x) / (1.0 - x)); }

    public static void geoToPlane(double lon, double lat, double k0, double l0, double E0, double[] out, int off) {
        geoToPlaneRad(toRadians(lon), toRadians(lat), k0, l0, E0, out, off);
    }

    public static void geoToPlaneRad(double lonRad, double latRad, double k0, double l0, double E0, double[] out, int off) {
        double Q1 = asinh(tan(latRad));
        double Q2 = atanh(e * sin(latRad));
        double Q = Q1 - (e * Q2);

        double l = lonRad - l0;
        double B = atan(sinh(Q));
        double n_ = atanh(cos(B) * sin(l));

        double ks_ = asin(sin(B) / sech(n_));

        double ks1 = h1_ * sin(2.0 * ks_) * cosh(2.0 * n_);
        double ks2 = h2_ * sin(4.0 * ks_) * cosh(4.0 * n_);
        double ks3 = h3_ * sin(6.0 * ks_) * cosh(6.0 * n_);
        double ks4 = h4_ * sin(8.0 * ks_) * cosh(8.0 * n_);

        double n1_ = h1_ * cos(2.0 * ks_) * sinh(2.0 * n_);
        double n2_ = h2_ * cos(4.0 * ks_) * sinh(4.0 * n_);
        double n3_ = h3_ * cos(6.0 * ks_) * sinh(6.0 * n_);
        double n4_ = h4_ * cos(8.0 * ks_) * sinh(8.0 * n_);

        double ks = ks_ + ks1 + ks2 + ks3 + ks4;
        double nn = n_ + n1_ + n2_ + n3_ + n4_;

        double N = A1 * ks * k0;
        double E = A1 * nn * k0 + E0;

        out[off + 0] = E;
        out[off + 1] = N;
    }

    public static void planeToGeo(double E, double N, double k0, double l0, double E0, double[] out, int off) {
        planeToGeoRad(E, N, k0, l0, E0, out, off);
        out[off + 0] = toDegrees(out[off + 0]);
        out[off + 1] = toDegrees(out[off + 1]);
    }

    public static void planeToGeoRad(double E, double N, double k0, double l0, double E0, double[] out, int off) {
        double ks = N / (A1 * k0);
        double nn = (E - E0) / (A1 * k0);

        double ks1_ = h1 * sin(2.0 * ks) * cosh(2.0 * nn);
        double ks2_ = h2 * sin(4.0 * ks) * cosh(4.0 * nn);
        double ks3_ = h3 * sin(6.0 * ks) * cosh(6.0 * nn);
        double ks4_ = h4 * sin(8.0 * ks) * cosh(8.0 * nn);

        double nn1_ = h1 * cos(2.0 * ks) * sinh(2.0 * nn);
        double nn2_ = h2 * cos(4.0 * ks) * sinh(4.0 * nn);
        double nn3_ = h3 * cos(6.0 * ks) * sinh(6.0 * nn);
        double nn4_ = h4 * cos(8.0 * ks) * sinh(8.0 * nn);

        double ks_ = ks - ks1_ - ks2_ - ks3_ - ks4_;
        double nn_ = nn - nn1_ - nn2_ - nn3_ - nn4_;

        double B = asin(sech(nn_) * sin(ks_));
        double l = asin(tanh(nn_) / cos(B));

        double Q = asinh(tan(B));
        double Q1 = Q + e * atanh(e * tanh(Q));

        double delta;
        do {
            double Q2 = Q + e * atanh(e * tanh(Q1));
            delta = Q2 - Q1;
            Q1 = Q2;
        } while (abs(delta) > epsilon);

        double lat = atan(sinh(Q1));
        double lon = l0 + l;

        out[off + 0] = lon;
        out[off + 1] = lat;
    }

    public static void geoToTM35fin(double lon, double lat, double[] out, int off) {
        geoToTM35finRad(toRadians(lon), toRadians(lat), out, off);
    }

    public static void geoToTM35finRad(double lonRad, double latRad, double[] out, int off) {
        geoToPlaneRad(lonRad, latRad, k0_TM, l0_TM35, E0_TM, out, off);
    }

    public static void tm35finToGeo(double E, double N, double[] out, int off) {
        planeToGeo(E, N, k0_TM, l0_TM35, E0_TM, out, off);
    }

    public static void tm35finToGeoRad(double E, double N, double[] out, int off) {
        planeToGeoRad(E, N, k0_TM, l0_TM35, E0_TM, out, off);
    }

    public static void geoToWebMerc(double lon, double lat, double[] out, int off) {
        geoToWebMercRad(toRadians(lon), toRadians(lat), out, off);
    }

    public static void geoToWebMercRad(double lonRad, double latRad, double[] out, int off) {
        // Ignore differences between etrs89 and wgs84
        out[off + 0] = lonRad * a;
        out[off + 1] = log(tan(PI / 4.0 + latRad / 2.0)) * a;
    }

    public static void webMercToGeoRad(double x, double y, double[] out, int off) {
        // Ignore differences between etrs89 and wgs84
        out[off + 0] = x * inv_a;
        out[off + 1] = atan(exp(y * inv_a)) * 2 - PI / 2.0;
    }

    public static void tm35finToWebMerc(double E, double N, double[] out, int off) {
        tm35finToGeoRad(E, N, out, off);
        geoToWebMercRad(out[off], out[off + 1], out, off);
    }

    public static void webMercToTM35fin(double x, double y, double[] out, int off) {
        webMercToGeoRad(x, y, out, off);
        geoToTM35finRad(out[off], out[off + 1], out, off);
    }

    public static void gkNNtoGeo(int nn, double E, double N, double[] out, int off) {
        double k0 = 1.0;
        double l0 = toRadians(nn);
        double E0 = 1_000_000 * nn + 500_000;
        planeToGeo(E, N, k0, l0, E0, out, off);
    }

    public static void gkNNtoGeoRad(int nn, double E, double N, double[] out, int off) {
        double k0 = 1.0;
        double l0 = toRadians(nn);
        double E0 = 1_000_000 * nn + 500_000;
        planeToGeoRad(E, N, k0, l0, E0, out, off);
    }

    public static void geoToGKnn(int nn, double lon, double lat, double[] out, int off) {
        geoToGKnnRad(nn, toRadians(lon), toRadians(lat), out, off);
    }

    public static void geoToGKnnRad(int nn, double lonRad, double latRad, double[] out, int off) {
        double k0 = 1.0;
        double l0 = toRadians(nn);
        double E0 = 1_000_000 * nn + 500_000;
        geoToPlaneRad(lonRad, latRad, k0, l0, E0, out, off);
    }

    public static int tm35finToGKnn(double E, double N, double[] out, int off) {
        tm35finToGeoRad(E, N, out, off);
        double lonRad = out[off];
        double latRad = out[off + 1];
        int nn = (int) Math.round(toDegrees(lonRad));
        geoToGKnnRad(nn, lonRad, latRad, out, off);
        return nn;
    }

    public static void tm35finToGKnn(double E, double N, int nn, double[] out, int off) {
        tm35finToGeoRad(E, N, out, off);
        geoToGKnnRad(nn, out[off], out[off + 1], out, off);
    }

    public static void gkNNtoTM35fin(int nn, double E, double N, double[] out, int off) {
        gkNNtoGeoRad(nn, E, N, out, off);
        geoToTM35finRad(out[off], out[off + 1], out, off);
    }

    public static void tm35finToTM34(double E, double N, double[] out, int off) {
        planeToGeoRad(E, N, k0_TM, l0_TM35, E0_TM, out, off);
        geoToPlaneRad(out[off], out[off + 1], k0_TM, l0_TM34, E0_TM, out, off);
    }

    public static void tm34toTM35fin(double E, double N, double[] out, int off) {
        planeToGeoRad(E, N, k0_TM, l0_TM34, E0_TM, out, off);
        geoToPlaneRad(out[off], out[off + 1], k0_TM, l0_TM35, E0_TM, out, off);
    }

    public static void tm35finToTM35(double E, double N, double[] out, int off) {
        out[off++] = E;
        out[off++] = N;
    }

    public static void tm35toTM35fin(double E, double N, double[] out, int off) {
        out[off++] = E;
        out[off++] = N;
    }

    public static void tm35finToTM36(double E, double N, double[] out, int off) {
        planeToGeoRad(E, N, k0_TM, l0_TM35, E0_TM, out, off);
        geoToPlaneRad(out[off], out[off + 1], k0_TM, l0_TM36, E0_TM, out, off);
    }

    public static void tm36toTM35fin(double E, double N, double[] out, int off) {
        planeToGeoRad(E, N, k0_TM, l0_TM36, E0_TM, out, off);
        geoToPlaneRad(out[off], out[off + 1], k0_TM, l0_TM35, E0_TM, out, off);
    }

}