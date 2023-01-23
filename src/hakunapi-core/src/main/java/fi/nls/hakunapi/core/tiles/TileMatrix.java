package fi.nls.hakunapi.core.tiles;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fi.nls.hakunapi.core.schemas.Component;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class TileMatrix implements Component {

    private String identifier;
    private double scaleDenominator;
    private int tileWidth;
    private int tileHeight;
    private int matrixWidth;
    private int matrixHeight;
    private double[] topLeftCorner;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public double getScaleDenominator() {
        return scaleDenominator;
    }

    public void setScaleDenominator(double scaleDenominator) {
        this.scaleDenominator = scaleDenominator;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public void setTileWidth(int tileWidth) {
        this.tileWidth = tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
    }

    public int getMatrixWidth() {
        return matrixWidth;
    }

    public void setMatrixWidth(int matrixWidth) {
        this.matrixWidth = matrixWidth;
    }

    public int getMatrixHeight() {
        return matrixHeight;
    }

    public void setMatrixHeight(int matrixHeight) {
        this.matrixHeight = matrixHeight;
    }

    public double[] getTopLeftCorner() {
        return topLeftCorner;
    }

    public void setTopLeftCorner(double[] topLeftCorner) {
        this.topLeftCorner = topLeftCorner;
    }
    
    @Override
    @JsonIgnore
    public String getComponentName() {
        return "tileMatrix";
    }

    @Override
    @JsonIgnore
    public Schema toSchema(Map<String, Schema> components) {
        if (!components.containsKey(getComponentName())) {
            Schema schema = new ObjectSchema()
                    .addProperties("identifier", new StringSchema())
                    .addProperties("scaleDenominator", new NumberSchema())
                    .addProperties("tileWidth", new IntegerSchema())
                    .addProperties("tileHeight", new IntegerSchema())
                    .addProperties("matrixWidth", new IntegerSchema())
                    .addProperties("matrixHeight", new IntegerSchema())
                    .addProperties("topLeftCorner", new ArraySchema().items(new NumberSchema()).minItems(2).maxItems(2));
            components.put(getComponentName(), schema);
        }
        return new Schema<>().$ref(getComponentName());
    }

}
