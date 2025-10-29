package fi.nls.hakunapi.cql2.function;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import fi.nls.hakunapi.core.schemas.FunctionInfo;
import fi.nls.hakunapi.core.schemas.FunctionsContent;

public class CQL2Functions {
    public static final CQL2Functions INSTANCE = new CQL2Functions();

    Map<String, FunctionTable> FUNCTION_TABLES = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public void register() {
        init(FunctionTableProvider.getFunctionTables());
    }

    public void init(List<FunctionTable> functionTables) {
        functionTables.forEach(ft -> {
            FUNCTION_TABLES.put(ft.getPackageName(), ft);
        });
    }

    public Map<String, FunctionTable> getFunctionTables() {
        return FUNCTION_TABLES;
    }

    public Optional<Function> getAnyFunction(String functionName) {
        return FUNCTION_TABLES.entrySet().stream().map(entry -> entry.getValue().getFunction(functionName))
                .filter(Objects::nonNull).findFirst();
    }

    public static FunctionInfo fromFunc(Function<?> func) {
        final FunctionInfo funcInfo = new FunctionInfo(func.getName(), func.getDescription(), func.getMetadataUrl());
        funcInfo.setArguments(func.getArguments());
        funcInfo.setReturns(func.getReturns());

        return funcInfo;
    }

    public FunctionsContent toFunctionsMetadata() {

        final List<Function> functions = new ArrayList<>();
        FUNCTION_TABLES.entrySet().stream().filter(e -> !e.getValue().isHidden()).forEach(e -> {
            e.getValue().getFunctions(functions);
        });

        final List<FunctionInfo> functionInfos = functions.stream().map(CQL2Functions::fromFunc)
                .collect(Collectors.toList());

        return new FunctionsContent(functionInfos);
    }

}
