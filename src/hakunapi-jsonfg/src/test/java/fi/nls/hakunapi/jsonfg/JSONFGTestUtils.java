package fi.nls.hakunapi.jsonfg;

import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;

import fi.nls.hakunapi.core.DatetimeProperty;
import fi.nls.hakunapi.core.FeatureCollectionWriter;
import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.FeatureStream;
import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FeatureWriter;
import fi.nls.hakunapi.core.NextCursor;
import fi.nls.hakunapi.core.ObjectArrayValueContainer;
import fi.nls.hakunapi.core.PaginationStrategyOffset;
import fi.nls.hakunapi.core.SimpleFeatureType;
import fi.nls.hakunapi.core.ValueProvider;
import fi.nls.hakunapi.core.geom.HakunaGeometry;
import fi.nls.hakunapi.core.geom.HakunaGeometryDimension;
import fi.nls.hakunapi.core.geom.HakunaGeometryJTS;
import fi.nls.hakunapi.core.geom.HakunaGeometryType;
import fi.nls.hakunapi.core.param.LimitParam;
import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import fi.nls.hakunapi.core.property.HakunaPropertyWriters;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyDate;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyLong;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyString;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyTimestamptz;
import fi.nls.hakunapi.core.request.WriteReport;
import fi.nls.hakunapi.core.schemas.Crs;
import fi.nls.hakunapi.proj.gt.GeoToolsProjectionTransformerFactory;

