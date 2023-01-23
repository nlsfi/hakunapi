package fi.nls.hakunapi.simple.postgis.filter;

public class SQLGreaterThanOrEqualTo extends SQLComparison {

    @Override
    public String getOp() {
        return ">=";
    }

}
