package fi.nls.hakunapi.cql2.model;

public class EmptyExpression implements Expression {

    public static final EmptyExpression INSTANCE = new EmptyExpression();

    private EmptyExpression() {
        // Block init
    }

}
