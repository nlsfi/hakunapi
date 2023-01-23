package fi.nls.hakunapi.cql2.function.geometry;

import java.util.Arrays;
import java.util.List;

import fi.nls.hakunapi.cql2.function.FunctionTable;
import fi.nls.hakunapi.cql2.function.FunctionTableFactory;

public class GeometryFunctionsFactory implements FunctionTableFactory {

    @Override
    public List<FunctionTable> createFunctionTables() {

        return Arrays.asList(FunctionTable.of("geometry_editors",
                //
                new ST_Buffer(),
                //
                new Buffer()
        //
        ));
    }

}
