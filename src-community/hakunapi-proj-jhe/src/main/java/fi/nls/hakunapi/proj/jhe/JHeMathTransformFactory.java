package fi.nls.hakunapi.proj.jhe;

public class JHeMathTransformFactory {

    public static JHeMathTransform findMathTransform(int from, int to) {
        EUREFFINTransform t = findEurefTransform(from, to);
        if (t == null) {
            return null;
        }
        return new JHeMathTransform() {
            @Override
            public void transform(double[] src, int srcOff, double[] dst, int dstOff, int n) {
                for (int i = 0; i < n; i++) {
                    t.transform(src[srcOff++], src[srcOff++], dst, dstOff);
                }
            }
        };
    }

    private static EUREFFINTransform findEurefTransform(int from, int to) {
        from = from == 84 || from == 4326 ? 4258 : from;
        to = to == 84 || to == 4326 ? 4258 : to;

        if (from == to) {
            return null;
        }

        if (to == 4258) {
            if (from >= 3873 && from <= 3885) { // GKnn
                int nn = from - 3873 + 19;
                return (x, y, out, off) -> EUREFFIN.gkNNtoGeo(nn, x, y, out, off);
            } else {
                switch (from) {
                case 3046:
                    return (x, y, out, off) -> EUREFFIN.planeToGeo(x, y, EUREFFIN.k0_TM, EUREFFIN.l0_TM34, EUREFFIN.E0_TM, out, off);
                case 3047:
                    return (x, y, out, off) -> EUREFFIN.planeToGeo(x, y, EUREFFIN.k0_TM, EUREFFIN.l0_TM35, EUREFFIN.E0_TM, out, off);
                case 3048:
                    return (x, y, out, off) -> EUREFFIN.planeToGeo(x, y, EUREFFIN.k0_TM, EUREFFIN.l0_TM36, EUREFFIN.E0_TM, out, off);
                case 3067:
                    return (x, y, out, off) -> EUREFFIN.planeToGeo(x, y, EUREFFIN.k0_TM, EUREFFIN.l0_TM35, EUREFFIN.E0_TM, out, off);
                case 3857:
                    return EUREFFIN::webMercToGeoRad;
                default:
                    throw new IllegalArgumentException("Could not find transfrom from " + from + " to " + to);
                }
            }
        }

        // to is NOT IN (84, 4258, 4326)
        // First transform from EPSG:from to geodetic radians
        // Second transform geodetic radians to EPSG:to (which is NOT IN (84, 4258, 4326))

        final EUREFFINTransform first;
        if (from == 4258) {
            first = (lon, lat, out, off) -> {
                out[off + 0] = Math.toRadians(lon);
                out[off + 1] = Math.toRadians(lat);
            };
        } else {
            if (from >= 3873 && to <= 3885) { // GKnn
                int nn = to - 3873 + 19;
                first = (x, y, out, off) -> EUREFFIN.gkNNtoGeoRad(nn, x, y, out, off);
            } else {
                switch (from) {
                case 3046:
                    first = (x, y, out, off) -> EUREFFIN.planeToGeoRad(x, y, EUREFFIN.k0_TM, EUREFFIN.l0_TM34, EUREFFIN.E0_TM, out, off);
                    break;
                case 3047:
                    first = (x, y, out, off) -> EUREFFIN.planeToGeoRad(x, y, EUREFFIN.k0_TM, EUREFFIN.l0_TM35, EUREFFIN.E0_TM, out, off);
                    break;
                case 3048:
                    first = (x, y, out, off) -> EUREFFIN.planeToGeoRad(x, y, EUREFFIN.k0_TM, EUREFFIN.l0_TM36, EUREFFIN.E0_TM, out, off);
                    break;
                case 3067:
                    first = (x, y, out, off) -> EUREFFIN.planeToGeoRad(x, y, EUREFFIN.k0_TM, EUREFFIN.l0_TM35, EUREFFIN.E0_TM, out, off);
                    break;
                case 3857:
                    first = EUREFFIN::webMercToGeoRad;
                    break;
                default:
                    throw new IllegalArgumentException("Could not find transfrom from " + from + " to " + to);
                }
            }
        }

        final EUREFFINTransform second;
        if (to >= 3873 && to <= 3885) { // GKnn
            int nn = to - 3873 + 19;
            second = (lonRad, latRad, out, off) -> EUREFFIN.geoToGKnnRad(nn, lonRad, latRad, out, off);
        } else {
            switch (to) {
            case 3046:
                second = (lonRad, latRad, out, off) -> EUREFFIN.geoToPlaneRad(lonRad, latRad, EUREFFIN.k0_TM, EUREFFIN.l0_TM34, EUREFFIN.E0_TM, out, off);
                break;
            case 3047:
                second = (lonRad, latRad, out, off) -> EUREFFIN.geoToPlaneRad(lonRad, latRad, EUREFFIN.k0_TM, EUREFFIN.l0_TM35, EUREFFIN.E0_TM, out, off);
                break;
            case 3048:
                second = (lonRad, latRad, out, off) -> EUREFFIN.geoToPlaneRad(lonRad, latRad, EUREFFIN.k0_TM, EUREFFIN.l0_TM36, EUREFFIN.E0_TM, out, off);
                break;
            case 3067:
                second = (lonRad, latRad, out, off) -> EUREFFIN.geoToPlaneRad(lonRad, latRad, EUREFFIN.k0_TM, EUREFFIN.l0_TM35, EUREFFIN.E0_TM, out, off);
                break;
            case 3857:
                second = EUREFFIN::geoToWebMercRad;
                break;
            default:
                throw new IllegalArgumentException("Could not find transfrom from " + from + " to " + to);
            }
        }

        return new EUREFFINTransform() {
            @Override
            public void transform(double x, double y, double[] out, int off) {
                first.transform(x, y, out, off);
                second.transform(out[off + 0], out[off + 1], out, off);
            }
        };
    }

}
