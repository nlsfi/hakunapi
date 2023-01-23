package fi.nls.hakunapi.gpkg.function;

import java.sql.Connection;
import java.sql.SQLException;

import org.sqlite.Function;

public abstract class GPKGFunction extends Function {

    public abstract String getName();
    
    public void create(Connection c) throws SQLException {
        Function.create(c, getName(), this, 1, Function.FLAG_DETERMINISTIC);
    }

}
