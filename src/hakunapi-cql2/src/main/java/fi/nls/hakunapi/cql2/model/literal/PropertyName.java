package fi.nls.hakunapi.cql2.model.literal;

import fi.nls.hakunapi.cql2.model.Expression;

public class PropertyName implements Expression {

    private final String value;
    private final boolean casei;

    public PropertyName(String value, boolean casei) {
        this.value = value;
        this.casei = casei;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean isCasei() {
        return casei;
    }

}
