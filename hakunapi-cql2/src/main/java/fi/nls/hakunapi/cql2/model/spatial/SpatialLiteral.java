package fi.nls.hakunapi.cql2.model.spatial;

import org.locationtech.jts.geom.Geometry;

public class SpatialLiteral implements SpatialExpression {

    private final Geometry geometry;

    public SpatialLiteral(Geometry geometry) {
        this.geometry = geometry;
    }

    public Geometry getGeometry() {
        return geometry;
    }

}
