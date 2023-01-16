package fi.nls.hakunapi.core.transformer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyTransformed;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class JSONCodeListTransformer implements ValueTransformer {

    private static final String NULL_KEY = "$null";

    private HakunaPropertyType innerType;
    private HakunaPropertyType publicType;
    private Map toInner;
    private Map toPublic;

    private Object publicNullValue; // returned by toPublic(null)
    private Object innerNullValue; // returned by toInner(null);
    private boolean innerNullValueSet;

    @Override
    public void init(HakunaProperty property, String codelist) throws Exception {
        if (!(property instanceof HakunaPropertyTransformed)) {
            throw new IllegalArgumentException("Expected HakunaPropertyTransformed");
        }
        HakunaPropertyTransformed t = (HakunaPropertyTransformed) property;
        this.innerType = t.getInnerType();

        URL url = new URL(codelist);
        Map<String, Object> map = readMap(url);

        initFromMap(innerType, map);
    }

    protected Map<String, Object> readMap(URL url) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper om = new ObjectMapper();
        TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
        return om.readValue(url, typeRef);
    }

    protected void initFromMap(HakunaPropertyType innerType, Map<String, Object> map) {
        Object firstValue = map.values().iterator().next();
        if (firstValue.getClass() == Integer.class
                || firstValue.getClass() == Byte.class
                || firstValue.getClass() == Short.class) {
            this.publicType = HakunaPropertyType.INT;
        } else if (firstValue.getClass() == Long.class) {
            this.publicType = HakunaPropertyType.LONG;
        } else if (firstValue.getClass() == String.class) {
            this.publicType = HakunaPropertyType.STRING;
        } else {
            throw new IllegalArgumentException("Can't handle values of class " + firstValue.getClass().getName());
        }

        toInner = new HashMap();
        toPublic = new HashMap();
        publicNullValue = null;
        innerNullValue = null;

        for (Map.Entry<String, Object> e : map.entrySet())  {
            final Object innerValue = getInnerValue(e.getKey(), innerType);
            final Object publicValue = e.getValue();
            if (innerValue == null) {
                publicNullValue = publicValue;
                if (toInner.containsKey(publicValue)) {
                    Object currentInnerValue = toInner.get(publicValue);
                    toInner.put(publicValue, combine(currentInnerValue, innerValue));
                } else {
                    toInner.put(publicValue, innerValue);
                }
            } else if (publicValue == null) {
                if (!innerNullValueSet) {
                    innerNullValue = innerValue;
                    innerNullValueSet = true;
                } else {
                    innerNullValue = combine(innerNullValue, innerValue);
                }
                if (toPublic.containsKey(innerValue)) {
                    Object currentPublicValue = toPublic.get(innerValue);
                    toPublic.put(innerValue, combine(currentPublicValue, innerValue));
                } else {
                    toPublic.put(innerValue, publicValue);
                }
            } else {
                toPublic.put(innerValue, publicValue);
                if (toInner.containsKey(publicValue)) {
                    Object currentInnerValue = toInner.get(publicValue);
                    toInner.put(publicValue, combine(currentInnerValue, innerValue));
                } else {
                    toInner.put(publicValue, innerValue);
                }
            }
        }
    }

    private Object getInnerValue(String key, HakunaPropertyType innerType) {
        if (NULL_KEY.equalsIgnoreCase(key)) {
            return null;
        }
        switch (innerType) {
        case INT:
            return Integer.parseInt(key);
        case LONG:
            return Long.parseLong(key);
        case STRING:
            return key;
        default:
            throw new IllegalArgumentException("Invalid inner type");
        }
    }

    /**
     * merge two values into a list, or append valueToAdd to the list if it already is a list
     * if both are null then just return null
     */
    private Object combine(Object currentValue, Object valueToAdd) {
        if (currentValue == null && valueToAdd == null) {
            return null;
        } else if (currentValue != null && currentValue instanceof Collection) {
            List c = (List) currentValue;
            c.add(valueToAdd);
            return c;
        } else {
            List<Object> c = new ArrayList<>();
            c.add(currentValue);
            c.add(valueToAdd);
            return c;
        }
    }

    @Override
    public HakunaPropertyType getPublicType() {
        return publicType;
    }

    @Override
    public Object toInner(String key) {
        if (key == null) {
            return innerNullValue;
        }
        Object k = toInnerType(key);
        Object value = toInner.get(k);
        if (value == null) {
            return HakunaProperty.UNKNOWN;
        }
        return value;
    }

    private Object toInnerType(String str) {
        switch (publicType) {
        case INT:
            return Integer.parseInt(str);
        case LONG:
            return Long.parseLong(str);
        case STRING:
            return str;
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Object toPublic(Object key) {
        if (key == null) {
            return publicNullValue;
        }
        return toPublic.get(key);
    }

    public Collection<Object> getEnum() {
        return toPublic.values();
    }

    @Override
    public Schema<?> getPublicSchema() {
        switch (publicType) {
        case INT:
            return new IntegerSchema();
        case LONG:
            return new IntegerSchema().type("int64");
        case STRING:
            return new StringSchema();
        default:
            throw new IllegalArgumentException();
        }
    }

}
