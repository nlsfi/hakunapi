package fi.nls.hakunapi.core.join;

public abstract class JoinBase implements Join {

    protected final String leftTable;
    protected final String leftColumn;
    protected final String rightTable;
    protected final String rightColumn;

    public JoinBase(String leftTable, String leftColumn, String rightTable, String rightColumn) {
        this.leftTable = leftTable;
        this.leftColumn = leftColumn;
        this.rightTable = rightTable;
        this.rightColumn = rightColumn;
    }

    public String getLeftTable() {
        return leftTable;
    }

    public String getLeftColumn() {
        return leftColumn;
    }

    public String getRightTable() {
        return rightTable;
    }

    public String getRightColumn() {
        return rightColumn;
    }

}
