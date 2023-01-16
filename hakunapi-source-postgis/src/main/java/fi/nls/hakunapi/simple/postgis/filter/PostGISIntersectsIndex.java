package fi.nls.hakunapi.simple.postgis.filter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBWriter;

import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.projection.ProjectionHelper;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;

public class PostGISIntersectsIndex implements SQLFilter {

    @Override
    public String toSQL(Filter filter) {
        HakunaProperty prop = filter.getProp();
        return String.format("\"%s\".\"%s\" && ST_GeomFromWKB(?, ?)",
                prop.getTable(), prop.getColumn());
    }

    @Override
    public int bind(Filter filter, Connection c, PreparedStatement ps, int i) throws SQLException {
        HakunaPropertyGeometry prop = (HakunaPropertyGeometry) filter.getProp();
        Geometry bbox = (Geometry) filter.getValue();
        bbox = ProjectionHelper.reprojectToStorageCRS(prop, bbox);

        int outputDimension = bbox.getDimension() > 2 ? 3 : 2;
        byte[] wkb = new WKBWriter(outputDimension, false).write(bbox);
        ps.setBytes(i++, wkb);
        ps.setInt(i++, bbox.getSRID());

        return i;
    }

}
