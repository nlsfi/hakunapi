package fi.nls.hakunapi.simple.sdo.sql.filter;

public class SQLLessThanOrEqualTo extends SQLComparison {

    @Override
    public String getOp() {
        return "<=";
    }

}
