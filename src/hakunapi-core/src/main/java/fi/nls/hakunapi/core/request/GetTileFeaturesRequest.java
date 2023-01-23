package fi.nls.hakunapi.core.request;

import fi.nls.hakunapi.core.util.SimplifyAlgorithm;

public class GetTileFeaturesRequest extends GetFeatureRequest {

    private int buffer;
    private int extent;
    private double tolerance;
    private SimplifyAlgorithm algorithm;

    public int getBuffer() {
        return buffer;
    }

    public void setBuffer(int buffer) {
        this.buffer = buffer;
    }

    public int getExtent() {
        return extent;
    }

    public void setExtent(int extent) {
        this.extent = extent;
    }

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    public SimplifyAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(SimplifyAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

}
