package fi.nls.hakunapi.core.transformer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyComposite;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class StringJoinerTransformer implements ValueTransformer {
    
    private List<HakunaProperty> properties;
    private String separator;

    @Override
    public void init(HakunaProperty property, String arg) throws Exception {
        if (!(property instanceof HakunaPropertyComposite)) {
            throw new IllegalArgumentException("Expected HakunaPropertyComposite");
        }
        this.properties = ((HakunaPropertyComposite) property).getParts();
        this.separator = arg;
    }

    @Override
    public HakunaPropertyType getPublicType() {
        return HakunaPropertyType.STRING;
    }

    @Override
    public Object toPublic(Object value) throws IllegalArgumentException {
        if (value == null) {
            return null;
        }
        Object[] arr = (Object[]) value;
        return Arrays.stream(arr)
                .map(Object::toString)
                .collect(Collectors.joining(separator));
    }

    @Override
    public Schema<?> getPublicSchema() {
        return new StringSchema();
    }

    @Override
    public Object toInner(String value) throws IllegalArgumentException {
        String[] separated = value.split(separator);
        if (separated.length != properties.size()) {
            return null;
        }
        Object[] values = new Object[properties.size()];
        for (int i = 0; i < properties.size(); i++) {
            values[i] = properties.get(i).toInner(separated[i]);
        }
        return values;
    }

}
