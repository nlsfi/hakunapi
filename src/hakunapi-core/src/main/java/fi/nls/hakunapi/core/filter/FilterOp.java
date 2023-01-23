package fi.nls.hakunapi.core.filter;

public enum FilterOp {

    PASS,
    DENY,

    EQUAL_TO,
    NOT_EQUAL_TO,
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL_TO,
    LESS_THAN,
    LESS_THAN_OR_EQUAL_TO,

    LIKE,
    NOT_LIKE,

    OR,
    AND,
    NOT,

    NULL,
    NOT_NULL,

    INTERSECTS_INDEX,
    INTERSECTS,
    EQUALS,
    DISJOINT,
    TOUCHES,
    WITHIN,
    OVERLAPS,
    CROSSES,
    CONTAINS,

    ARRAY_OVERLAPS,

}
