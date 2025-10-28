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

public class Function {

    @FunctionalInterface
    public interface FunctionImplementation {
        Object invoke(Function func, List<Object> args, Object context);
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

    public Object invoke(List<Object> args, Object context) {
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
            public Object invoke(List<Object> args, Object context) {
                return impl.invoke(this, args, context);
            }
        };
    }

    protected Geometry getGeometryArg(List<Object> args, String name) {
        int i = argumentsPosName.get(name);
        return (Geometry) args.get(i);
    }

    protected Number getNumberArg(List<Object> args, String name) {
        int i = argumentsPosName.get(name);
        return (Number) args.get(i);
    }

    protected String getStringArg(List<Object> args, String name) {
        int i = argumentsPosName.get(name);
        return args.get(i).toString();
    }

}
