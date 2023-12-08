package fi.nls.hakunapi.source.gpkg.filter;

public class SQLNotEqualTo extends SQLComparison {

    @Override
    public String getOp() {
        return "<>";
    }

}
