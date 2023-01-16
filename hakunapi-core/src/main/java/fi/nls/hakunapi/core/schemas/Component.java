package fi.nls.hakunapi.core.schemas;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.models.media.Schema;

public interface Component {

    @JsonIgnore
    public abstract String getComponentName();
    @JsonIgnore
    public abstract Schema toSchema(Map<String, Schema> components);

}
