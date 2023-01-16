package fi.nls.hakunapi.cql2.model.function;

import java.util.List;

import fi.nls.hakunapi.cql2.model.Expression;
import fi.nls.hakunapi.cql2.model.spatial.SpatialExpression;

public class FunctionCall implements SpatialExpression {

    private final String name;
    private final List<Expression> args;

    public FunctionCall(String name, List<Expression> args) {
        this.name = name;
        this.args = args;
    }

    public String getName() {
        return name;
    }

    public List<Expression> getArgs() {
        return args;
    }

}
