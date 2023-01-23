package fi.nls.hakunapi.core.schemas;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

public class Extent implements Component {

    private static final String DESCRIPTION = "The extent of the features in the collection. In the Core only spatial and temporal"
            + "extents are specified. Extensions may add additional members to represent other "
            + "extents, for example, thermal or pressure ranges.";

    private final SpatialExtent spatialExtent;
    private final TemporalExtent temporalExtent;

    Extent() {
        spatialExtent = null;
        temporalExtent = null;
    }

    public Extent(SpatialExtent spatialExtent, TemporalExtent temporalExtent) {
        this.spatialExtent = spatialExtent;
        this.temporalExtent = temporalExtent;
    }

    @Override
    public String getComponentName() {
        return "extent";
    }

    @Override
    public Schema toSchema(Map<String, Schema> components) {
        if (!components.containsKey(getComponentName())) {
            Schema spatial = SpatialExtent.toSchema();
            Schema temporal = TemporalExtent.toSchema();
            Schema schema = new ObjectSchema()
                    .addProperties("spatial", spatial)
                    .addProperties("temporal", temporal)
                    .description(DESCRIPTION);
            components.put(getComponentName(), schema);
        }
        return new Schema<>().$ref(getComponentName());
    }

    @JsonProperty("spatial")
    public SpatialExtent getSpatialExtent() {
        return spatialExtent;
    }

    @JsonProperty("temporal")
    public TemporalExtent getTemporalExtent() {
        return temporalExtent;
    }

}
