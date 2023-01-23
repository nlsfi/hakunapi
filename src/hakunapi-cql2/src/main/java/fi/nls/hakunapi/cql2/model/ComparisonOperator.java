package fi.nls.hakunapi.cql2.model;

public enum ComparisonOperator {

    EQ("="),
    NEQ("<>"),
    LT("<"),
    LTE("<="),
    GT(">"),
    GTE(">=");

    public final String op;

    private ComparisonOperator(String op) {
        this.op = op;
    }

    public static ComparisonOperator from(String op) {
        switch (op) {
        case "=" : return EQ;
        case "<>": return NEQ;
        case "<" : return LT;
        case "<=": return LTE;
        case ">" : return GT;
        case ">=": return GTE;
        default:
            throw new IllegalArgumentException();
        }
    }

}
