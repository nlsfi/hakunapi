package fi.nls.hakunapi.crs.properties;

import java.util.Properties;

import fi.nls.hakunapi.core.CRSRegistry;
import fi.nls.hakunapi.core.SRIDCode;
import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;

/**
 * Implementation of CRSRegistry that uses the default configuration file as the source of truth
 * srid=<code1>,<code2>
 * srid.<code>.latLon=true/false (default false)
 * srid.<code>.degrees=true/false (default false)
 * srid.<code>.geometryDimension=XY/XYZ/XYZM (default XY)
 * srid.<code>.wrapXMin=<number> (optional, e.g., -180.0 for geographic, -20037508 for WebMercator)
 * srid.<code>.wrapXMax=<number> (optional, e.g., 180.0 for geographic, 20037508 for WebMercator)
 */
public class PropertiesCRSRegistry implements CRSRegistry {

    @Override
    public SRIDCode detect(Properties config, int srid) {
        String latLonStr = config.getProperty("srid." + srid + ".latLon", "false");
        String degreesStr = config.getProperty("srid." + srid + ".degrees", "false");
        String dimensionStr = config.getProperty("srid." + srid + ".geometryDimension", "XY");
        String wrapXMinStr = config.getProperty("srid." + srid + ".wrapXMin");
        String wrapXMaxStr = config.getProperty("srid." + srid + ".wrapXMax");

        boolean latLon = "true".equalsIgnoreCase(latLonStr);
        boolean degrees = "true".equalsIgnoreCase(degreesStr);
        HakunaGeometryDimension geomDimension = HakunaGeometryDimension.valueOf(dimensionStr);

        Double wrapXMin = parseDouble(wrapXMinStr);
        Double wrapXMax = parseDouble(wrapXMaxStr);

        // Validate: both must be set or neither
        if ((wrapXMin == null) != (wrapXMax == null)) {
            throw new IllegalArgumentException(
                "srid." + srid + ": Both wrapXMin and wrapXMax must be set, or neither");
        }

        return new SRIDCode(srid, latLon, degrees, geomDimension, wrapXMin, wrapXMax);
    }

    private static Double parseDouble(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return Double.parseDouble(value);
    }

}
