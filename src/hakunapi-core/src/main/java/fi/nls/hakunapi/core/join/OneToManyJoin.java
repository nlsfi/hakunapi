package fi.nls.hakunapi.core.join;

public final class OneToManyJoin extends JoinBase {

    public OneToManyJoin(String leftTable, String leftColumn, String rightTable, String rightColumn) {
        super(leftTable, leftColumn, rightTable, rightColumn);
    }
    
    @Override
    public JoinType getJoinType() {
        return JoinType.OneToMany;
    }

    @Override
    public Join swapOrder() {
        return new ManyToOneJoin(rightTable, rightColumn, leftTable, leftColumn);
    }

}
