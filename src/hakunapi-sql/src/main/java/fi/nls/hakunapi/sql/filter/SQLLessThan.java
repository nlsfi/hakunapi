package fi.nls.hakunapi.sql.filter;

public class SQLLessThan extends SQLComparison {

    @Override
    public String getOp() {
        return "<";
    }

}
