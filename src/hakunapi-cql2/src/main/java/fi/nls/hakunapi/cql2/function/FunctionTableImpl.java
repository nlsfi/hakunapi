package fi.nls.hakunapi.cql2.function;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FunctionTableImpl implements FunctionTable {

    final String packageName;
    final Map<String, Function> functions = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    boolean hidden = false;

    @Override
    public void getFunctions(List<Function> list) {
        list.addAll(functions.values());
    }

    @Override
    // [<packageName>_]<functionName>
    public Function getFunction(String functionName) {
        return functions.get(functionName);
    }

    public void putFunction(Function func) {
        functions.put(func.getName(), func);
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public String getPackageName() {
        return packageName;
    }

    public FunctionTableImpl(String packageName, boolean hidden) {
        super();
        this.packageName = packageName;
        this.hidden = hidden;
    }

    public FunctionTableImpl(String packageName) {
        super();
        this.packageName = packageName;
        this.hidden = false;
    }

}
