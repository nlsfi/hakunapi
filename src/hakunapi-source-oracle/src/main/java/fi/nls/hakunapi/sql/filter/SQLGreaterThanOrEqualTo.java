package fi.nls.hakunapi.sql.filter;

public class SQLGreaterThanOrEqualTo extends SQLComparison {

    @Override
    public String getOp() {
        return ">=";
    }

}
