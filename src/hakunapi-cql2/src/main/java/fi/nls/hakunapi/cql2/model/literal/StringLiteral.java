package fi.nls.hakunapi.cql2.model.literal;

public class StringLiteral implements Literal {
    
    private final String value;
    
    public StringLiteral(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
