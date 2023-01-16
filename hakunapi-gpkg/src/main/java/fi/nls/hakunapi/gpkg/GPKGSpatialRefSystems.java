package fi.nls.hakunapi.gpkg;

import java.util.HashMap;
import java.util.Map;

import fi.nls.hakunapi.core.schemas.Crs;

public class GPKGSpatialRefSystems {
    
    private GPKGSpatialRefSystems() {}
    
    public static final GPKGSpatialRefSys WGS84 = new GPKGSpatialRefSys("WGS84", 4326, "EPSG", 4326, "GEOGCS[\"WGS 84\", DATUM[\"World Geodetic System 1984\", SPHEROID[\"WGS 84\", 6378137.0, 298.257223563, AUTHORITY[\"EPSG\",\"7030\"]], AUTHORITY[\"EPSG\",\"6326\"]], PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]], UNIT[\"degree\", 0.017453292519943295], AXIS[\"Geodetic longitude\", EAST], AXIS[\"Geodetic latitude\", NORTH], AUTHORITY[\"EPSG\",\"4326\"]]", "WGS84");
    public static final GPKGSpatialRefSys UNDEFINED_CARTESIAN = new GPKGSpatialRefSys("Undefined Cartesian", -1, "NONE", -1, "undefined", "undefined");
    public static final GPKGSpatialRefSys UNDEFINED_GEOGRAPHIC = new GPKGSpatialRefSys("Undefined Geographic", 0, "NONE", 0, "undefined", "undefined");
    public static final GPKGSpatialRefSys EPSG_3067 = new GPKGSpatialRefSys("ETRS89 / TM35FIN(E,N) -- Finland", 3067, "EPSG", 3067, "PROJCS[\"ETRS89 / TM35FIN(E,N)\",GEOGCS[\"ETRS89\",DATUM[\"European_Terrestrial_Reference_System_1989\",SPHEROID[\"GRS 1980\",6378137,298.257222101,AUTHORITY[\"EPSG\",\"7019\"]],TOWGS84[0,0,0,0,0,0,0],AUTHORITY[\"EPSG\",\"6258\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.0174532925199433,AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4258\"]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"latitude_of_origin\",0],PARAMETER[\"central_meridian\",27],PARAMETER[\"scale_factor\",0.9996],PARAMETER[\"false_easting\",500000],PARAMETER[\"false_northing\",0],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],AXIS[\"Easting\",EAST],AXIS[\"Northing\",NORTH],AUTHORITY[\"EPSG\",\"3067\"]]");
    
    private static final Map<Integer, GPKGSpatialRefSys> BY_SRID = new HashMap<>();
    static {
        register(WGS84);
        register(UNDEFINED_CARTESIAN);
        register(UNDEFINED_GEOGRAPHIC);
        register(EPSG_3067);
    }
    
    public static void register(GPKGSpatialRefSys sys) {
        BY_SRID.put(sys.getSrsId(), sys);
    }
    
    public static GPKGSpatialRefSys get(int srid) {
        if (srid == Crs.CRS84_SRID) {
            return WGS84;
        }
        return BY_SRID.get(srid);
    }

    
}
