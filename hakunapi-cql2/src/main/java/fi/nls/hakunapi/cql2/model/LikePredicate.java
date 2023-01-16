package fi.nls.hakunapi.cql2.model;

import fi.nls.hakunapi.cql2.model.literal.PropertyName;

public class LikePredicate implements Expression {

    private final PropertyName property;
    private final String pattern;

    public LikePredicate(PropertyName property, String pattern) {
        this.property = property;
        this.pattern = pattern;
    }

    public PropertyName getProperty() {
        return property;
    }

    public String getPattern() {
        return pattern;
    }

}
