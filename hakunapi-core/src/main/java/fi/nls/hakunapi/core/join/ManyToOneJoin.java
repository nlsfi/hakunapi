package fi.nls.hakunapi.core.join;

public final class ManyToOneJoin extends JoinBase {

    public ManyToOneJoin(String leftTable, String leftColumn, String rightTable, String rightColumn) {
        super(leftTable, leftColumn, rightTable, rightColumn);
    }
    
    @Override
    public JoinType getJoinType() {
        return JoinType.ManyToOne;
    }

    @Override
    public Join swapOrder() {
        return new OneToManyJoin(rightTable, rightColumn, leftTable, leftColumn);
    }

}
