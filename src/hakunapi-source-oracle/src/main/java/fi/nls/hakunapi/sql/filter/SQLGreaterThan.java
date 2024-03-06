package fi.nls.hakunapi.sql.filter;

public class SQLGreaterThan extends SQLComparison {

    @Override
    public String getOp() {
        return ">";
    }

}
