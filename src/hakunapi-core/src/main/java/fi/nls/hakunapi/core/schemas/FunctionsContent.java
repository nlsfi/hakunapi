package fi.nls.hakunapi.core.schemas;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

public class FunctionsContent implements Component {

    @Override
    public String getComponentName() {
        return "functions";
    }

    private final List<FunctionInfo> functions;

    public FunctionsContent() {
        this.functions = null;
    }

    public FunctionsContent(List<FunctionInfo> functions) {
        this.functions = Objects.requireNonNull(functions);
    }

    @Override
    public Schema toSchema(Map<String, Schema> components) {
        if (!components.containsKey(getComponentName())) {
            Schema schema = new ObjectSchema().name(getComponentName()).addProperties("functions",
                    new ArraySchema().items(new FunctionInfo().toSchema(components)));
            components.put(getComponentName(), schema);
        }
        return new Schema<>().$ref(getComponentName());
    }

    public List<FunctionInfo> getFunctions() {
        return functions;
    }

}
