package fi.nls.hakunapi.core.property.simple;

import java.util.Arrays;
import java.util.function.BiConsumer;

import org.locationtech.jts.geom.CoordinateSequenceFilter;
import org.locationtech.jts.geom.Geometry;

import fi.nls.hakunapi.core.QueryContext;
import fi.nls.hakunapi.core.ValueContainer;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.geom.HakunaGeometryJTS;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.core.projection.JTSTransformer;
import fi.nls.hakunapi.core.projection.ProjectionTransformer;
import fi.nls.hakunapi.core.property.HakunaPropertyDynamic;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.HakunaPropertyWriter;
import fi.nls.hakunapi.core.schemas.Crs;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

public class HakunaPropertyGeometry extends HakunaPropertyDynamic {

    private final HakunaGeometryType geometryType;
    private final int[] srid;
    private final int storageSRID;
    private final int dimension;

    public HakunaPropertyGeometry(String name, String table, String column, boolean nullable, HakunaGeometryType geometryType, int[] srid, int storageSRID, int dimension, HakunaPropertyWriter propWriter) {
        super(name, table, column, HakunaPropertyType.GEOMETRY, nullable, false, propWriter);
        this.geometryType = geometryType;
        this.srid = addDefaultSrids(srid);
        this.storageSRID = storageSRID;
        this.dimension = dimension;
    }

    public HakunaGeometryType getGeometryType() {
        return geometryType;
    }

    protected static int[] addDefaultSrids(int[] srid) {
        srid = Arrays.stream(srid)
                .distinct()
                .filter(it -> it != Crs.CRS84_SRID && it != 4326)
                .toArray();
        int[] dst = new int[srid.length + 2];
        dst[0] = Crs.CRS84_SRID;
        dst[1] = 4326;
        System.arraycopy(srid, 0, dst, 2, srid.length);
        return dst;
    }

    public int[] getSrid() {
        return srid;
    }

    public int getDimension() {
        return dimension;
    }

    public int getStorageSRID() {
        return storageSRID;
    }

    public boolean isSRIDSupported(int srid) {
        return Arrays.stream(this.srid).anyMatch(it -> it == srid);
    }

    @Override
    public Schema<?> getSchema() {
        switch (geometryType) {
        case POINT:
            return new Schema<>().$ref("https://geojson.org/schema/Point.json");
        case LINESTRING:
            return new Schema<>().$ref("https://geojson.org/schema/LineString.json");
        case POLYGON:
            return new Schema<>().$ref("https://geojson.org/schema/Polygon.json");
        case MULTIPOINT:
            return new Schema<>().$ref("https://geojson.org/schema/MultiPoint.json");
        case MULTILINESTRING:
            return new Schema<>().$ref("https://geojson.org/schema/MultiLineString.json");
        case MULTIPOLYGON:
            return new Schema<>().$ref("https://geojson.org/schema/MultiPolygon.json");
        case GEOMETRYCOLLECTION:
            return new Schema<>().$ref("https://geojson.org/schema/GeometryCollection.json");
        case GEOMETRY:
            return new ComposedSchema().oneOf(Arrays.asList(
                    new Schema<>().$ref("https://geojson.org/schema/Point.json"),
                    new Schema<>().$ref("https://geojson.org/schema/LineString.json"),
                    new Schema<>().$ref("https://geojson.org/schema/Polygon.json"),
                    new Schema<>().$ref("https://geojson.org/schema/MultiPoint.json"),
                    new Schema<>().$ref("https://geojson.org/schema/MultiLineString.json"),
                    new Schema<>().$ref("https://geojson.org/schema/MultiPolygon.json")
            ));
        default:
            throw new RuntimeException("Unknown property type");
        }
    }

    @Override
    public Object toInner(String value) {
        return null;
    }

    @Override
    public BiConsumer<ValueProvider, ValueContainer> getMapperFunction(int iValueProvider, int iValueContainer, QueryContext ctx) {
        ProjectionTransformer t;
        try {
            t = getFeatureType().getProjectionTransformerFactory().getTransformer(getStorageSRID(), ctx.getSRID());
        } catch (Exception e) {
            throw new RuntimeException("Projetion transformation process failed", e);
        }
        if (t.isNOP()) {
            return (vp, vc) -> vc.setObject(iValueContainer, vp.getHakunaGeometry(iValueProvider));
        }
        
        CoordinateSequenceFilter csq = new JTSTransformer(t);

        return (vp, vc) -> {
            HakunaGeometry hg = vp.getHakunaGeometry(iValueProvider);
            if (hg == null) {
                vc.setNull(iValueContainer);
            } else {
                Geometry g = hg.toJTSGeometry();
                g.apply(csq);
                g.setSRID(t.getToSRID());
                hg = new HakunaGeometryJTS(g);
                vc.setObject(iValueContainer, hg);
            }
        };
    }

}
