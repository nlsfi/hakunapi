package fi.nls.hakunapi.sql.filter;

public class SQLLessThanOrEqualTo extends SQLComparison {

    @Override
    public String getOp() {
        return "<=";
    }

}
