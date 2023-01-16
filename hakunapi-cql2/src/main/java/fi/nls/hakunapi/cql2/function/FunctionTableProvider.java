package fi.nls.hakunapi.cql2.function;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class FunctionTableProvider {

    private static final ServiceLoader<FunctionTableFactory> LOADER = ServiceLoader.load(FunctionTableFactory.class);

    public static List<FunctionTable> getFunctionTables() {
        LOADER.reload();

        final List<FunctionTable> functionTables = new ArrayList<>();

        for (FunctionTableFactory factory : LOADER) {
            functionTables.addAll(factory.createFunctionTables());
        }
        return functionTables;
    }

}
