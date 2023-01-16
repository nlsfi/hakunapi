package fi.nls.hakunapi.cql2.model;

import fi.nls.hakunapi.cql2.model.literal.PropertyName;

public class BinaryComparisonPredicate implements ComparisonPredicate {

    private final PropertyName prop;
    private final ComparisonOperator op;
    private final Expression value;

    public BinaryComparisonPredicate(PropertyName prop, ComparisonOperator op, Expression value) {
        this.prop = prop;
        this.op = op;
        this.value = value;
    }

    public PropertyName getProp() {
        return prop;
    }

    public ComparisonOperator getOp() {
        return op;
    }

    public Expression getValue() {
        return value;
    }

}
