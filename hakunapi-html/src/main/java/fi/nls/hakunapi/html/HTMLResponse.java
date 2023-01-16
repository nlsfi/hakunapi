package fi.nls.hakunapi.html;

import java.util.Map;

import fi.nls.hakunapi.core.schemas.Component;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class HTMLResponse implements Component {

    @Override
    public String getComponentName() {
        return null;
    }

    @Override
    public Schema toSchema(Map<String, Schema> components) {
        return new StringSchema();
    }

}
