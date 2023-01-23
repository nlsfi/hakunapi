package fi.nls.hakunapi.core.tiles;

import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fi.nls.hakunapi.core.schemas.Component;
import fi.nls.hakunapi.core.schemas.Link;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class TilingSchemes implements Component {

    private final Collection<TilingSchemeInfo> tilingSchemes;

    public TilingSchemes() {
        // No-arg constructor required for Component
        this.tilingSchemes = null;
    }
    
    public TilingSchemes(Collection<TilingSchemeInfo> tilingSchemes) {
        this.tilingSchemes = tilingSchemes;
    }

    public Collection<TilingSchemeInfo> getTilingSchemes() {
        return tilingSchemes;
    }

    @JsonIgnore
    @Override
    public String getComponentName() {
        return "tilingSchemes";
    }

    @JsonIgnore
    @Override
    public Schema toSchema(Map<String, Schema> components) {
        if (!components.containsKey(getComponentName())) {
            Schema tilingScheme = new ObjectSchema()
                    .addProperties("identifier", new StringSchema())
                    .addProperties("links", new ArraySchema().items(new Link().toSchema(components)))
                    .addRequiredItem("identifier")
                    .addRequiredItem("links");
            Schema schema = new ObjectSchema()
                    .addProperties("tilingSchemes", new ArraySchema().items(tilingScheme));
            components.put(getComponentName(), schema);
        }
        return new Schema<>().$ref(getComponentName());
    }

}
