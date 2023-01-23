package fi.nls.hakunapi.cql2.function;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import fi.nls.hakunapi.core.schemas.FunctionArgumentInfo.FunctionArgumentType;
import fi.nls.hakunapi.cql2.model.function.FunctionCall;
import fi.nls.hakunapi.cql2.model.literal.StringLiteral;

public class FunctionTableTest {

    @Test
    public void testFunction() {

        FunctionTableImpl functions = FunctionTable.of("poc",
                //
                Function.of("A", (func, call) -> {
                    return func.toString(call, "param");
                }).argument("param", FunctionArgumentType.string),
                //
                Function.of("B", (func, call) -> {
                    return func.toString(call, "param");
                }).argument("param", FunctionArgumentType.string)

        );

        FunctionCall callA = new FunctionCall("A", Arrays.asList(new StringLiteral("A")));

        assertTrue("A".equals(functions.getFunction("A").visit(callA)));

        FunctionCall callB = new FunctionCall("B", Arrays.asList(new StringLiteral("B")));

        assertTrue("B".equals(functions.getFunction("B").visit(callB)));

    }
}
