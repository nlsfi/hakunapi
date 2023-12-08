package fi.nls.hakunapi.source.gpkg.filter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.projection.ProjectionHelper;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.source.gpkg.GpkgFeatureType;

public class GpkgIntersects implements SQLFilter {

    @Override
    public String toSQL(Filter filter) {
        HakunaPropertyGeometry prop = (HakunaPropertyGeometry) filter.getProp();
        GpkgFeatureType ft = (GpkgFeatureType) prop.getFeatureType();

        StringBuilder where = new StringBuilder();

        if (ft.isSpatialIndex()) {
            String col = SQLUtil.toSQL(ft.getId());
            where.append(col);
            where.append(" IN (");
            where.append("SELECT id");
            where.append(" FROM ").append(ft.getSpatialIndexName()).append(" rtree");
            where.append(" WHERE rtree.maxx >= ?");
            where.append(" AND   rtree.minx <= ?");
            where.append(" AND   rtree.maxy >= ?");
            where.append(" AND   rtree.miny <= ?");
            where.append(")");
        } else {
            String col = SQLUtil.toSQL(prop);
            where.append("(");
            where.append("ST_MaxX(").append(col).append(") >= ?");
            where.append(" AND ");
            where.append("ST_MinX(").append(col).append(") <= ?");
            where.append(" AND ");
            where.append("ST_MaxY(").append(col).append(") >= ?");
            where.append(" AND ");
            where.append("ST_MinY(").append(col).append(") <= ?");
            where.append(")");
        }

        return where.toString();
    }

    @Override
    public int bind(Filter filter, Connection c, PreparedStatement ps, int i) throws SQLException {
        HakunaPropertyGeometry prop = (HakunaPropertyGeometry) filter.getProp();
        Geometry geom = ProjectionHelper.reprojectToStorageCRS(prop, (Geometry) filter.getValue());
        Envelope envelope = geom.getEnvelopeInternal();

        ps.setDouble(i++, envelope.getMinX());
        ps.setDouble(i++, envelope.getMaxX());
        ps.setDouble(i++, envelope.getMinY());
        ps.setDouble(i++, envelope.getMaxY());

        return i;
    }

}
