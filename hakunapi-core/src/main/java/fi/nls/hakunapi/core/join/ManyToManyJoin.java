package fi.nls.hakunapi.core.join;

public final class ManyToManyJoin extends JoinBase {

    private final String joinTable;
    private final String joinColumnLeft;
    private final String joinColumnRight;

    public ManyToManyJoin(String leftTable, String leftColumn, String rightTable, String rightColumn, String joinTable, String joinColumnLeft, String joinColumnRight) {
        super(leftTable, leftColumn, rightTable, rightColumn);
        this.joinTable = joinTable;
        this.joinColumnLeft = joinColumnLeft;
        this.joinColumnRight = joinColumnRight;
    }

    public String getJoinTable() {
        return joinTable;
    }

    public String getJoinColumnLeft() {
        return joinColumnLeft;
    }

    public String getJoinColumnRight() {
        return joinColumnRight;
    }

    @Override
    public JoinType getJoinType() {
        return JoinType.ManyToMany;
    }

    @Override
    public Join swapOrder() {
        return new ManyToManyJoin(rightTable, rightColumn, leftTable, leftColumn, joinTable, joinColumnRight, joinColumnLeft);
    }

    /*
    public Filter createFilter(Collection<T1> parentItems) {
     * WHERE child.fk IN (
     *   SELECT inverseJoinColumn
     *   FROM joinTable
     *   WHERE joinColumn IN (parent.fk))
     * )

        String joinColumn = joinTable.getJoinColumn();
        HakunaPropertyType fkType = joinTable.getParentProperty().getDynamicProperty().getInnerType();
        HakunaPropertyDynamic joinColumnProp = new HakunaPropertyDynamic(joinColumn, joinColumn, fkType);

        List<Filter> ors = parentItems.stream()
                .map(item -> referencedProperty.getGetter().apply(item))
                .filter(key -> key != null)
                .map(key -> key.toString())
                .distinct()
                .map(key -> Filter.equalTo(joinColumnProp, key))
                .collect(Collectors.toList());
        Filter joinColumnIn = Filter.or(ors);

        HakunaPropertyDynamic childProp = joinTable.getChildProperty().getDynamicProperty();
        SubQuery subQuery = new SubQuery(Comparison.IN, joinTable.getInverseJoinColumn(), joinTable.getTable());
        subQuery.setFilter(joinColumnIn);
        return Filter.subQuery(childProp, subQuery);
    }
     */

}
