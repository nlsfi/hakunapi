package fi.nls.hakunapi.core.join;

public interface Join {
    
    public JoinType getJoinType();
    public String getLeftTable();
    public String getLeftColumn();
    public String getRightTable();
    public String getRightColumn();
    public Join swapOrder();

}
