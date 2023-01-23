package fi.nls.hakunapi.core.schemas;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class TemporalExtent {

    private static final String DESCRIPTION = "The temporal extent of the features in the collection.";
    private static final String DESCRIPTION_INTERVAL = "One or more time intervals that describe the temporal extent of the dataset. The value `null` is supported and indicates an open time interval. In the Core only a single time interval is supported. Extensions may support multiple intervals. If multiple intervals are provided, the union of the intervals describes the temporal extent.";
    private static final String DESCRIPTION_INTERVAL_ITEM = "Begin and end times of the time interval. The timestamps are in the coordinate reference system specified in `trs`. By default this is the Gregorian calendar.";
    private static final String DESCRIPTION_TRS = "Coordinate reference system of the coordinates in the temporal extent (property `interval`). The default reference system is the Gregorian calendar. In the Core this is the only supported temporal reference system. Extensions may support additional temporal reference systems and add additional enum values.";

    private final List<Instant[]> interval;
    private final String trs;

    public TemporalExtent(Instant[] interval, String trs) {
        this(Collections.singletonList(interval), trs);
    }

    public TemporalExtent(List<Instant[]> interval, String trs) {
        this.interval = interval;
        this.trs = trs;
    }

    public List<Instant[]> getInterval() {
        return interval;
    }

    public String getTrs() {
        return trs;
    }

    static Schema toSchema() {
        Schema singleIntervalSchema = new ArraySchema()
                .items(new DateTimeSchema().nullable(true))
                .minItems(2)
                .maxItems(2)
                .example(new Instant[] { Instant.parse("2011-11-11T12:22:11Z"), null })
                .description(DESCRIPTION_INTERVAL_ITEM);

        Schema intervalSchemaSchema = new ArraySchema()
                .items(singleIntervalSchema)
                .description(DESCRIPTION_INTERVAL);

        Schema trsSchema = new StringSchema()
                ._enum(Collections.singletonList(Trs.Gregorian))
                ._default(Trs.Gregorian)
                .description(DESCRIPTION_TRS);

        return new ObjectSchema()
                .addProperties("bbox", intervalSchemaSchema)
                .addProperties("crs", trsSchema)
                .description(DESCRIPTION);
    }

}
