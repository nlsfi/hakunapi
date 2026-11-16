package fi.nls.hakunapi.proj.jhe;

/**
 * WKT 1 definitions of the CRSs JHe can transform, from the EPSG Geodetic Parameter Dataset.
 * Self-contained (JDK only) so it can be copied as is.
 * <p>
 * GENERATED, do not edit by hand.
 */
public class JHeWKT {

    private JHeWKT() {}

    /**
     * @param longitudeFirst x/y (easting/longitude first) axis order instead of the authority's
     * @return WKT 1 definition, or null if the srid is not supported
     */
    public static String get(int srid, boolean longitudeFirst) {
        return longitudeFirst ? xy(srid) : authority(srid);
    }

    private static String xy(int srid) {
        return switch (srid) {
        case 4258 -> """
                GEOGCS["ETRS89",\s
                  DATUM["European Terrestrial Reference System 1989",\s
                    SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                    TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                    AUTHORITY["EPSG","6258"]],\s
                  PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                  UNIT["degree", 0.017453292519943295],\s
                  AXIS["Geodetic longitude", EAST],\s
                  AXIS["Geodetic latitude", NORTH],\s
                  AUTHORITY["EPSG","4258"]]""";
        case 4326 -> """
                GEOGCS["WGS 84",\s
                  DATUM["World Geodetic System 1984",\s
                    SPHEROID["WGS 84", 6378137.0, 298.257223563, AUTHORITY["EPSG","7030"]],\s
                    AUTHORITY["EPSG","6326"]],\s
                  PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                  UNIT["degree", 0.017453292519943295],\s
                  AXIS["Geodetic longitude", EAST],\s
                  AXIS["Geodetic latitude", NORTH],\s
                  AUTHORITY["EPSG","4326"]]""";
        case 3046 -> """
                PROJCS["ETRS89 / UTM zone 34N (N-E)",\s
                  GEOGCS["ETRS89",\s
                    DATUM["European Terrestrial Reference System 1989",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","6258"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AUTHORITY["EPSG","4258"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 21.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 0.9996],\s
                  PARAMETER["false_easting", 500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Easting", EAST],\s
                  AXIS["Northing", NORTH],\s
                  AUTHORITY["EPSG","3046"]]""";
        case 3047 -> """
                PROJCS["ETRS89 / UTM zone 35N (N-E)",\s
                  GEOGCS["ETRS89",\s
                    DATUM["European Terrestrial Reference System 1989",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","6258"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AUTHORITY["EPSG","4258"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 27.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 0.9996],\s
                  PARAMETER["false_easting", 500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Easting", EAST],\s
                  AXIS["Northing", NORTH],\s
                  AUTHORITY["EPSG","3047"]]""";
        case 3048 -> """
                PROJCS["ETRS89 / UTM zone 36N (N-E)",\s
                  GEOGCS["ETRS89",\s
                    DATUM["European Terrestrial Reference System 1989",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","6258"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AUTHORITY["EPSG","4258"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 33.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 0.9996],\s
                  PARAMETER["false_easting", 500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Easting", EAST],\s
                  AXIS["Northing", NORTH],\s
                  AUTHORITY["EPSG","3048"]]""";
        case 3067 -> """
                PROJCS["EUREF-FIN / TM35FIN(E,N)",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 27.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 0.9996],\s
                  PARAMETER["false_easting", 500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Easting", EAST],\s
                  AXIS["Northing", NORTH],\s
                  AUTHORITY["EPSG","3067"]]""";
        case 3857 -> """
                PROJCS["WGS 84 / Pseudo-Mercator",\s
                  GEOGCS["WGS 84",\s
                    DATUM["World Geodetic System 1984",\s
                      SPHEROID["WGS 84", 6378137.0, 298.257223563, AUTHORITY["EPSG","7030"]],\s
                      AUTHORITY["EPSG","6326"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AUTHORITY["EPSG","4326"]],\s
                  PROJECTION["Popular Visualisation Pseudo Mercator", AUTHORITY["EPSG","1024"]],\s
                  PARAMETER["semi_minor", 6378137.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["central_meridian", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 0.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Easting", EAST],\s
                  AXIS["Northing", NORTH],\s
                  AUTHORITY["EPSG","3857"]]""";
        case 3873 -> """
                PROJCS["EUREF-FIN / GK19FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 19.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 19500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Easting", EAST],\s
                  AXIS["Northing", NORTH],\s
                  AUTHORITY["EPSG","3873"]]""";
        case 3874 -> """
                PROJCS["EUREF-FIN / GK20FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 20.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 20500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Easting", EAST],\s
                  AXIS["Northing", NORTH],\s
                  AUTHORITY["EPSG","3874"]]""";
        case 3875 -> """
                PROJCS["EUREF-FIN / GK21FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 21.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 21500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Easting", EAST],\s
                  AXIS["Northing", NORTH],\s
                  AUTHORITY["EPSG","3875"]]""";
        case 3876 -> """
                PROJCS["EUREF-FIN / GK22FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 22.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 22500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Easting", EAST],\s
                  AXIS["Northing", NORTH],\s
                  AUTHORITY["EPSG","3876"]]""";
        case 3877 -> """
                PROJCS["EUREF-FIN / GK23FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 23.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 23500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Easting", EAST],\s
                  AXIS["Northing", NORTH],\s
                  AUTHORITY["EPSG","3877"]]""";
        case 3878 -> """
                PROJCS["EUREF-FIN / GK24FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 24.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 24500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Easting", EAST],\s
                  AXIS["Northing", NORTH],\s
                  AUTHORITY["EPSG","3878"]]""";
        case 3879 -> """
                PROJCS["EUREF-FIN / GK25FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 25.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 25500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Easting", EAST],\s
                  AXIS["Northing", NORTH],\s
                  AUTHORITY["EPSG","3879"]]""";
        case 3880 -> """
                PROJCS["EUREF-FIN / GK26FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 26.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 26500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Easting", EAST],\s
                  AXIS["Northing", NORTH],\s
                  AUTHORITY["EPSG","3880"]]""";
        case 3881 -> """
                PROJCS["EUREF-FIN / GK27FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 27.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 27500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Easting", EAST],\s
                  AXIS["Northing", NORTH],\s
                  AUTHORITY["EPSG","3881"]]""";
        case 3882 -> """
                PROJCS["EUREF-FIN / GK28FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 28.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 28500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Easting", EAST],\s
                  AXIS["Northing", NORTH],\s
                  AUTHORITY["EPSG","3882"]]""";
        case 3883 -> """
                PROJCS["EUREF-FIN / GK29FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 29.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 29500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Easting", EAST],\s
                  AXIS["Northing", NORTH],\s
                  AUTHORITY["EPSG","3883"]]""";
        case 3884 -> """
                PROJCS["EUREF-FIN / GK30FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 30.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 30500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Easting", EAST],\s
                  AXIS["Northing", NORTH],\s
                  AUTHORITY["EPSG","3884"]]""";
        case 3885 -> """
                PROJCS["EUREF-FIN / GK31FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 31.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 31500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Easting", EAST],\s
                  AXIS["Northing", NORTH],\s
                  AUTHORITY["EPSG","3885"]]""";
        default -> null;
        };
    }

