package fi.nls.hakunapi.cql2.text;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import fi.nls.hakunapi.cql2.Cql2Parser;
import fi.nls.hakunapi.cql2.Cql2Parser.BinaryComparisonPredicateContext;
import fi.nls.hakunapi.cql2.Cql2Parser.PropertyNameContext;
import fi.nls.hakunapi.cql2.Cql2ParserBaseVisitor;
import fi.nls.hakunapi.cql2.Cql2ParserVisitor;
import fi.nls.hakunapi.cql2.model.BinaryComparisonPredicate;
import fi.nls.hakunapi.cql2.model.ComparisonOperator;
import fi.nls.hakunapi.cql2.model.Expression;
import fi.nls.hakunapi.cql2.model.IsNullPredicate;
import fi.nls.hakunapi.cql2.model.LikePredicate;
import fi.nls.hakunapi.cql2.model.function.FunctionCall;
import fi.nls.hakunapi.cql2.model.literal.BooleanLiteral;
import fi.nls.hakunapi.cql2.model.literal.DateLiteral;
import fi.nls.hakunapi.cql2.model.literal.NumberLiteral;
import fi.nls.hakunapi.cql2.model.literal.PropertyName;
import fi.nls.hakunapi.cql2.model.literal.StringLiteral;
import fi.nls.hakunapi.cql2.model.literal.TimestampLiteral;
import fi.nls.hakunapi.cql2.model.logical.And;
import fi.nls.hakunapi.cql2.model.logical.Not;
import fi.nls.hakunapi.cql2.model.logical.Or;
import fi.nls.hakunapi.cql2.model.spatial.SpatialExpression;
import fi.nls.hakunapi.cql2.model.spatial.SpatialLiteral;
import fi.nls.hakunapi.cql2.model.spatial.SpatialOperator;
import fi.nls.hakunapi.cql2.model.spatial.SpatialPredicate;

public class TextParserVisitor extends Cql2ParserBaseVisitor<Expression> implements Cql2ParserVisitor<Expression> {

    private final GeometryFactory gf;

    public TextParserVisitor(GeometryFactory gf) {
        this.gf = gf;
    }

    @Override
    public Expression visitCqlExpression(Cql2Parser.CqlExpressionContext ctx) {
        return ctx.booleanExpression().accept(this);
    }

    @Override
    public Expression visitBooleanExpression(Cql2Parser.BooleanExpressionContext ctx) {
        List<Expression> or = ctx.booleanTerm().stream()
                .map(term -> term.accept(this))
                .collect(Collectors.toList());
        return or.size() == 1 ? or.get(0) : Or.from(or);
    }

    @Override
    public Expression visitBooleanTerm(Cql2Parser.BooleanTermContext ctx) {
        List<Expression> and = ctx.booleanFactor().stream()
                .map(term -> term.accept(this))
                .collect(Collectors.toList());
        return and.size() == 1 ? and.get(0) : And.from(and);
    }

    @Override
    public Expression visitBooleanFactor(Cql2Parser.BooleanFactorContext ctx) {
        boolean not = ctx.NOT() != null;
        Expression e = ctx.booleanPrimary().accept(this);
        return not ? Not.from(e) : e;
    }

    @Override
    public Expression visitBooleanPrimary(Cql2Parser.BooleanPrimaryContext ctx) {
        var wrapped = ctx.booleanExpression();
        if (wrapped != null) {
            return wrapped.accept(this);
        }
        return ctx.getChild(0).accept(this);
    }

    @Override
    public BinaryComparisonPredicate visitBinaryComparisonPredicate(BinaryComparisonPredicateContext ctx) {
        PropertyName propName = (PropertyName) ctx.maybeCaseiProperty().accept(this);
        ComparisonOperator op = ComparisonOperator.from(ctx.COMPARISON_OPERATOR().getText());
        Expression expr = ctx.scalarExpression().accept(this);
        return new BinaryComparisonPredicate(propName, op, expr);
    }

    @Override
    public Expression visitIsLikePredicate(Cql2Parser.IsLikePredicateContext ctx) {
        PropertyName prop = (PropertyName) ctx.maybeCaseiProperty().accept(this);
        StringLiteral pattern = (StringLiteral) ctx.maybeCaseiValue().accept(this);
        LikePredicate like = new LikePredicate(prop, pattern);
        boolean not = ctx.NOT() != null;
        return not ? Not.from(like) : like;
    }

    @Override
    public Expression visitMaybeCaseiProperty(Cql2Parser.MaybeCaseiPropertyContext ctx) {
        PropertyName prop = (PropertyName) ctx.propertyName().accept(this);
        boolean casei = ctx.CASEI() != null;
        return new PropertyName(prop.getValue(), casei);
    }

    @Override
    public Expression visitMaybeCaseiValue(Cql2Parser.MaybeCaseiValueContext ctx) {
        StringLiteral string = (StringLiteral) ctx.stringLiteral().accept(this);
        boolean casei = ctx.CASEI() != null;
        return new StringLiteral(string.getValue(), casei);
    }

