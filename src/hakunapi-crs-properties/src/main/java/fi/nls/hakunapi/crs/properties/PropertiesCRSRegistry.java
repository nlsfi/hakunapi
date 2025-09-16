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
 */
public class PropertiesCRSRegistry implements CRSRegistry {

    @Override
    public SRIDCode detect(Properties config, int srid) {
        String latLonStr = config.getProperty("srid." + srid + ".latLon", "false");
        String degreesStr = config.getProperty("srid." + srid + ".degrees", "false");
        String dimensionStr = config.getProperty("srid." + srid + ".geometryDimension", "XY");

        boolean latLon = "true".equalsIgnoreCase(latLonStr);
        boolean degrees = "true".equalsIgnoreCase(degreesStr);
        HakunaGeometryDimension geomDimension = HakunaGeometryDimension.valueOf(dimensionStr);                

        return new SRIDCode(srid, latLon, degrees, geomDimension);
    }

}
