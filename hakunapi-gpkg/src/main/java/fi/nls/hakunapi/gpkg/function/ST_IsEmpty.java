package fi.nls.hakunapi.gpkg.function;

import java.sql.SQLException;

import fi.nls.hakunapi.gpkg.GPKGGeometry;

public class ST_IsEmpty extends GPKGFunction {
    
    @Override
    public String getName() {
        return "ST_IsEmpty";
    }

    @Override
    protected void xFunc() throws SQLException {
        if (args() != 1) {
            throw new SQLException("Expected args() = 1");
        }
        byte[] geometry = value_blob(0);
        /* Check magic?
        if (geometry[0] != 'G' || geometry[1] != 'P') {
            throw new SQLException("Illegal magic number!");
        }
        */
        byte flags = geometry[3];
        result(GPKGGeometry.isEmpty(flags) ? 1 : 0);
    }

}
