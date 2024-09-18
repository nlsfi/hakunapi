package fi.nls.hakunapi.proj.gt;

import org.geotools.referencing.CRS;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;

import fi.nls.hakunapi.core.projection.NOPProjectionTransformer;
import fi.nls.hakunapi.core.projection.ProjectionTransformer;
import fi.nls.hakunapi.core.projection.ProjectionTransformerFactory;
import fi.nls.hakunapi.core.schemas.Crs;

public class GeoToolsProjectionTransformerFactory implements ProjectionTransformerFactory {

    private static final CoordinateReferenceSystem ETRS89_LON_LAT;
    private static final CoordinateReferenceSystem CRS84;

    static {
        try {
            ETRS89_LON_LAT = CRS.decode("EPSG:4258", true);
            CRS84 = CRS.decode("EPSG:4326", true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ProjectionTransformer getTransformer(int sridFrom, int sridTo) throws Exception {
        if (sridFrom == sridTo) {
            return NOPProjectionTransformer.INSTANCE;
        }
        if (sridFrom == Crs.CRS84_SRID) {
            return fromCRS84(sridTo);
        }
        if (sridTo == Crs.CRS84_SRID) {
            return toCRS84(sridFrom);
        }
        CoordinateReferenceSystem from = getCRS(sridFrom);
        CoordinateReferenceSystem to = getCRS(sridTo);
        if (equals2D(from, to)) {
            // Double check if they are the same even without forcing lon,lat order
            CoordinateReferenceSystem fromAuthority = getCRSForce(sridFrom, false);
            CoordinateReferenceSystem toAuthority = getCRSForce(sridTo, false);
            if (equals2D(fromAuthority, toAuthority)) {
                return NOPProjectionTransformer.INSTANCE;
            }
        }
        return getProjectionTransformer(sridFrom, from, sridTo, to);
    }

    private boolean equals2D(CoordinateReferenceSystem from, CoordinateReferenceSystem to) {
        CoordinateReferenceSystem from2d = CRS.getHorizontalCRS(from);
        CoordinateReferenceSystem to2d = CRS.getHorizontalCRS(to);
        return CRS.equalsIgnoreMetadata(from2d, to2d);
    }

    @Override
    public ProjectionTransformer toCRS84(int sridFrom) throws Exception {
        CoordinateReferenceSystem from = getCRS(sridFrom);
        CoordinateReferenceSystem to;
        if (CRS.getEllipsoid(from).equals(CRS.getEllipsoid(ETRS89_LON_LAT))) {
            // This is a hack for performance
            // for example TM35 (east,north) -> WGS84 (lon,lat) takes about twice as long
            // compared to TM35 (east,north) -> ETRS89 (lat,lon) (same reference system)
            // TM35 (east,north) -> ETRS89 (lon,lat) takes even less time (no axis order swap?)
            to = ETRS89_LON_LAT;
        } else {
            to = CRS84;
        }
        return getProjectionTransformer(sridFrom, from, Crs.CRS84_SRID, to);
    }

    @Override
    public ProjectionTransformer fromCRS84(int sridTo) throws Exception {
        CoordinateReferenceSystem from = CRS.decode("EPSG:4326", true);
        CoordinateReferenceSystem to = getCRS(sridTo);
        return getProjectionTransformer(Crs.CRS84_SRID, from, sridTo, to);
    }

    private CoordinateReferenceSystem getCRS(int srid) throws Exception {
        if (srid == 3903) {
            // TODO: Figure out if this is causes more errors than it fixes
            srid = 3067;
        }
        return getCRSForce(srid, true);
    }

    private CoordinateReferenceSystem getCRSForce(int srid, boolean forceLonLat) throws Exception {
        return CRS.decode("EPSG:" + srid, forceLonLat);
    }

    private GeoToolsProjectionTransformer getProjectionTransformer(int fromSRID,
            CoordinateReferenceSystem fromCRS,
            int toSRID,
            CoordinateReferenceSystem toCRS) throws Exception {
        boolean lenient = true;
        MathTransform t = CRS.findMathTransform(fromCRS, toCRS, lenient);
        return new GeoToolsProjectionTransformer(fromSRID, toSRID, t);
    }

}
