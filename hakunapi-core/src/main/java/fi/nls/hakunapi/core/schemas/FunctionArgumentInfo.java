package fi.nls.hakunapi.core.schemas;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class FunctionArgumentInfo {

    String title;
    String description;
    List<String> type;

    public FunctionArgumentInfo(String title, String description, FunctionArgumentType types) {
        super();
        this.title = title;
        this.description = description;
        this.type = Stream.of(types).map(t -> t.name()).collect(Collectors.toList());
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getType() {
        return type;
    }

    public enum FunctionArgumentType {
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
        FunctionArgumentType(Schema s) {
            s.name(name());
        }
    }

}
