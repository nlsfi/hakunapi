package fi.nls.hakunapi.simple.sdo.sql.filter;

public class SQLEqualTo extends SQLComparison {

    @Override
    public String getOp() {
        return "=";
    }

}
