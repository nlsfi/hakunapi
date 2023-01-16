package fi.nls.hakunapi.simple.postgis.filter;

public class SQLLessThanOrEqualTo extends SQLComparison {

    @Override
    public String getOp() {
        return "<=";
    }

}
