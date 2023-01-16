package fi.nls.hakunapi.simple.postgis.filter;

public class SQLGreaterThan extends SQLComparison {

    @Override
    public String getOp() {
        return ">";
    }

}
