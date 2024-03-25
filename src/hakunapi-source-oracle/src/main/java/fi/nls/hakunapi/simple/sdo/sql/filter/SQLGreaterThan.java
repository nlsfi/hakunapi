package fi.nls.hakunapi.simple.sdo.sql.filter;

public class SQLGreaterThan extends SQLComparison {

    @Override
    public String getOp() {
        return ">";
    }

}
