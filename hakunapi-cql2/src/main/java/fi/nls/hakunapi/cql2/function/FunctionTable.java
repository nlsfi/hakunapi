package fi.nls.hakunapi.cql2.function;

import java.util.List;
import java.util.stream.Stream;

public interface FunctionTable {

    public String getPackageName();

    // [<packageName>_]<functionName>
    public Function getFunction(String functionName);

    public void getFunctions(List<Function> list);

    public boolean isHidden();

    public static FunctionTableImpl of(String packageName, Function... functions) {

        final FunctionTableImpl impl = new FunctionTableImpl(packageName);
        Stream.of(functions).forEach(f -> impl.putFunction(f));
        return impl;

    }
}
