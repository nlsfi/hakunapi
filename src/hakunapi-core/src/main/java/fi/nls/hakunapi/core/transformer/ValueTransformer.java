package fi.nls.hakunapi.core.transformer;

import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import io.swagger.v3.oas.models.media.Schema;

public interface ValueTransformer {
    
    public void init(HakunaProperty property, String arg) throws Exception;
    public HakunaPropertyType getPublicType();
    public Object toInner(String value) throws IllegalArgumentException;
    public Object toPublic(Object value) throws IllegalArgumentException;
    public Schema<?> getPublicSchema();

}
