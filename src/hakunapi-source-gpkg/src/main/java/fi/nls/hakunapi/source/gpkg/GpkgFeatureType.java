package fi.nls.hakunapi.source.gpkg;

import javax.sql.DataSource;

import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.SimpleFeatureType;
import fi.nls.hakunapi.gpkg.extension.RtreeIndexExtension;

public class GpkgFeatureType extends SimpleFeatureType {

    private String table;
    private boolean spatialIndex;
    private DataSource ds;

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public boolean isSpatialIndex() {
        return spatialIndex;
    }

    public void setSpatialIndex(boolean spatialIndex) {
        this.spatialIndex = spatialIndex;
    }

    public DataSource getDatabase() {
        return ds;
    }

    public void setDatabase(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public FeatureProducer getFeatureProducer() {
        return new GpkgSimpleFeatureProducer();
    }

    public String getSpatialIndexName() {
        return RtreeIndexExtension.getRtreeName(table, getGeom().getColumn());
    }

}
