package fi.nls.hakunapi.tiles;

import java.util.List;
import java.util.Optional;

/** OGC 17-083r4 tile matrix set. */
public class TileMatrixSet {

    private final String id;
    private final String title;
    /** CRS as a URI or EPSG code reference, e.g. http://www.opengis.net/def/crs/EPSG/0/3857 */
    private final String crs;
    private final int srid;
    private final List<TileMatrix> tileMatrices;

    public TileMatrixSet(String id, String title, String crs, int srid, List<TileMatrix> tileMatrices) {
        this.id = id;
        this.title = title;
        this.crs = crs;
        this.srid = srid;
        this.tileMatrices = List.copyOf(tileMatrices);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCrs() {
        return crs;
    }

    public int getSrid() {
        return srid;
    }

    public List<TileMatrix> getTileMatrices() {
        return tileMatrices;
    }

    public Optional<TileMatrix> getTileMatrix(String tileMatrixId) {
        return tileMatrices.stream().filter(tm -> tm.getId().equals(tileMatrixId)).findAny();
    }

}
