package fi.nls.hakunapi.core.projection;

import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Geometry;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.util.CrsUtil;

public class ProjectionHelper {
    
    public static Geometry reprojectToStorageCRS(HakunaPropertyGeometry prop, Geometry geom) {
        int geomSRID = geom.getSRID();
        int storageSRID = prop.getStorageSRID();
        if (geomSRID == storageSRID) {
            return geom;
        }

        FeatureType ft = prop.getFeatureType();
        ProjectionTransformerFactory f = ft.getProjectionTransformerFactory();
        ProjectionTransformer t;
        try {
            t = f.getTransformer(geomSRID, storageSRID);
        } catch (Exception e) {
            throw new IllegalArgumentException(CrsUtil.ERR_UNSUPPORTED_CRS);
        }
        geom = ProjectionHelper.reproject(geom, t);

        return geom;
    }

    public static Geometry reproject(Geometry geom, ProjectionTransformer transformer) {
        if (transformer == NOPProjectionTransformer.INSTANCE) {
            return geom;
        }
        if (transformer.getFromSRID() == transformer.getToSRID()) {
            return geom;
        }

        geom.apply(new CoordinateSequenceFilter() {
            double[] xy = new double[2];

            public boolean isGeometryChanged() { return true; }
            public boolean isDone() { return false; }

            @Override
            public void filter(CoordinateSequence seq, int i) {
                xy[0] = seq.getX(i);
                xy[1] = seq.getY(i);
                try {
                    transformer.transformInPlace(xy, 0, 1);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                seq.setOrdinate(i, CoordinateSequence.X, xy[0]);
                seq.setOrdinate(i, CoordinateSequence.Y, xy[1]);
            }
        });
        geom.setSRID(transformer.getToSRID());

        return geom;
    }

}
