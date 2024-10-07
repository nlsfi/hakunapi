package fi.nls.hakunapi.core.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;

import fi.nls.hakunapi.core.property.HakunaProperty;
import fi.nls.hakunapi.core.property.HakunaPropertyArray;
import fi.nls.hakunapi.core.property.HakunaPropertyComposite;
import fi.nls.hakunapi.core.property.simple.HakunaPropertyGeometry;

public class Filter {

    public static final Filter PASS = new Filter(FilterOp.PASS, null, null);
    public static final Filter DENY = new Filter(FilterOp.DENY, null, null);

    private final FilterOp op;
    private final HakunaProperty prop;
    private final Object value;
    private final boolean caseInsensitive;
    private String tag;

    public Filter(FilterOp op, HakunaProperty prop, Object value) {
        this(op, prop, value, false);
    }

    public Filter(FilterOp op, HakunaProperty prop, Object value, boolean caseInsensitive) {
        this.op = op;
        this.prop = prop;
        this.value = value;
        this.caseInsensitive = caseInsensitive;
    }

    public Filter negate() {
        switch (op) {
        case PASS:
            return DENY;
        case DENY:
            return PASS;
        case EQUAL_TO:
            return new Filter(FilterOp.NOT_EQUAL_TO, prop, value, caseInsensitive);
        case NOT_EQUAL_TO:
            return new Filter(FilterOp.EQUAL_TO, prop, value, caseInsensitive);
        case GREATER_THAN:
            return new Filter(FilterOp.LESS_THAN_OR_EQUAL_TO, prop, value);
        case GREATER_THAN_OR_EQUAL_TO:
            return new Filter(FilterOp.LESS_THAN, prop, value);
        case LESS_THAN:
            return new Filter(FilterOp.GREATER_THAN_OR_EQUAL_TO, prop, value);
        case LESS_THAN_OR_EQUAL_TO:
            return new Filter(FilterOp.GREATER_THAN, prop, value);
        case LIKE:
            return new Filter(FilterOp.NOT_LIKE, prop, value, caseInsensitive);
        case NOT_LIKE:
            return new Filter(FilterOp.LIKE, prop, value, caseInsensitive);
        case OR:
            return and(((List<Filter>) value).stream().map(Filter::negate).collect(Collectors.toList()));
        case AND:
            return or(((List<Filter>) value).stream().map(Filter::negate).collect(Collectors.toList()));
        case NULL:
            return new Filter(FilterOp.NOT_NULL, prop, value);
        case NOT_NULL:
            return new Filter(FilterOp.NULL, prop, value);
        case NOT:
            return (Filter) this.value;
        case INTERSECTS:
            return new Filter(FilterOp.DISJOINT, prop, value);
        case DISJOINT:
            return new Filter(FilterOp.INTERSECTS, prop, value);
        default:
            return new Filter(FilterOp.NOT, null, this);
        }
    }

    public Filter inverse() {
        switch (op) {
        case GREATER_THAN:
            return new Filter(FilterOp.LESS_THAN, prop, value);
        case GREATER_THAN_OR_EQUAL_TO:
            return new Filter(FilterOp.LESS_THAN_OR_EQUAL_TO, prop, value);
        case LESS_THAN:
            return new Filter(FilterOp.GREATER_THAN, prop, value);
        case LESS_THAN_OR_EQUAL_TO:
            return new Filter(FilterOp.GREATER_THAN_OR_EQUAL_TO, prop, value);
        case WITHIN: // ST_Contains is the inverse of ST_Within. So ST_Contains(A,B) implies ST_Within(B,A)
            return new Filter(FilterOp.CONTAINS, prop, value);
        case CONTAINS: // @see https://postgis.net/docs/ST_Contains.html
            return new Filter(FilterOp.WITHIN, prop, value);
        case PASS:
        case DENY:
        case LIKE:
        case NOT_LIKE:
        case EQUAL_TO:
        case NOT_EQUAL_TO:
        case OR:
        case AND:
        case NULL:
        case NOT_NULL:
        case INTERSECTS_INDEX:
        case INTERSECTS:
        case EQUALS:
        case DISJOINT:
        case TOUCHES:
        case OVERLAPS:
        case CROSSES:
        case ARRAY_OVERLAPS:
            return this;
        default:
            throw new IllegalArgumentException("Can not inverse " + op.name());
        }
    }

    public FilterOp getOp() {
        return op;
    }

    public HakunaProperty getProp() {
        return prop;
    }

