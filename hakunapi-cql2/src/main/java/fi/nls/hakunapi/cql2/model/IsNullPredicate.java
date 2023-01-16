package fi.nls.hakunapi.cql2.model;

import fi.nls.hakunapi.cql2.model.literal.PropertyName;

public class IsNullPredicate implements Expression {

    private final PropertyName property;

    public IsNullPredicate(PropertyName property) {
        this.property = property;
    }

    public PropertyName getProperty() {
        return property;
    }

}
