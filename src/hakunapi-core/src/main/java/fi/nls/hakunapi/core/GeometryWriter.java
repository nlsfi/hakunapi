package fi.nls.hakunapi.core;

import fi.nls.hakunapi.core.geom.HakunaGeometryType;

public interface GeometryWriter {

    public void init(HakunaGeometryType type, int srid, int dimension) throws Exception;
    public void end() throws Exception;

    public void writeCoordinate(double x, double y) throws Exception;
    public void writeCoordinate(double x, double y, double z) throws Exception;
    public void writeCoordinate(double x, double y, double z, double m) throws Exception;

    public void startRing() throws Exception;
    public void endRing() throws Exception;

}
