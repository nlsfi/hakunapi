package fi.nls.hakunapi.simple.postgis.filter;

public class SQLEqualTo extends SQLComparison {

    @Override
    public String getOp() {
        return "=";
    }

}
