package fi.nls.hakunapi.source.gpkg.filter;

public class SQLEqualTo extends SQLComparison {

    @Override
    public String getOp() {
        return "=";
    }

}
