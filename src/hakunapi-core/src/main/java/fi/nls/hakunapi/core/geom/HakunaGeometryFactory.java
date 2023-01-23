package fi.nls.hakunapi.core.geom;

import org.locationtech.jts.geom.GeometryFactory;

public class HakunaGeometryFactory {

    public static final GeometryFactory GF = new GeometryFactory(new HakunaCoordinateSequenceFactory());

}
