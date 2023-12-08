package fi.nls.hakunapi.source.gpkg.filter;

public class SQLGreaterThanOrEqualTo extends SQLComparison {

    @Override
    public String getOp() {
        return ">=";
    }

}
