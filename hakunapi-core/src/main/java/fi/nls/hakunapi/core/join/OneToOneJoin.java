package fi.nls.hakunapi.core.join;

public final class OneToOneJoin extends JoinBase {

    public OneToOneJoin(String leftTable, String leftColumn, String rightTable, String rightColumn) {
        super(leftTable, leftColumn, rightTable, rightColumn);
    }

    @Override
    public JoinType getJoinType() {
        return JoinType.OneToOne;
    }
    
    public Join swapOrder() {
        return new OneToOneJoin(rightTable, rightColumn, leftTable, leftColumn);
    }

}