public class JSONFGTestUtils {

    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);

    protected final int SRID = Crs.CRS84_SRID;
    protected final int PLACE_SRID = 3067;
    protected final boolean PLACE_SRID_IS_LATLON = false;

    static final GeometryFactory geomFac = new GeometryFactory();
    static final GeoToolsProjectionTransformerFactory projFac = new GeoToolsProjectionTransformerFactory();

    protected static final List<HakunaGeometry> GEOMS = Arrays.asList(
            new HakunaGeometryJTS(geomFac.createPoint(new Coordinate(385511.123, 6675311.111))),
            new HakunaGeometryJTS(geomFac.createPoint(new Coordinate(385022.231, 6674022.222))));

    final HakunaPropertyGeometry geomProp = new HakunaPropertyGeometry("geometry", "table", "geometry", true,
            HakunaGeometryType.POINT, new int[] { SRID }, PLACE_SRID, 2,
            HakunaPropertyWriters.getGeometryPropertyWriter("geometry", true));

    final HakunaPropertyString stringProp = new HakunaPropertyString("prop", "table", "prop", true, false,
            HakunaPropertyWriters.getSimplePropertyWriter("prop", HakunaPropertyType.STRING));

    protected static final List<Object[]> FEATURES_WITH_TIMESTAMP = Arrays.asList(
            //
            new Object[] { 1L, GEOMS.get(0), "propValue1", Instant.now() },
            //
            new Object[] { 2L, GEOMS.get(1), "propValue2", null });

    public FeatureStream getFeaturesWithTimestamp() {
        return new TestFeaturesStream(FEATURES_WITH_TIMESTAMP);
    }

    public FeatureType getFeatureTypeWithTimestamp(String ftName) {
        final HakunaPropertyTimestamptz timestampzProp = new HakunaPropertyTimestamptz("time", "table", "time", true,
                false, HakunaPropertyWriters.getSimplePropertyWriter("time", HakunaPropertyType.TIMESTAMPTZ));

        return new SimpleFeatureType() {
            {
                setName(ftName);

                final HakunaPropertyLong idProp = new HakunaPropertyLong("id", "table", "id", false, true,
                        HakunaPropertyWriters.getIdPropertyWriter(this, getName(), "id", HakunaPropertyType.LONG));
                setId(idProp);
                setGeom(geomProp);

                setGeomDimension(HakunaGeometryDimension.XY);

                final List<HakunaProperty> properties = Arrays.asList(stringProp, timestampzProp);
                setProperties(properties);
                List<DatetimeProperty> datetimeProperties = Arrays.asList(new DatetimeProperty(timestampzProp, true));
                setDatetimeProperties(datetimeProperties);
                setPaginationStrategy(PaginationStrategyOffset.INSTANCE);
                setStaticFilters(Arrays.asList());
                setProjectionTransformerFactory(projFac);
            }

            @Override
            public FeatureProducer getFeatureProducer() {
                return null;
            }

        };
    }

    protected static final List<Object[]> FEATURES_WITH_DATE = Arrays.asList(
            //
            new Object[] { 1L, GEOMS.get(0), "propValue1", LocalDate.now() },
            //
            new Object[] { 2L, GEOMS.get(1), "propValue2", null });

    public FeatureStream getFeaturesWithDate() {
        return new TestFeaturesStream(FEATURES_WITH_DATE);
    }

    public FeatureType getFeatureTypeWithDate(String ftName) {
        final HakunaPropertyDate dateProp = new HakunaPropertyDate("date", "table", "date", true, false,
                HakunaPropertyWriters.getSimplePropertyWriter("date", HakunaPropertyType.DATE));

        return new SimpleFeatureType() {
            {
                setName(ftName);

                final HakunaPropertyLong idProp = new HakunaPropertyLong("id", "table", "id", false, true,
                        HakunaPropertyWriters.getIdPropertyWriter(this, getName(), "id", HakunaPropertyType.LONG));
                setId(idProp);
                setGeom(geomProp);

                setGeomDimension(HakunaGeometryDimension.XY);

                final List<HakunaProperty> properties = Arrays.asList(stringProp, dateProp);
                setProperties(properties);
                List<DatetimeProperty> datetimeProperties = Arrays.asList(new DatetimeProperty(dateProp, true));
                setDatetimeProperties(datetimeProperties);
                setPaginationStrategy(PaginationStrategyOffset.INSTANCE);
                setStaticFilters(Arrays.asList());
                setProjectionTransformerFactory(projFac);
            }

            @Override
            public FeatureProducer getFeatureProducer() {
                return null;
            }

        };
    }

    public class TestFeaturesStream implements FeatureStream {

        int n = 0;
        private List<Object[]> features;

        TestFeaturesStream(List<Object[]> features) {
            this.features = features;
        }

        @Override
        public void close() throws Exception {
        }

        @Override
        public boolean hasNext() {
            return n < features.size();
        }

        @Override
        public ValueProvider next() {
            return ObjectArrayValueContainer.wrap(features.get(n++));
        }

    }

    public void validateFeatureCollection(JsonNode jsonNode) {
        JsonSchema jsonSchema = factory
                .getSchema(JSONFGTestUtils.class.getResourceAsStream("featurecollection.min.json"));

        Set<ValidationMessage> errors = jsonSchema.validate(jsonNode);
        for (ValidationMessage err : errors) {
            System.err.println(err);
        }
        assertTrue(errors.isEmpty());
    }

    public void validateFeatureWithDate(JsonNode jsonNode) {
        JsonSchema jsonSchema = factory
                .getSchema(JSONFGTestUtils.class.getResourceAsStream("feature-with-date-schema.json"));

        Set<ValidationMessage> errors = jsonSchema.validate(jsonNode);
        for (ValidationMessage err : errors) {
            System.err.println(err);
        }
        assertTrue(errors.isEmpty());
    }

    public void validateFeatureWithTimestamp(JsonNode jsonNode) {
        JsonSchema jsonSchema = factory
                .getSchema(JSONFGTestUtils.class.getResourceAsStream("feature-with-timestamp-schema.json"));

        Set<ValidationMessage> errors = jsonSchema.validate(jsonNode);
        assertTrue(errors.isEmpty());

    }

    public static WriteReport writeFeatureCollection(FeatureCollectionWriter writer, FeatureType ft,
            List<HakunaProperty> properties, FeatureStream features, int offset, int limit) throws Exception {
        if (limit == LimitParam.UNLIMITED) {
            return writeFeatureCollectionFully(writer, ft, properties, features);
        }

        int numberReturned = 0;
        for (; numberReturned < limit; numberReturned++) {
            if (!features.hasNext()) {
                break;
            }
            writeFeature(writer, ft, properties, features.next());
            writer.endFeature();
        }

        NextCursor next = null;
        if (numberReturned == limit && features.hasNext()) {
            next = ft.getPaginationStrategy().getNextCursor(offset, limit, features.next());
        }

        features.close();

        return new WriteReport(numberReturned, next);
    }

    public static WriteReport writeFeatureCollectionFully(FeatureCollectionWriter writer, FeatureType ft,
            List<HakunaProperty> properties, FeatureStream features) throws Exception {
        int numberReturned = 0;
        while (features.hasNext()) {
            ValueProvider feature = features.next();
            writeFeature(writer, ft, properties, feature);
            writer.endFeature();
            numberReturned++;
        }
        return new WriteReport(numberReturned, null);
    }

    public static void writeFeature(FeatureWriter writer, FeatureType ft, List<HakunaProperty> properties,
            ValueProvider feature) throws Exception {
        int i = 0;
        for (HakunaProperty property : properties) {
            property.write(feature, i++, writer);
        }
    }
}
