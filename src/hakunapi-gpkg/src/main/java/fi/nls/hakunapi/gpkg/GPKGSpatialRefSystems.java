package fi.nls.hakunapi.gpkg;

import java.util.HashMap;
import java.util.Map;

import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.CRS;

import fi.nls.hakunapi.core.schemas.Crs;

public class GPKGSpatialRefSystems {

    private GPKGSpatialRefSystems() {}

    public static final GPKGSpatialRefSys WGS84 = new GPKGSpatialRefSys("WGS84", 4326, "EPSG", 4326, "GEOGCS[\"WGS 84\", DATUM[\"World Geodetic System 1984\", SPHEROID[\"WGS 84\", 6378137.0, 298.257223563, AUTHORITY[\"EPSG\",\"7030\"]], AUTHORITY[\"EPSG\",\"6326\"]], PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]], UNIT[\"degree\", 0.017453292519943295], AXIS[\"Geodetic longitude\", EAST], AXIS[\"Geodetic latitude\", NORTH], AUTHORITY[\"EPSG\",\"4326\"]]", "WGS84");
    public static final GPKGSpatialRefSys UNDEFINED_CARTESIAN = new GPKGSpatialRefSys("Undefined Cartesian", -1, "NONE", -1, "undefined", "undefined");
    public static final GPKGSpatialRefSys UNDEFINED_GEOGRAPHIC = new GPKGSpatialRefSys("Undefined Geographic", 0, "NONE", 0, "undefined", "undefined");

    private static final Map<Integer, GPKGSpatialRefSys> BY_SRID = new HashMap<>();
    static {
        register(WGS84);
        register(UNDEFINED_CARTESIAN);
        register(UNDEFINED_GEOGRAPHIC);
    }

    public static void register(GPKGSpatialRefSys sys) {
        BY_SRID.put(sys.getSrsId(), sys);
    }

    public static GPKGSpatialRefSys get(int srid) {
        if (srid == Crs.CRS84_SRID) {
            return WGS84;
        }
        return BY_SRID.computeIfAbsent(srid, GPKGSpatialRefSystems::epsg);
    }

    private static GPKGSpatialRefSys epsg(int srid) {
        try {
            String srsName = "EPSG:" + srid;
            CoordinateReferenceSystem crs = CRS.decode(srsName, true);
            String definition = crs.toWKT();
            return new GPKGSpatialRefSys(srsName, srid, "EPSG", srid, definition, srsName);
        } catch (Exception e) {
            throw new RuntimeException("Could not decode CRS for srid: " + srid);
        }
    }

}