    @Override
    public Expression visitIsBetweenPredicate(Cql2Parser.IsBetweenPredicateContext ctx) {
        PropertyName prop = (PropertyName) ctx.propertyName().accept(this);
        Expression lo = ctx.numericLiteral(0).accept(this);
        Expression hi = ctx.numericLiteral(1).accept(this);
        if (ctx.NOT() == null) {
            return And.from(
                    new BinaryComparisonPredicate(prop, ComparisonOperator.GTE, lo),
                    new BinaryComparisonPredicate(prop, ComparisonOperator.LTE, hi)
                    );
        } else {
            return Or.from(
                    new BinaryComparisonPredicate(prop, ComparisonOperator.LT, lo),
                    new BinaryComparisonPredicate(prop, ComparisonOperator.GT, hi)
                    );
        }
    }

    @Override
    public Expression visitIsNullPredicate(Cql2Parser.IsNullPredicateContext ctx) {
        PropertyName prop = (PropertyName) ctx.propertyName().accept(this);
        IsNullPredicate isNull = new IsNullPredicate(prop);
        boolean not = ctx.NOT() != null;
        return not ? Not.from(isNull) : isNull;
    }

    @Override
    public Expression visitIsInListPredicate(Cql2Parser.IsInListPredicateContext ctx) {
        PropertyName prop = (PropertyName) ctx.maybeCaseiProperty().accept(this);
        boolean not = ctx.NOT() != null;
        List<Expression> values = ctx.scalarExpression().stream()
                .map(expr -> expr.accept(this))
                .collect(Collectors.toList());

        if (not) {
            List<Expression> and = values.stream()
                    .map(value -> new BinaryComparisonPredicate(prop, ComparisonOperator.NEQ, value))
                    .collect(Collectors.toList());
            return And.from(and);
        } else {
            List<Expression> or = values.stream()
                    .map(value -> new BinaryComparisonPredicate(prop, ComparisonOperator.EQ, value))
                    .collect(Collectors.toList());
            return Or.from(or);
        }
    }

    @Override
    public Expression visitPropertyName(PropertyNameContext ctx) {
        return new PropertyName(ctx.Identifier().getText(), false);
    }

    @Override
    public FunctionCall visitFunction(Cql2Parser.FunctionContext ctx) {
        String name = ctx.Identifier().getText();
        List<Expression> args = ctx.argument().stream()
                .map(arg -> arg.accept(this))
                .collect(Collectors.toList());
        return new FunctionCall(name, args);
    }

    @Override
    public Expression visitStringLiteral(Cql2Parser.StringLiteralContext ctx) {
        String text = ctx.StringLiteral().getText();
        String cleanedUp = cleanUpStringLiteral(text);
        return new StringLiteral(cleanedUp, false);
    }

    private static final String cleanUpStringLiteral(final String s) {
        // String literals are wrapped in single quotes and single quotes
        // are escaped (either by backslash or another single quote)
        int p = s.length() - 2;
        char[] buf = new char[p];
        for (int i = p; i > 0; i--) {
            char c = buf[--p] = s.charAt(i);
            if (c == '\'') {
                i--;
            }
        }
        return new String(buf, p, buf.length - p);
    }

    @Override
    public Expression visitNumericLiteral(Cql2Parser.NumericLiteralContext ctx) {
        return new NumberLiteral(Double.parseDouble(ctx.Number().getText()));
    }

    @Override
    public Expression visitBooleanLiteral(Cql2Parser.BooleanLiteralContext ctx) {
        return new BooleanLiteral(Boolean.parseBoolean(ctx.Boolean().getText()));
    }

    @Override
    public Expression visitSpatialPredicate(Cql2Parser.SpatialPredicateContext ctx) {
        PropertyName prop = (PropertyName) ctx.propertyName().accept(this);
        SpatialOperator op = SpatialOperator.valueOf(ctx.SPATIAL_OPERATOR().getText().toUpperCase());
        SpatialExpression value = (SpatialExpression) ctx.spatialExpression().accept(this);
        return new SpatialPredicate(prop, op, value);
    }

    @Override
    public Expression visitCoordinate(Cql2Parser.CoordinateContext ctx) {
        double x = Double.parseDouble(ctx.children.get(0).getText());
        double y = Double.parseDouble(ctx.children.get(1).getText());
        double z = Coordinate.NULL_ORDINATE;
        if (ctx.getChildCount() == 3) {
            z = Double.parseDouble(ctx.children.get(2).getText());
        }
        return new CoordinateExpression(new Coordinate(x, y, z));
    }

    @Override
    public Expression visitPoint(Cql2Parser.PointContext ctx) {
        return ctx.pointText().accept(this);
    }

    @Override
    public Expression visitPointText(Cql2Parser.PointTextContext ctx) {
        return new SpatialLiteral(gf.createPoint(((CoordinateExpression) ctx.coordinate().accept(this)).c));
    }

    @Override
    public Expression visitLineString(Cql2Parser.LineStringContext ctx) {
        return ctx.lineStringText().accept(this);
    }

