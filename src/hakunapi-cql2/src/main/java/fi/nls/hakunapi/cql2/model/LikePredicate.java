package fi.nls.hakunapi.cql2.model;

import fi.nls.hakunapi.cql2.model.literal.PropertyName;
import fi.nls.hakunapi.cql2.model.literal.StringLiteral;

public class LikePredicate implements Expression {

    private final PropertyName property;
    private final StringLiteral pattern;

    public LikePredicate(PropertyName property, StringLiteral pattern) {
        this.property = property;
        this.pattern = pattern;
    }

    public PropertyName getProperty() {
        return property;
    }

    public StringLiteral getPattern() {
        return pattern;
    }

}
