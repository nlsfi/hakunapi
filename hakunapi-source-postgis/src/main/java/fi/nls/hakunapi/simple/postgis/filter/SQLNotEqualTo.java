package fi.nls.hakunapi.simple.postgis.filter;

public class SQLNotEqualTo extends SQLComparison {

    @Override
    public String getOp() {
        return "<>";
    }

}
