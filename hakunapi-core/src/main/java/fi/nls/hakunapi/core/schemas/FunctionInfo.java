package fi.nls.hakunapi.core.schemas;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class FunctionInfo extends ExtendableComponent {

    private String name;
    private String description;
    private String metadataUrl;
    private List<FunctionArgumentInfo> arguments;
    private List<String> returns;

    public FunctionInfo(String name, String description, String metadataUrl) {
        this.name = name;
        this.description = description;
        this.metadataUrl = metadataUrl;
    }

    public FunctionInfo() {
    }

    @Override
    public String getComponentName() {
        return "function-info";
    }

    List<String> funcInOut = Arrays.asList("string", "number", "integer", "datetime", "geometry", "boolean");

    @SuppressWarnings("rawtypes")
    @Override
    public Schema toSchema(Map<String, Schema> components) {
        if (!components.containsKey(getComponentName())) {
            StringSchema schemaInOut = new StringSchema();
            for (String s : funcInOut) {
                schemaInOut.addEnumItem(s);
            }
            Schema schema = new ObjectSchema().name(getComponentName())
                    .addProperties("name", new StringSchema().description("name of the function"))
                    .addProperties("description", new StringSchema().description("description of the function"))
                    .addProperties("metadataUrl", new StringSchema().description("metadata url for function"))
                    .addProperties("arguments", new ArraySchema().items(new ObjectSchema())
                            .description("a description of the arguments of the function")
                            .addProperties("title", new StringSchema()).addProperties("description", new StringSchema())
                            .addProperties("type", new ArraySchema().items(schemaInOut)))
                    .addProperties("returns", new ArraySchema().items(schemaInOut))
                    .description("a description of the results of the function");
            addExtensionsToSchema(schema);
            components.put(getComponentName(), schema);
        }
        return new Schema<>().$ref(getComponentName());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMetadataUrl() {
        return metadataUrl;
    }

    public void setMetadataUrl(String metadataUrl) {
        this.metadataUrl = metadataUrl;
    }

    public List<FunctionArgumentInfo> getArguments() {
        return arguments;
    }

    public void setArguments(List<FunctionArgumentInfo> arguments) {
        this.arguments = arguments;
    }

    public List<String> getReturns() {
        return returns;
    }

    public void setReturns(FunctionReturnsInfo returns) {
        this.returns = returns.getType();
    }

}
