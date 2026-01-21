package fi.nls.hakunapi.cql2.function;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import fi.nls.hakunapi.core.schemas.FunctionArgumentInfo.FunctionArgumentType;

public class FunctionTableTest {

    @Test
    public void testFunction() {

        FunctionTableImpl functions = FunctionTable.of("poc",
                //
                Function.of("echo", (func, args, ctx) -> {
                    return func.getStringArg(args, "param");
                }).argument("param", FunctionArgumentType.string),
                //
                Function.of("numToString", (func, args, ctx) -> {
                    return func.getNumberArg(args, "arg1").toString();
                }).argument("arg1", FunctionArgumentType.number)

        );

        assertTrue("A".equals(functions.getFunction("echo").invoke(List.of("A"), null)));
        assertTrue("1337.0".equals(functions.getFunction("numToString").invoke(List.of(1337.0), null)));

    }
}
