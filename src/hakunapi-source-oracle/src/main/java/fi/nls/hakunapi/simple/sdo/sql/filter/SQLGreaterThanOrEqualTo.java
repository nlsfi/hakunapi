package fi.nls.hakunapi.simple.sdo.sql.filter;

public class SQLGreaterThanOrEqualTo extends SQLComparison {

    @Override
    public String getOp() {
        return ">=";
    }

}
