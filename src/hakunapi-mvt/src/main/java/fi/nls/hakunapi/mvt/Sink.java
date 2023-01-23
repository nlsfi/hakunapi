package fi.nls.hakunapi.mvt;

public interface Sink {

    public void add(double x, double y);
    public void closePolygon();

}
