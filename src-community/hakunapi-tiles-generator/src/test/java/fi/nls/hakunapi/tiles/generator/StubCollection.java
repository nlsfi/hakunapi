package fi.nls.hakunapi.tiles.generator;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import fi.nls.hakunapi.core.DatetimeProperty;
import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.FeatureStream;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.OrderBy;
import fi.nls.hakunapi.core.PaginationStrategy;
import fi.nls.hakunapi.core.QueryContext;
import fi.nls.hakunapi.core.ValueMapper;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.core.filter.FilterOp;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.geom.HakunaGeometryJTS;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.core.param.GetFeatureParam;
import fi.nls.hakunapi.core.projection.ProjectionTransformerFactory;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.HakunaPropertyWriter;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.request.GetFeatureCollection;
import fi.nls.hakunapi.core.request.GetFeatureRequest;
import fi.nls.hakunapi.core.util.StringPair;

public class StubCollection implements FeatureType {

    private static final GeometryFactory GF = new GeometryFactory();

    private static final HakunaPropertyWriter NOOP_WRITER = new HakunaPropertyWriter() {
        @Override
        public void write(ValueProvider p, int i, fi.nls.hakunapi.core.FeatureWriter w) {}
    };

    private final String name;
    private final int srid;
    private final HakunaProperty id;
    private final HakunaPropertyGeometry geom;
    private final HakunaProperty kohdeluokka;
    private final List<HakunaProperty> props;
    private final List<Point> points = new ArrayList<>();

    public volatile int queries;

    public StubCollection(String name, int srid) {
        this.name = name;
        this.srid = srid;
        this.id = new StubProperty("id", HakunaPropertyType.LONG);
        this.kohdeluokka = new StubProperty("kohdeluokka", HakunaPropertyType.INT);
        this.geom = new HakunaPropertyGeometry("geom", "t", "geom", true,
                HakunaGeometryType.POINT, new int[] { srid }, srid, 2, NOOP_WRITER);
        this.props = new ArrayList<>();
        this.props.add(geom);
        this.props.add(kohdeluokka);
    }

    public StubCollection add(long fid, double x, double y, int kohdeluokka) {
        points.add(new Point(fid, x, y, kohdeluokka));
        return this;
    }

    private record Point(long fid, double x, double y, int kohdeluokka) {}

    @Override
    public FeatureProducer getFeatureProducer() {
        return new FeatureProducer() {
            @Override
            public int getNumberMatched(GetFeatureRequest r, GetFeatureCollection c) {
                return points.size();
            }

            @Override
            public FeatureStream getFeatures(GetFeatureRequest r, GetFeatureCollection c) {
                queries++;
                double[] bbox = bboxOf(c);
                List<Row> rows = new ArrayList<>();
                for (int i = 0; i < points.size(); i++) {
                    Point p = points.get(i);
                    if (bbox == null || (p.x() >= bbox[0] && p.x() <= bbox[2]
                            && p.y() >= bbox[1] && p.y() <= bbox[3])) {
                        rows.add(row(p));
                    }
                }
                return new StubStream(rows);
            }
        };
    }

    private static double[] bboxOf(GetFeatureCollection c) {
        List<Filter> filters = c.getFilters();
        for (int i = 0; i < filters.size(); i++) {
            Filter f = filters.get(i);
            if ((f.getOp() == FilterOp.INTERSECTS_INDEX || f.getOp() == FilterOp.INTERSECTS)
                    && f.getValue() instanceof Geometry g) {
                Envelope e = g.getEnvelopeInternal();
                return new double[] { e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY() };
            }
        }
        return null;
    }