    @Override
    public Expression visitLineStringText(Cql2Parser.LineStringTextContext ctx) {
        Coordinate[] coordinates = ctx.coordinate().stream()
                .map(it -> (CoordinateExpression) it.accept(this))
                .map(ce -> ce.c)
                .collect(Collectors.toList())
                .toArray(new Coordinate[0]);
        return new SpatialLiteral(gf.createLineString(coordinates));
    }

    @Override
    public Expression visitPolygon(Cql2Parser.PolygonContext ctx) {
        return ctx.polygonText().accept(this);
    }

    @Override
    public Expression visitPolygonText(Cql2Parser.PolygonTextContext ctx) {
        List<LinearRing> rings = ctx.ring().stream()
                .map(it -> (LinearRingExpression) it.accept(this))
                .map(re -> re.ring)
                .collect(Collectors.toList());
        LinearRing shell = rings.get(0);
        LinearRing[] holes = rings.subList(1, rings.size()).toArray(new LinearRing[0]);
        return new SpatialLiteral(gf.createPolygon(shell, holes));
    }

    @Override
    public Expression visitRing(Cql2Parser.RingContext ctx) {
        List<Coordinate> coordinates = ctx.coordinate().stream()
                .map(it -> (CoordinateExpression) it.accept(this))
                .map(ce -> ce.c)
                .collect(Collectors.toList());
        if (!coordinates.get(0).equals3D(coordinates.get(coordinates.size() - 1))) {
            // Close rings
            coordinates.add(coordinates.get(0));
        }
        return new LinearRingExpression(gf.createLinearRing(coordinates.toArray(new Coordinate[0])));
    }

    @Override
    public Expression visitMultiPoint(Cql2Parser.MultiPointContext ctx) {
        Point[] points = ctx.pointText().stream()
                .map(it -> (SpatialLiteral) it.accept(this))
                .map(it -> (Point) it.getGeometry())
                .collect(Collectors.toList())
                .toArray(new Point[0]);
        return new SpatialLiteral(gf.createMultiPoint(points));
    }

    @Override
    public Expression visitMultiLineString(Cql2Parser.MultiLineStringContext ctx) {
        LineString[] lineStrings = ctx.lineStringText().stream()
                .map(it -> (SpatialLiteral) it.accept(this))
                .map(it -> (LineString) it.getGeometry())
                .collect(Collectors.toList())
                .toArray(new LineString[0]);
        return new SpatialLiteral(gf.createMultiLineString(lineStrings));
    }

    @Override
    public Expression visitMultiPolygon(Cql2Parser.MultiPolygonContext ctx) {
        Polygon[] polygons = ctx.polygonText().stream()
                .map(it -> (SpatialLiteral) it.accept(this))
                .map(it -> (Polygon) it.getGeometry())
                .collect(Collectors.toList())
                .toArray(new Polygon[0]);
        return new SpatialLiteral(gf.createMultiPolygon(polygons));
    }

    @Override
    public Expression visitGeometryCollection(Cql2Parser.GeometryCollectionContext ctx) {
        Geometry[] geometries = ctx.geometryLiteral().stream()
                .map(it -> (SpatialLiteral) it.accept(this))
                .map(it -> (Geometry) it.getGeometry())
                .collect(Collectors.toList())
                .toArray(new Geometry[0]);
        return new SpatialLiteral(gf.createGeometryCollection(geometries));
    }

    @Override
    public Expression visitBboxLiteral(Cql2Parser.BboxLiteralContext ctx) {
        double[] values = ctx.numericLiteral().stream()
                .map(v -> v.accept(this))
                .map(NumberLiteral.class::cast)
                .mapToDouble(NumberLiteral::getValue)
                .toArray();
        int i = 0;
        double x1 = values[i++];
        double y1 = values[i++];
        if (values.length == 6) {
            // Skip z
            i++;
            // Currently we throw an exception with ?bbox=x1,y1,z1,x2,y2,z2 so this is not exact same behaviour
        }
        double x2 = values[i++];
        double y2 = values[i++];
        Coordinate[] shell = new Coordinate[] {
                new Coordinate(x1, y1),
                new Coordinate(x2, y1),
                new Coordinate(x2, y2),
                new Coordinate(x1, y2),
                new Coordinate(x1, y1)
        };
        return new SpatialLiteral(gf.createPolygon(shell));
    }

    @Override
    public Expression visitDateLiteral(Cql2Parser.DateLiteralContext ctx) {
        String text = ctx.StringLiteral().getText();
        int i = text.indexOf('\'');
        int j = text.lastIndexOf('\'');
        return new DateLiteral(LocalDate.parse(text.substring(i + 1, j)));
    }

    @Override
    public Expression visitTimestampLiteral(Cql2Parser.TimestampLiteralContext ctx) {
        String text = ctx.StringLiteral().getText();
        int i = text.indexOf('\'');
        int j = text.lastIndexOf('\'');
        return new TimestampLiteral(Instant.parse(text.substring(i + 1, j)));
    }

    private class LinearRingExpression implements Expression {

        private final LinearRing ring;

        public LinearRingExpression(LinearRing ring) {
            this.ring = ring;
        }

    }

    private class CoordinateExpression implements Expression {

        private final Coordinate c;

        public CoordinateExpression(Coordinate c) {
            this.c = c;
        }

    }

}
