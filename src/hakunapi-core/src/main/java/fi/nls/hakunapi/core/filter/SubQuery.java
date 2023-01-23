package fi.nls.hakunapi.core.filter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubQuery {
    
    public enum Comparison {
        IS, IN 
    }

    private final Comparison predicate; 
    private final List<String> columns;
    private final String table;
    private Filter filter;
    private Set<String> groupBy;

    @Deprecated
    public SubQuery(String column, String table) {
        this(Comparison.IN, column, table);
    }
    
    public SubQuery(Comparison predicate, String column, String table) {
        this(predicate, Arrays.asList(column), table);
    }

    public SubQuery(Comparison predicate, List<String> columns, String table) {
        this.predicate = predicate;
        this.columns = columns;
        this.table = table;
    }
    
    public Comparison getPredicate() {
        return predicate;
    }

    public List<String> getColumns() {
        return columns;
    }

    public String getTable() {
        return table;
    }
    
    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Set<String> getGroupBy() {
        return groupBy;
    }

    public void doGroupBy(String... column) {
        if (groupBy == null) {
            groupBy = new HashSet<>();
        }
        for (String c : column) {
            groupBy.add(c);
        }
    }
  

}
