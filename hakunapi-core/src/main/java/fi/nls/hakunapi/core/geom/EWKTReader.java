package fi.nls.hakunapi.core.geom;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class EWKTReader {

    private final WKTReader wkt;

    public EWKTReader(WKTReader wkt) {
        this.wkt = wkt;
    }

    public Geometry read(String ewkt) throws ParseException {
        int srid = 0;
        if (ewkt.startsWith("SRID=")) {
            int i = ewkt.indexOf(';');
            if (i < 0) {
                throw new IllegalArgumentException("Invalid EWKT");
            }
            String srid_str = ewkt.substring("SRID=".length(), i);
            try {
                srid = Integer.parseInt(srid_str);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid EWKT");
            }
            ewkt = ewkt.substring(i + 1);
        }
        Geometry g = wkt.read(ewkt);
        g.setSRID(srid);
        return g;
    }

}
