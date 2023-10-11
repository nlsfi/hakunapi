package fi.nls.hakunapi.source.gpkg.filter;

public class SQLLessThanOrEqualTo extends SQLComparison {

    @Override
    public String getOp() {
        return "<=";
    }

}
