package fi.nls.hakunapi.core.util;

import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;
import fi.nls.hakunapi.core.schemas.Crs;

public class CrsUtil {

    public static final String URI_PREFIX = "http://www.opengis.net/def/crs/";
    public static final String ERR_FORMAT_MODEL = "'%s' shall follow format model 'http://www.opengis.net/def/crs/{authority}/{version}/{code}'";
    public static final String ERR_UNKNOWN_CRS = "Unrecognized coordinate reference system in '%s'";
    public static final String ERR_UNSUPPORTED_CRS = "Coordinate reference system not supported";

    public static int parseSRID(String crsUri, String paramName) throws IllegalArgumentException {
        if (crsUri == null || crsUri.isEmpty()) {
            return Crs.CRS84_SRID;
        }
        if (Crs.CRS84.equals(crsUri)) {
            return Crs.CRS84_SRID;
        }

        if (!crsUri.startsWith(URI_PREFIX)) {
            String err = String.format(ERR_FORMAT_MODEL, paramName);
            throw new IllegalArgumentException(err);
        }
        crsUri = crsUri.substring(URI_PREFIX.length());

        int i = crsUri.indexOf('/');
        if (i < 0) {
            String err = String.format(ERR_FORMAT_MODEL, paramName);
            throw new IllegalArgumentException(err);
        }

        String authority = crsUri.substring(0, i);
        crsUri = crsUri.substring(i + 1);

        i = crsUri.indexOf('/');
        if (i < 0) {
            String err = String.format(ERR_FORMAT_MODEL, paramName);
            throw new IllegalArgumentException(err);
        }

        String version = crsUri.substring(0, i);

        String code = crsUri.substring(i + 1);
        try {
            return Integer.parseInt(code);
        } catch (NumberFormatException e) {
            String err = String.format(ERR_UNKNOWN_CRS, paramName);
            throw new IllegalArgumentException(err);
        }
    }

    public static String toUri(int srid) {
        if (srid == Crs.CRS84_SRID) {
            return Crs.CRS84;
        }
        return "http://www.opengis.net/def/crs/EPSG/0/" + srid;
    }

    // FIXME: Use some sane logic, for example unit of measure
    public static int getDefaultMaxDecimalCoordinates(int srid) {
        switch (srid) {
        case Crs.CRS84_SRID:
        case 4326:
        case 4258:
            return 7;
        default:
            return 3;
        }
    }


}
