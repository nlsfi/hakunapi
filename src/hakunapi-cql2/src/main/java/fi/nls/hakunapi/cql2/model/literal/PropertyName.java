package fi.nls.hakunapi.cql2.model.literal;

import fi.nls.hakunapi.cql2.model.Expression;

public class PropertyName implements Expression {

    private final String value;

    public PropertyName(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
