package fi.nls.hakunapi.source.gpkg.filter;

public class SQLGreaterThan extends SQLComparison {

    @Override
    public String getOp() {
        return ">";
    }

}
