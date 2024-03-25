package fi.nls.hakunapi.simple.sdo.sql.filter;

public class SQLLessThan extends SQLComparison {

    @Override
    public String getOp() {
        return "<";
    }

}
