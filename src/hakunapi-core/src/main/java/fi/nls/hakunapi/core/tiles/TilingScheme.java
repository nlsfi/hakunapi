package fi.nls.hakunapi.core.tiles;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fi.nls.hakunapi.core.schemas.Component;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class TilingScheme implements Component {

    private String identifier;
    private String title;
    private String supportedCRS;
    private String wellKnownScaleSet;
    private BoundingBox boundingBox;
    private TileMatrix[] tileMatrices;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSupportedCRS() {
        return supportedCRS;
    }

    public void setSupportedCRS(String supportedCRS) {
        this.supportedCRS = supportedCRS;
    }

    public String getWellKnownScaleSet() {
        return wellKnownScaleSet;
    }

    public void setWellKnownScaleSet(String wellKnownScaleSet) {
        this.wellKnownScaleSet = wellKnownScaleSet;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public TileMatrix[] getTileMatrices() {
        return tileMatrices;
    }

    public void setTileMatrices(TileMatrix[] tileMatrices) {
        this.tileMatrices = tileMatrices;
    }

    @JsonIgnore
    @Override
    public String getComponentName() {
        return "tilingScheme";
    }

    @JsonIgnore
    @Override
    public Schema toSchema(Map<String, Schema> components) {
        if (!components.containsKey(getComponentName())) {
            Schema schema = new ObjectSchema()
                    .addProperties("identifier", new StringSchema())
                    .addProperties("title", new StringSchema())
                    .addProperties("boundingBox", new BoundingBox().toSchema(components))
                    .addProperties("supportedCRS", new StringSchema())
                    .addProperties("wellKnownScaleSet", new StringSchema())
                    .addProperties("tileMatrices", new ArraySchema().items(new TileMatrix().toSchema(components)))
                    .addRequiredItem("identifier")
                    .addRequiredItem("supportedCRS")
                    .addRequiredItem("tileMatrices");
            components.put(getComponentName(), schema);
        }
        return new Schema<>().$ref(getComponentName());
    }

}
