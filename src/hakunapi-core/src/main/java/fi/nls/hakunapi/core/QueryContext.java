package fi.nls.hakunapi.core;

public class QueryContext {

    private int srid;
    private boolean sourceShouldProjectToSrid;

    public int getSRID() {
        return srid;
    }

    public void setSRID(int srid) {
        this.srid = srid;
    }

    public boolean isSourceShouldProjectToSrid() {
        return sourceShouldProjectToSrid;
    }

    public void setSourceShouldProjectToSrid(boolean sourceShouldProjectToSrid) {
        this.sourceShouldProjectToSrid = sourceShouldProjectToSrid;
    }

}
