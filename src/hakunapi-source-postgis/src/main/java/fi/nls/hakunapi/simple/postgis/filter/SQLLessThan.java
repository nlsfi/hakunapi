package fi.nls.hakunapi.simple.postgis.filter;

public class SQLLessThan extends SQLComparison {

    @Override
    public String getOp() {
        return "<";
    }

}