    private Row row(Point p) {
        Geometry g = GF.createPoint(new Coordinate(p.x(), p.y()));
        g.setSRID(srid);
        HakunaGeometry hg = new HakunaGeometryJTS(g);
        return new Row(new Object[] { p.fid(), hg, hg, p.kohdeluokka() });
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNS() {
        return null;
    }

    @Override
    public String getSchemaLocation() {
        return null;
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return Collections.emptyMap();
    }

    @Override
    public HakunaProperty getId() {
        return id;
    }

    @Override
    public HakunaPropertyGeometry getGeom() {
        return geom;
    }

    @Override
    public List<HakunaProperty> getProperties() {
        return props;
    }

    @Override
    public List<HakunaProperty> getQueryableProperties() {
        return props;
    }

    @Override
    public List<DatetimeProperty> getDatetimeProperties() {
        return Collections.emptyList();
    }

    @Override
    public double[] getSpatialExtent() {
        return null;
    }

    @Override
    public Instant[] getTemporalExtent() {
        return null;
    }

    @Override
    public List<GetFeatureParam> getParameters() {
        return Collections.emptyList();
    }

    @Override
    public List<Filter> getStaticFilters() {
        return Collections.emptyList();
    }

    @Override
    public ProjectionTransformerFactory getProjectionTransformerFactory() {
        return null;
    }

    @Override
    public PaginationStrategy getPaginationStrategy() {
        return null;
    }

    @Override
    public List<OrderBy> getDefaultOrderBy() {
        return Collections.emptyList();
    }

    private static final class Row implements ValueProvider {
        private final Object[] vals;

        Row(Object[] vals) {
            this.vals = vals;
        }

        @Override
        public int size() {
            return vals.length;
        }

        @Override
        public boolean isNull(int i) {
            return vals[i] == null;
        }

        @Override
        public Boolean getBoolean(int i) {
            return (Boolean) vals[i];
        }

        @Override
        public Integer getInt(int i) {
            return vals[i] instanceof Integer v ? v : null;
        }

        @Override
        public Long getLong(int i) {
            if (vals[i] instanceof Long v) {
                return v;
            }
            return vals[i] instanceof Integer v ? v.longValue() : null;
        }

        @Override
        public Float getFloat(int i) {
            return (Float) vals[i];
        }

        @Override
        public Double getDouble(int i) {
            return (Double) vals[i];
        }

        @Override
        public String getString(int i) {
            return vals[i] == null ? null : vals[i].toString();
        }

        @Override
        public Instant getInstant(int i) {
            return (Instant) vals[i];
        }

        @Override
        public LocalDateTime getLocalDateTime(int i) {
            return vals[i] instanceof LocalDateTime v ? v : null;
        }

        @Override
        public LocalDate getLocalDate(int i) {
            return vals[i] instanceof LocalDate v ? v : null;
        }

        @Override
        public HakunaGeometry getHakunaGeometry(int i) {
            return (HakunaGeometry) vals[i];
        }

        @Override
        public Object[] getArray(int i) {
            return null;
        }

        @Override
        public UUID getUUID(int i) {
            return null;
        }

        @Override
        public Object getObject(int i) {
            return vals[i];
        }
    }

    private static final class StubStream implements FeatureStream {
        private final Iterator<Row> it;

        StubStream(List<Row> rows) {
            this.it = rows.iterator();
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public ValueProvider next() {
            return it.next();
        }

        @Override
        public void close() {}
    }

    private static final class StubProperty implements HakunaProperty {
        private final String name;
        private final HakunaPropertyType type;

        StubProperty(String name, HakunaPropertyType type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getTable() {
            return null;
        }

        @Override
        public String getColumn() {
            return name;
        }

        @Override
        public HakunaPropertyType getType() {
            return type;
        }

        @Override
        public boolean nullable() {
            return true;
        }

        @Override
        public boolean unique() {
            return false;
        }

        @Override
        public io.swagger.v3.oas.models.media.Schema<?> getSchema() {
            return null;
        }

        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public FeatureType getFeatureType() {
            return null;
        }

        @Override
        public void setFeatureType(FeatureType ft) {}

        @Override
        public HakunaPropertyWriter getPropertyWriter() {
            return null;
        }

        @Override
        public ValueMapper getMapper(Map<StringPair, Integer> columnToIndex, int out, QueryContext ctx) {
            return null;
        }

        @Override
        public Object toInner(String value) {
            return value;
        }
    }

}
