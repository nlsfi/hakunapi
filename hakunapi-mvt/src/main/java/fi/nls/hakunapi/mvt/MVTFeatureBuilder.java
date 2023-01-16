package fi.nls.hakunapi.mvt;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;

import com.wdtinc.mapbox_vector_tile.VectorTile;
import com.wdtinc.mapbox_vector_tile.VectorTile.Tile.Feature.Builder;
import com.wdtinc.mapbox_vector_tile.adapt.jts.IUserDataConverter;
import com.wdtinc.mapbox_vector_tile.build.MvtLayerProps;

public class MVTFeatureBuilder implements IUserDataConverter {

    private MvtLayerProps layerProps;
    private Long id;
    private Geometry geometry;
    private List<String> keys = new ArrayList<>();
    private List<Object> values = new ArrayList<>();

    public MVTFeatureBuilder(MvtLayerProps layerProps) {
        this.layerProps = layerProps;
    }

    private void reset() {
        this.id = null;
        this.geometry = null;
        this.keys.clear();
        this.values.clear();
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public void addProperty(String key, Object value) {
        if (value == null) {
            return;
        }
        keys.add(key);
        values.add(value);
    }

    public List<String> getKeys() {
        return keys;
    }

    public List<Object> getValues() {
        return values;
    }

    public VectorTile.Tile.Feature toFeature() {
        try {
            if (geometry == null) {
                return null;
            }
            List<VectorTile.Tile.Feature> features = JtsAdapter.toFeatures(geometry, layerProps, this);
            if (features.isEmpty()) {
                return null;
            }
            // Trust that geometry is never a GeometryCollection
            return features.get(0);
        } finally {
            reset();
        }
    }

    @Override
    public void addTags(Object userData, MvtLayerProps layerProps, Builder featureBuilder) {
        if (id != null) {
            featureBuilder.setId(id);
        }
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            Object value = values.get(i);
            int valueIndex = layerProps.addValue(value);
            if (valueIndex < 0) {
                continue;
            }
            int keyIndex = layerProps.addKey(key);
            featureBuilder.addTags(keyIndex);
            featureBuilder.addTags(valueIndex);
        }
    }

}
