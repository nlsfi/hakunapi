package fi.nls.hakunapi.simple.sdo.sql.filter;

public class SQLNotEqualTo extends SQLComparison {

    @Override
    public String getOp() {
        return "<>";
    }

}
