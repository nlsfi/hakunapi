package fi.nls.hakunapi.core.transformer;

import java.util.Arrays;

import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyTransformed;
import fi.nls.hakunapi.core.property.HakunaPropertyType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class BuildingFunctionTransformer implements ValueTransformer {

    @Override
    public void init(HakunaProperty property, String arg) {
        if (!(property instanceof HakunaPropertyTransformed)) {
            throw new IllegalArgumentException("Expected HakunaPropertyTransformed");
        }
        HakunaPropertyTransformed t = (HakunaPropertyTransformed) property;
        if (t.getInnerType() != HakunaPropertyType.STRING) {
            throw new IllegalArgumentException("Invalid type");
        }
    }

    @Override
    public HakunaPropertyType getPublicType() {
        return HakunaPropertyType.STRING;
    }

    @Override
    public Object toInner(String value) throws IllegalArgumentException {
        if (value == null) {
            return null;
        }
        int len = value.length();
        if (len == 0) {
            return value;
        }
        Integer code = parseInt(value);
        if (code != null) {
            // 1-19 tai -99
            if (code == -99 || (code >= 1 && code <= 19)) {
                // OK
                return value;
            } else {
                return HakunaProperty.UNKNOWN;
            }
        } else {
            // A - N, L "kielletty"
            // ?function=A => WHERE function LIKE A%
            // ?function=N => WHERE function LIKE N% OR function LIKE L%
            char c = value.charAt(0);
            if (value.length() > 1 || c == 'L' || c < 'A' || c > 'N') {
                return HakunaProperty.UNKNOWN; 
            } else if (value.charAt(0) == 'N') {
                return Arrays.asList("N%", "L%"); 
            } else {
                return value + "%";
            }
        }
    }
    
    private Integer parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Object toPublic(Object value) {
        if (value == null) {
            return null;
        }
        String s = value.toString();
        if ("-99".equals(s)) {
            return s;
        }

        char c = s.charAt(0);

        // L Palo- ja pelastustoimen rakennukset -> N Muut rakennukset
        if (c == 'L') {
            return "N";
        }

        int len = c == '0' || c == '1' ? 2 : 1;
        return s.substring(0, len);
    }

    @Override
    public Schema<?> getPublicSchema() {
        return new StringSchema();
    }

}
