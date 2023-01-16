package fi.nls.hakunapi.core.tiles;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class BoundingBox {

    private double[] lowerCorner;
    private double[] upperCorner;
    private String crs;

    public double[] getLowerCorner() {
        return lowerCorner;
    }

    public void setLowerCorner(double[] lowerCorner) {
        this.lowerCorner = lowerCorner;
    }

    public double[] getUpperCorner() {
        return upperCorner;
    }

    public void setUpperCorner(double[] upperCorner) {
        this.upperCorner = upperCorner;
    }

    public String getCrs() {
        return crs;
    }

    public void setCrs(String crs) {
        this.crs = crs;
    }

    @JsonIgnore
    public Schema toSchema(Map<String, Schema> components) {
        return new ObjectSchema()
                .addProperties("lowerCorner", new ArraySchema().items(new NumberSchema()).minItems(2).maxItems(2))
                .addProperties("upperCorner", new ArraySchema().items(new NumberSchema()).minItems(2).maxItems(2))
                .addProperties("crs", new StringSchema());
    }
}
