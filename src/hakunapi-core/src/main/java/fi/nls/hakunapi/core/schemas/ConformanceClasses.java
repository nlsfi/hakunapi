package fi.nls.hakunapi.core.schemas;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fi.nls.hakunapi.core.ConformanceClass;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class ConformanceClasses implements Component {

    public final List<String> conformsTo;

    public ConformanceClasses() {
        conformsTo = null;
    }

    public ConformanceClasses(ConformanceClass... reqClass) {
        this(Arrays.asList(reqClass));
    }

    public ConformanceClasses(List<ConformanceClass> conformanceClasses) {
        this.conformsTo = conformanceClasses.stream().map(c -> c.uri).collect(Collectors.toList());
    }

    public List<String> getConformsTo() {
        return Collections.unmodifiableList(conformsTo);
    }

    @Override
    public String getComponentName() {
        return "req-classes";
    }

    @Override
    public Schema toSchema(Map<String, Schema> components) {
        String[] example = {
                ConformanceClass.Core.uri,
                ConformanceClass.OpenAPI30.uri,
                ConformanceClass.GeoJSON.uri
        };
        if (!components.containsKey(getComponentName())) {
            ObjectSchema schema = new ObjectSchema();
            components.put(getComponentName(), schema);
            schema
            .name(getComponentName())
            .addProperties("conformsTo", new ArraySchema().items(new StringSchema()).example(example));
            schema.setRequired(Collections.singletonList("conformsTo"));
            components.put(getComponentName(), schema);
        }
        return new Schema<>().$ref(getComponentName());
    }

}
