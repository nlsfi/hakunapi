package fi.nls.hakunapi.sql.filter;

public class SQLNotEqualTo extends SQLComparison {

    @Override
    public String getOp() {
        return "<>";
    }

}
