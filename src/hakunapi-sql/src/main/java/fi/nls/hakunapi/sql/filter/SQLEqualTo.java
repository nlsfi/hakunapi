package fi.nls.hakunapi.sql.filter;

public class SQLEqualTo extends SQLComparison {

    @Override
    public String getOp() {
        return "=";
    }

}
