package fi.nls.hakunapi.core.schemas;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class FunctionReturnsInfo {

    List<String> type;

    public FunctionReturnsInfo(FunctionReturnsType... types) {
        super();
        this.type = Stream.of(types).map(t -> t.name()).collect(Collectors.toList());
    }

    public List<String> getType() {
        return type;
    }

    public enum FunctionReturnsType {
        string(new StringSchema()),
        //
        number(new NumberSchema()),
        //
        geometry(new ComposedSchema().oneOf(Arrays.asList(new Schema<>().$ref("https://geojson.org/schema/Point.json"),
                new Schema<>().$ref("https://geojson.org/schema/LineString.json"),
                new Schema<>().$ref("https://geojson.org/schema/Polygon.json"),
                new Schema<>().$ref("https://geojson.org/schema/MultiPoint.json"),
                new Schema<>().$ref("https://geojson.org/schema/MultiLineString.json"),
                new Schema<>().$ref("https://geojson.org/schema/MultiPolygon.json"))));
        @SuppressWarnings("rawtypes")
        FunctionReturnsType(Schema s) {
            s.name(name());
        }
    }

}
