package fi.nls.hakunapi.cql2.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Geometry;

import fi.nls.hakunapi.core.schemas.FunctionArgumentInfo;
import fi.nls.hakunapi.core.schemas.FunctionArgumentInfo.FunctionArgumentType;
import fi.nls.hakunapi.core.schemas.FunctionReturnsInfo;
import fi.nls.hakunapi.core.schemas.FunctionReturnsInfo.FunctionReturnsType;
import fi.nls.hakunapi.cql2.model.function.FunctionCall;
import fi.nls.hakunapi.cql2.model.literal.NumberLiteral;
import fi.nls.hakunapi.cql2.model.literal.StringLiteral;
import fi.nls.hakunapi.cql2.model.spatial.SpatialLiteral;

public class Function {

    @FunctionalInterface
    public interface FunctionImplementation {
        Object visit(Function func, FunctionCall call);
    }

    protected String name;
    protected String description;
    protected String metadataUrl;
    private final Map<String, Integer> argumentsPosName = new HashMap<>();
    private final List<FunctionArgumentInfo> arguments = new ArrayList<>();
    private FunctionReturnsInfo returns;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMetadataUrl() {
        return metadataUrl;
    }

    public void setMetadataUrl(String metadataUrl) {
        this.metadataUrl = metadataUrl;
    }

    public Function argument(String name, FunctionArgumentType type) {
        FunctionArgumentInfo funcArgInfo = new FunctionArgumentInfo(name, null, type);
        argumentsPosName.put(name, arguments.size());
        arguments.add(funcArgInfo);
        return this;
    }

    public Function returns(FunctionReturnsType type) {
        returns = new FunctionReturnsInfo(type);
        return this;
    }

    public List<FunctionArgumentInfo> getArguments() {
        return arguments;
    }

    public FunctionReturnsInfo getReturns() {
        return returns;
    }

    public Object visit(FunctionCall functionCall) {
        throw new RuntimeException("Missing function implementation");
    }

    protected Function(String name, String description, String metadataUrl) {
        super();
        setName(name);
        this.description = description;
        this.metadataUrl = metadataUrl;
    }

    public static Function of(String name) {
        return new Function(name, null, null);
    }

    public static Function of(String name, String description, String metadataUrl) {

        return new Function(name, description, metadataUrl);
    }

    public static Function of(String name, final FunctionImplementation impl) {

        return new Function(name, null, null) {
            public Object visit(FunctionCall call) {
                return impl.visit(this, call);
            }
        };
    }

    protected Geometry toGeometry(FunctionCall call, String name) {
        return ((SpatialLiteral) call.getArgs().get(argumentsPosName.get(name))).getGeometry();
    }

    protected Number toNumber(FunctionCall call, String name) {
        return ((NumberLiteral) call.getArgs().get(argumentsPosName.get(name))).getValue();
    }

    protected String toString(FunctionCall call, String name) {
        return ((StringLiteral) call.getArgs().get(argumentsPosName.get(name))).getValue();
    }

}
