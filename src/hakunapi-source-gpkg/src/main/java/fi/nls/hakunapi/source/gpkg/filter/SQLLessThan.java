package fi.nls.hakunapi.source.gpkg.filter;

public class SQLLessThan extends SQLComparison {

    @Override
    public String getOp() {
        return "<";
    }

}
