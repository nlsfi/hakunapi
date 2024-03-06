package fi.nls.hakunapi.sql;

import java.util.List;

import javax.sql.DataSource;

import fi.nls.hakunapi.core.FeatureProducer;
import fi.nls.hakunapi.core.SimpleFeatureType;
import fi.nls.hakunapi.core.join.Join;

public abstract class SQLFeatureType extends SimpleFeatureType {

    protected String dbSchema;
    protected String primaryTable;
    protected List<Join> joins;
    protected DataSource ds;

    public String getDbSchema() {
        return dbSchema;
    }

    public void setDbSchema(String dbSchema) {
        this.dbSchema = dbSchema;
    }

    public String getPrimaryTable() {
        return primaryTable;
    }

    public void setPrimaryTable(String primaryTable) {
        this.primaryTable = primaryTable;
    }

    public List<Join> getJoins() {
        return joins;
    }

    public void setJoins(List<Join> joins) {
        this.joins = joins;
    }

    public DataSource getDatabase() {
        return ds;
    }

    public void setDatabase(DataSource ds) {
        this.ds = ds;
    }

    public abstract FeatureProducer getFeatureProducer() ;
}
