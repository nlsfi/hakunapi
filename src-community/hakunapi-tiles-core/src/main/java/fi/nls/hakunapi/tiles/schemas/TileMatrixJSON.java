package fi.nls.hakunapi.tiles.schemas;

import java.util.List;

import fi.nls.hakunapi.tiles.TileMatrix;

public class TileMatrixJSON {

    private final TileMatrix tm;

    public TileMatrixJSON(TileMatrix tm) {
        this.tm = tm;
    }

    public String getId() {
        return tm.getId();
    }

    public double getScaleDenominator() {
        return tm.getScaleDenominator();
    }

    public double getCellSize() {
        return tm.getCellSize();
    }

    public List<Double> getPointOfOrigin() {
        return List.of(tm.getPointOfOriginX(), tm.getPointOfOriginY());
    }

    public int getTileWidth() {
        return tm.getTileWidth();
    }

    public int getTileHeight() {
        return tm.getTileHeight();
    }

    public int getMatrixWidth() {
        return tm.getMatrixWidth();
    }

    public int getMatrixHeight() {
        return tm.getMatrixHeight();
    }

}