    private static String authority(int srid) {
        return switch (srid) {
        case 4258 -> """
                GEOGCS["ETRS89",\s
                  DATUM["European Terrestrial Reference System 1989",\s
                    SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                    TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                    AUTHORITY["EPSG","6258"]],\s
                  PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                  UNIT["degree", 0.017453292519943295],\s
                  AXIS["Geodetic latitude", NORTH],\s
                  AXIS["Geodetic longitude", EAST],\s
                  AUTHORITY["EPSG","4258"]]""";
        case 4326 -> """
                GEOGCS["WGS 84",\s
                  DATUM["World Geodetic System 1984",\s
                    SPHEROID["WGS 84", 6378137.0, 298.257223563, AUTHORITY["EPSG","7030"]],\s
                    AUTHORITY["EPSG","6326"]],\s
                  PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                  UNIT["degree", 0.017453292519943295],\s
                  AXIS["Geodetic latitude", NORTH],\s
                  AXIS["Geodetic longitude", EAST],\s
                  AUTHORITY["EPSG","4326"]]""";
        case 3046 -> """
                PROJCS["ETRS89 / UTM zone 34N (N-E)",\s
                  GEOGCS["ETRS89",\s
                    DATUM["European Terrestrial Reference System 1989",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","6258"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AUTHORITY["EPSG","4258"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 21.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 0.9996],\s
                  PARAMETER["false_easting", 500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Northing", NORTH],\s
                  AXIS["Easting", EAST],\s
                  AUTHORITY["EPSG","3046"]]""";
        case 3047 -> """
                PROJCS["ETRS89 / UTM zone 35N (N-E)",\s
                  GEOGCS["ETRS89",\s
                    DATUM["European Terrestrial Reference System 1989",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","6258"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AUTHORITY["EPSG","4258"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 27.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 0.9996],\s
                  PARAMETER["false_easting", 500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Northing", NORTH],\s
                  AXIS["Easting", EAST],\s
                  AUTHORITY["EPSG","3047"]]""";
        case 3048 -> """
                PROJCS["ETRS89 / UTM zone 36N (N-E)",\s
                  GEOGCS["ETRS89",\s
                    DATUM["European Terrestrial Reference System 1989",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","6258"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AUTHORITY["EPSG","4258"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 33.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 0.9996],\s
                  PARAMETER["false_easting", 500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Northing", NORTH],\s
                  AXIS["Easting", EAST],\s
                  AUTHORITY["EPSG","3048"]]""";
        case 3067 -> """
                PROJCS["EUREF-FIN / TM35FIN(E,N)",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 27.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 0.9996],\s
                  PARAMETER["false_easting", 500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Easting", EAST],\s
                  AXIS["Northing", NORTH],\s
                  AUTHORITY["EPSG","3067"]]""";
        case 3857 -> """
                PROJCS["WGS 84 / Pseudo-Mercator",\s
                  GEOGCS["WGS 84",\s
                    DATUM["World Geodetic System 1984",\s
                      SPHEROID["WGS 84", 6378137.0, 298.257223563, AUTHORITY["EPSG","7030"]],\s
                      AUTHORITY["EPSG","6326"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AUTHORITY["EPSG","4326"]],\s
                  PROJECTION["Popular Visualisation Pseudo Mercator", AUTHORITY["EPSG","1024"]],\s
                  PARAMETER["semi_minor", 6378137.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["central_meridian", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 0.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Easting", EAST],\s
                  AXIS["Northing", NORTH],\s
                  AUTHORITY["EPSG","3857"]]""";
        case 3873 -> """
                PROJCS["EUREF-FIN / GK19FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 19.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 19500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Northing", NORTH],\s
                  AXIS["Easting", EAST],\s
                  AUTHORITY["EPSG","3873"]]""";
        case 3874 -> """
                PROJCS["EUREF-FIN / GK20FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 20.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 20500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Northing", NORTH],\s
                  AXIS["Easting", EAST],\s
                  AUTHORITY["EPSG","3874"]]""";
        case 3875 -> """
                PROJCS["EUREF-FIN / GK21FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 21.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 21500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Northing", NORTH],\s
                  AXIS["Easting", EAST],\s
                  AUTHORITY["EPSG","3875"]]""";
        case 3876 -> """
                PROJCS["EUREF-FIN / GK22FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 22.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 22500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Northing", NORTH],\s
                  AXIS["Easting", EAST],\s
                  AUTHORITY["EPSG","3876"]]""";
        case 3877 -> """
                PROJCS["EUREF-FIN / GK23FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 23.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 23500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Northing", NORTH],\s
                  AXIS["Easting", EAST],\s
                  AUTHORITY["EPSG","3877"]]""";
        case 3878 -> """
                PROJCS["EUREF-FIN / GK24FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 24.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 24500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Northing", NORTH],\s
                  AXIS["Easting", EAST],\s
                  AUTHORITY["EPSG","3878"]]""";
        case 3879 -> """
                PROJCS["EUREF-FIN / GK25FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 25.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 25500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Northing", NORTH],\s
                  AXIS["Easting", EAST],\s
                  AUTHORITY["EPSG","3879"]]""";
        case 3880 -> """
                PROJCS["EUREF-FIN / GK26FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 26.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 26500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Northing", NORTH],\s
                  AXIS["Easting", EAST],\s
                  AUTHORITY["EPSG","3880"]]""";
        case 3881 -> """
                PROJCS["EUREF-FIN / GK27FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 27.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 27500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Northing", NORTH],\s
                  AXIS["Easting", EAST],\s
                  AUTHORITY["EPSG","3881"]]""";
        case 3882 -> """
                PROJCS["EUREF-FIN / GK28FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 28.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 28500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Northing", NORTH],\s
                  AXIS["Easting", EAST],\s
                  AUTHORITY["EPSG","3882"]]""";
        case 3883 -> """
                PROJCS["EUREF-FIN / GK29FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 29.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 29500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Northing", NORTH],\s
                  AXIS["Easting", EAST],\s
                  AUTHORITY["EPSG","3883"]]""";
        case 3884 -> """
                PROJCS["EUREF-FIN / GK30FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 30.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 30500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Northing", NORTH],\s
                  AXIS["Easting", EAST],\s
                  AUTHORITY["EPSG","3884"]]""";
        case 3885 -> """
                PROJCS["EUREF-FIN / GK31FIN",\s
                  GEOGCS["EUREF-FIN",\s
                    DATUM["EUREF-FIN",\s
                      SPHEROID["GRS 1980", 6378137.0, 298.257222101, AUTHORITY["EPSG","7019"]],\s
                      TOWGS84[0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0],\s
                      AUTHORITY["EPSG","1391"]],\s
                    PRIMEM["Greenwich", 0.0, AUTHORITY["EPSG","8901"]],\s
                    UNIT["degree", 0.017453292519943295],\s
                    AXIS["Geodetic latitude", NORTH],\s
                    AXIS["Geodetic longitude", EAST],\s
                    AUTHORITY["EPSG","10690"]],\s
                  PROJECTION["Transverse_Mercator", AUTHORITY["EPSG","9807"]],\s
                  PARAMETER["central_meridian", 31.0],\s
                  PARAMETER["latitude_of_origin", 0.0],\s
                  PARAMETER["scale_factor", 1.0],\s
                  PARAMETER["false_easting", 31500000.0],\s
                  PARAMETER["false_northing", 0.0],\s
                  UNIT["m", 1.0],\s
                  AXIS["Northing", NORTH],\s
                  AXIS["Easting", EAST],\s
                  AUTHORITY["EPSG","3885"]]""";
        default -> null;
        };
    }

}
