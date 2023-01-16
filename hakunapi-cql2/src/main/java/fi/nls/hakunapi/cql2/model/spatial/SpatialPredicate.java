package fi.nls.hakunapi.cql2.model.spatial;

import fi.nls.hakunapi.cql2.model.Expression;
import fi.nls.hakunapi.cql2.model.literal.PropertyName;

public class SpatialPredicate implements Expression {

    private final PropertyName prop;
    private final SpatialOperator op;
    private final SpatialExpression value;

    public SpatialPredicate(PropertyName prop, SpatialOperator op, SpatialExpression value) {
        this.prop = prop;
        this.op = op;
        this.value = value;
    }

    public PropertyName getProp() {
        return prop;
    }

    public SpatialOperator getOp() {
        return op;
    }

    public SpatialExpression getValue() {
        return value;
    }

}
