package fi.nls.hakunapi.gml;

import java.util.Map;

import fi.nls.hakunapi.core.schemas.Component;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

/**
 * Replace me when OpenAPI gets support for XSD schemas 
 */
public class GMLResponse implements Component {

    @Override
    public String getComponentName() {
        return null;
    }

    @Override
    public Schema toSchema(Map<String, Schema> components) {
        return new StringSchema();
    }

}