    public Object getValue() {
        return value;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    public String getTag() {
        return tag;
    }

    public Filter setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public static Filter equalTo(HakunaProperty prop, String value) {
        return equalTo(prop, value, false);
    }

    public static Filter equalTo(HakunaProperty prop, String value, boolean caseInsensitive) {
        if (prop instanceof HakunaPropertyComposite) {
            return ((HakunaPropertyComposite) prop).toFilter(FilterOp.EQUAL_TO, value, caseInsensitive);
        }
        Object v = prop.toInner(value);
        if (v == null) {
            return isNull(prop);
        } else if (v == HakunaProperty.UNKNOWN) {
            return DENY;
        } else if (v instanceof Collection) {
            return collectionToFilter(prop, (Collection<Object>) v, it -> new Filter(FilterOp.EQUAL_TO, prop, it, caseInsensitive));
        } else {
            return new Filter(FilterOp.EQUAL_TO, prop, v, caseInsensitive);
        }
    }
    
    private static Filter collectionToFilter(HakunaProperty prop, Collection<Object> value, Function<Object, Filter> mapper) {
        List<Filter> or = new ArrayList<>();
        boolean anyNull = false;
        for (Object o : value) {
            if (o == null) {
                anyNull = true;
            } else {
                or.add(mapper.apply(o));
            }
        }
        if (anyNull) {
            or.add(isNull(prop));
        }
        return Filter.or(or);
    }

    public static Filter notEqualTo(HakunaProperty prop, String value) {
        return notEqualTo(prop, value, false);
    }

    public static Filter notEqualTo(HakunaProperty prop, String value, boolean caseInsensitive) {
        if (prop instanceof HakunaPropertyComposite) {
            return ((HakunaPropertyComposite) prop).toFilter(FilterOp.NOT_EQUAL_TO, value, caseInsensitive);
        }
        Object v = prop.toInner(value);
        if (v == null) {
            return isNotNull(prop);
        } else if (v == HakunaProperty.UNKNOWN) {
            return PASS;
        } else {
            return new Filter(FilterOp.NOT_EQUAL_TO, prop, v, caseInsensitive);
        }
    }

    public static Filter greaterThan(HakunaProperty prop, String value) {
        if (prop instanceof HakunaPropertyComposite) {
            return ((HakunaPropertyComposite) prop).toFilter(FilterOp.GREATER_THAN, value);
        }
        Object v = prop.toInner(value);
        if (v == null) {
            return Filter.DENY;
        } else if (v == HakunaProperty.UNKNOWN) {
            return Filter.DENY;
        } else if (v instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<? extends Comparable<Object>> c = (Collection<? extends Comparable<Object>>) v;
            v = c.stream()
                    .filter(Objects::nonNull)
                    .min(Comparable::compareTo)
                    .orElseThrow(() -> new RuntimeException("Failed to create filter"));
        }
        return new Filter(FilterOp.GREATER_THAN, prop, v);
    }

    public static Filter greaterThanOrEqualTo(HakunaProperty prop, String value) {
        if (prop instanceof HakunaPropertyComposite) {
            return ((HakunaPropertyComposite) prop).toFilter(FilterOp.GREATER_THAN_OR_EQUAL_TO, value);
        }
        Object v = prop.toInner(value);
        if (v == null) {
            return Filter.DENY;
        } else if (v == HakunaProperty.UNKNOWN) {
            return Filter.DENY;
        } else if (v instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<? extends Comparable<Object>> c = (Collection<? extends Comparable<Object>>) v;
            v = c.stream()
                    .filter(Objects::nonNull)
                    .min(Comparable::compareTo)
                    .orElseThrow(() -> new RuntimeException("Failed to create filter"));
        }
        return new Filter(FilterOp.GREATER_THAN_OR_EQUAL_TO, prop, v);
    }

    public static Filter lessThan(HakunaProperty prop, String value) {
        if (prop instanceof HakunaPropertyComposite) {
            return ((HakunaPropertyComposite) prop).toFilter(FilterOp.LESS_THAN, value);
        }
        Object v = prop.toInner(value);
        if (v == null) {
            return Filter.DENY;
        } else if (v == HakunaProperty.UNKNOWN) {
            return Filter.DENY;
        } else if (v instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<? extends Comparable<Object>> c = (Collection<? extends Comparable<Object>>) v;
            v = c.stream()
                    .filter(Objects::nonNull)
                    .max(Comparable::compareTo)
                    .orElseThrow(() -> new RuntimeException("Failed to create filter"));
        }
        return new Filter(FilterOp.LESS_THAN, prop, v);
    }

    public static Filter lessThanThanOrEqualTo(HakunaProperty prop, String value) {
        if (prop instanceof HakunaPropertyComposite) {
            return ((HakunaPropertyComposite) prop).toFilter(FilterOp.LESS_THAN_OR_EQUAL_TO, value);
        }
        Object v = prop.toInner(value);
        if (v == null) {
            return Filter.DENY;
        } else if (v == HakunaProperty.UNKNOWN) {
            return Filter.DENY;
        } else if (v instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<? extends Comparable<Object>> c = (Collection<? extends Comparable<Object>>) v;
            v = c.stream()
                    .filter(Objects::nonNull)
                    .max(Comparable::compareTo)
                    .orElseThrow(() -> new RuntimeException("Failed to create filter"));
        }
        return new Filter(FilterOp.LESS_THAN_OR_EQUAL_TO, prop, v);
    }

    public static Filter like(HakunaProperty prop, String value, char wild, char single, char escape, boolean caseInsensitive) {
        Object v = prop.toInner(value);
        if (v == null) {
            return Filter.DENY;
        } else if (v == HakunaProperty.UNKNOWN) {
            return Filter.DENY;
        } else if (v instanceof Collection) {
            return collectionToFilter(prop, (Collection<Object>) v,
                    it -> new LikeFilter(FilterOp.LIKE, prop, it.toString(), wild, single, escape, caseInsensitive));
        } else {
            return new LikeFilter(FilterOp.LIKE, prop, v.toString(), wild, single, escape, caseInsensitive);
        }
    }

    public static Filter intersects(HakunaPropertyGeometry prop, Geometry geom) {
        return new Filter(FilterOp.INTERSECTS, prop, geom);
    }

    public static Filter intersectsIndex(HakunaPropertyGeometry prop, Geometry geom) {
        return new Filter(FilterOp.INTERSECTS_INDEX, prop, geom);
    }

    public static Filter equals(HakunaPropertyGeometry prop, Geometry geom) {
        return new Filter(FilterOp.EQUALS, prop, geom);
    }

    public static Filter disjoint(HakunaPropertyGeometry prop, Geometry geom) {
        return new Filter(FilterOp.DISJOINT, prop, geom);
    }

    public static Filter touches(HakunaPropertyGeometry prop, Geometry geom) {
        return new Filter(FilterOp.TOUCHES, prop, geom);
    }

    public static Filter within(HakunaPropertyGeometry prop, Geometry geom) {
        return new Filter(FilterOp.WITHIN, prop, geom);
    }

    public static Filter overlaps(HakunaPropertyGeometry prop, Geometry geom) {
        return new Filter(FilterOp.OVERLAPS, prop, geom);
    }

    public static Filter crosses(HakunaPropertyGeometry prop, Geometry geom) {
        return new Filter(FilterOp.CROSSES, prop, geom);
    }

    public static Filter contains(HakunaPropertyGeometry prop, Geometry geom) {
        return new Filter(FilterOp.CONTAINS, prop, geom);
    }

    public static Filter arrayOverlaps(HakunaPropertyArray prop, List<Object> arr) {
        return new Filter(FilterOp.ARRAY_OVERLAPS, prop, arr);
    }

    public static Filter or(List<Filter> subFilters) {
        return or(subFilters, null);
    }

    public static Filter or(List<Filter> subFilters, String tag) {
        if (subFilters == null || subFilters.isEmpty()) {
            return null;
        }
        if (subFilters.size() == 1) {
            return subFilters.get(0).setTag(tag);
        }
        if (subFilters.stream().anyMatch(f -> f == PASS)) {
            return PASS;
        }
        return new Filter(FilterOp.OR, null, subFilters).setTag(tag);
    }

    @SuppressWarnings("unchecked")
    public static Filter or(Filter a, Filter b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        if (a == PASS || b == PASS) {
            return PASS;
        }
        if (a.op == FilterOp.OR) {
            ((List<Filter>) a.getValue()).add(b);
            return a;
        }
        if (b.op == FilterOp.OR) {
            ((List<Filter>) b.getValue()).add(a);
            return b;
        }
        return or(Arrays.asList(a, b));
    }

    public static Filter and(List<Filter> subFilters) {
        return and(subFilters, null);
    }

    public static Filter and(List<Filter> subFilters, String tag) {
        if (subFilters == null || subFilters.isEmpty()) {
            return null;
        }
        if (subFilters.size() == 1) {
            return subFilters.get(0).setTag(tag);
        }
        if (subFilters.stream().anyMatch(f -> f == DENY)) {
            return DENY;
        }
        return new Filter(FilterOp.AND, null, subFilters).setTag(tag);
    }

    @SuppressWarnings("unchecked")
    public static Filter and(Filter a, Filter b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        if (a == DENY || b == DENY) {
            return DENY;
        }
        if (a.op == FilterOp.AND) {
            ((List<Filter>) a.getValue()).add(b);
            return a;
        }
        if (b.op == FilterOp.AND) {
            ((List<Filter>) b.getValue()).add(a);
            return b;
        }
        return and(new ArrayList<>(Arrays.asList(a, b)));
    }

    public static Filter isNull(HakunaProperty prop) {
        return new Filter(FilterOp.NULL, prop, null);
    }

    public static Filter isNotNull(HakunaProperty prop) {
        return new Filter(FilterOp.NOT_NULL, prop, null);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((op == null) ? 0 : op.hashCode());
        result = prime * result + ((prop == null) ? 0 : prop.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Filter other = (Filter) obj;
        if (op != other.op)
            return false;
        if (prop == null) {
            if (other.prop != null)
                return false;
        } else if (!prop.equals(other.prop))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        if (caseInsensitive != other.caseInsensitive)
            return false;
        return true;
    }

}
