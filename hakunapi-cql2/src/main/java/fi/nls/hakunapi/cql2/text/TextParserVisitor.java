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
import fi.nls.hakunapi.cql2.Cql2Parser.AndExprContext;
import fi.nls.hakunapi.cql2.Cql2Parser.BinaryComparisonPredicateContext;
import fi.nls.hakunapi.cql2.Cql2Parser.OrExprContext;
import fi.nls.hakunapi.cql2.Cql2Parser.PredicateExprContext;
import fi.nls.hakunapi.cql2.Cql2Parser.PropertyNameContext;
import fi.nls.hakunapi.cql2.Cql2Parser.ScalarExpressionContext;
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
import fi.nls.hakunapi.cql2.model.literal.Literal;
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
        return ctx.expression().accept(this);
    }

    @Override
    public Expression visitNestedExpr(Cql2Parser.NestedExprContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public Expression visitPredicateExpr(PredicateExprContext ctx) {
        boolean not = ctx.NOT() != null;
        Expression e = ctx.predicate().accept(this);
        if (not) {
            return Not.from(e);
        } else {
            return e;
        }
    }

    @Override
    public Expression visitAndExpr(AndExprContext ctx) {
        Expression left = ctx.getChild(0).accept(this);
        Expression right = ctx.getChild(2).accept(this);
        return And.from(left, right);
    }

    @Override
    public Expression visitOrExpr(OrExprContext ctx) {
        Expression left = ctx.getChild(0).accept(this);
        Expression right = ctx.getChild(2).accept(this);
        return Or.from(left, right);
    }

    @Override
    public BinaryComparisonPredicate visitBinaryComparisonPredicate(BinaryComparisonPredicateContext ctx) {
        PropertyName prop = (PropertyName) ctx.propertyName().accept(this);
        ComparisonOperator op = ComparisonOperator.from(ctx.COMPARISON_OPERATOR().getText());
        Expression expr = ctx.scalarExpression().accept(this);
        return new BinaryComparisonPredicate(prop, op, expr);
    }

    @Override
    public LikePredicate visitIsLikePredicate(Cql2Parser.IsLikePredicateContext ctx) {
        PropertyName prop = (PropertyName) ctx.propertyName().accept(this);
        StringLiteral literal = (StringLiteral) ctx.stringLiteral().accept(this);
        return new LikePredicate(prop, literal.getValue());
    }

    @Override
    public Expression visitIsBetweenPredicate(Cql2Parser.IsBetweenPredicateContext ctx) {
        PropertyName prop = (PropertyName) ctx.propertyName().accept(this);
        Literal lo = (Literal) ctx.numberLiteral(0).accept(this);
        Literal hi = (Literal) ctx.numberLiteral(1).accept(this);
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
        PropertyName prop = (PropertyName) ctx.propertyName().accept(this);
        boolean not = ctx.NOT() != null;
        List<Literal> literals = ctx.scalarExpression().stream()
                .map(expr -> expr.accept(this))
                .map(Literal.class::cast)
                .collect(Collectors.toList());

        if (not) {
            List<Expression> and = literals.stream()
                    .map(value -> new BinaryComparisonPredicate(prop, ComparisonOperator.NEQ, value))
                    .collect(Collectors.toList());
            return And.from(and);
        } else {
            List<Expression> or = literals.stream()
                    .map(value -> new BinaryComparisonPredicate(prop, ComparisonOperator.EQ, value))
                    .collect(Collectors.toList());
            return Or.from(or);
        }
    }

    @Override
    public PropertyName visitPropertyName(PropertyNameContext ctx) {
        String text = ctx.getText();
        if (text.charAt(0) == '"') {
            text = text.substring(1, text.length() - 1);
        }
        return new PropertyName(text);
    }

    @Override
    public Expression visitScalarExpression(ScalarExpressionContext ctx) {
        return ctx.getChild(0).accept(this);
    }

    @Override
    public FunctionCall visitFunction(Cql2Parser.FunctionContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        List<Expression> args = ctx.argument().stream()
                .map(arg -> arg.accept(this))
                .collect(Collectors.toList());
        return new FunctionCall(name, args);
    }

    @Override
    public Expression visitArgument(Cql2Parser.ArgumentContext ctx) {
        return ctx.getChild(0).accept(this);
    }

    @Override
    public StringLiteral visitStringLiteral(Cql2Parser.StringLiteralContext ctx) {
        String text = ctx.getText();
        String value = text.substring(1, text.length() - 1);
        value = value.replaceAll("''", "'");
        return new StringLiteral(value);
    }

    @Override
    public NumberLiteral visitNumberLiteral(Cql2Parser.NumberLiteralContext ctx) {
        return new NumberLiteral(Double.parseDouble(ctx.getText()));
    }

    @Override
    public BooleanLiteral visitBooleanLiteral(Cql2Parser.BooleanLiteralContext ctx) {
        return new BooleanLiteral(Boolean.parseBoolean(ctx.getText()));
    }

    @Override
    public SpatialPredicate visitSpatialPredicate(Cql2Parser.SpatialPredicateContext ctx) {
        PropertyName prop = (PropertyName) ctx.propertyName().accept(this);
        SpatialOperator op = SpatialOperator.valueOf(ctx.SPATIAL_OPERATOR().getText().toUpperCase());
        SpatialExpression value = (SpatialExpression) ctx.spatialExpression().accept(this);
        return new SpatialPredicate(prop, op, value);
    }

    /* Just parse it ourselves instead of moving the logic to lexer and parsing the text with WKTReader
     * This doesn't work as Lexer removes whitespace
    @Override
    public SpatialLiteral visitSpatialLiteral(Cql2Parser.SpatialLiteralContext ctx) {
        try {
            Geometry g = new WKTReader(gf).read(ctx.getText());
            return new SpatialLiteral(g);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
     */

    @Override
    public SpatialLiteral visitPoint(Cql2Parser.PointContext ctx) {
        CoordinateExpression ce = (CoordinateExpression) ctx.coordinate().accept(this);
        return new SpatialLiteral(gf.createPoint(ce.c));
    }

    @Override
    public SpatialLiteral visitLineString(Cql2Parser.LineStringContext ctx) {
        return (SpatialLiteral) ctx.lineStringText().accept(this);
    }

    @Override
    public SpatialLiteral visitPolygon(Cql2Parser.PolygonContext ctx) {
        return (SpatialLiteral) ctx.polygonText().accept(this);
    }

    @Override
    public SpatialLiteral visitMultiPoint(Cql2Parser.MultiPointContext ctx) {
        Point[] points = ctx.coordinate().stream()
                .map(it -> (CoordinateExpression) it.accept(this))
                .map(ce -> ce.c)
                .map(c -> gf.createPoint(c))
                .collect(Collectors.toList())
                .toArray(new Point[0]);
        return new SpatialLiteral(gf.createMultiPoint(points));
    }

    @Override
    public SpatialLiteral visitMultiLineString(Cql2Parser.MultiLineStringContext ctx) {
        LineString[] lineStrings = ctx.lineStringText().stream()
                .map(it -> (SpatialLiteral) it.accept(this))
                .map(it -> (LineString) it.getGeometry())
                .collect(Collectors.toList())
                .toArray(new LineString[0]);
        return new SpatialLiteral(gf.createMultiLineString(lineStrings));
    }

    @Override
    public SpatialLiteral visitMultiPolygon(Cql2Parser.MultiPolygonContext ctx) {
        Polygon[] polygons = ctx.polygonText().stream()
                .map(it -> (SpatialLiteral) it.accept(this))
                .map(it -> (Polygon) it.getGeometry())
                .collect(Collectors.toList())
                .toArray(new Polygon[0]);
        return new SpatialLiteral(gf.createMultiPolygon(polygons));
    }

    @Override
    public SpatialLiteral visitGeometryCollection(Cql2Parser.GeometryCollectionContext ctx) {
        Geometry[] geometries = ctx.spatialLiteral().stream()
                .map(it -> (SpatialLiteral) it.accept(this))
                .map(it -> (Geometry) it.getGeometry())
                .collect(Collectors.toList())
                .toArray(new Geometry[0]);
        return new SpatialLiteral(gf.createGeometryCollection(geometries));
    }

    @Override
    public SpatialLiteral visitEnvelope(Cql2Parser.EnvelopeContext ctx) {
        Coordinate c0 = ((CoordinateExpression) ctx.coordinate(0).accept(this)).c;
        Coordinate c1 = ((CoordinateExpression) ctx.coordinate(1).accept(this)).c;
        return new SpatialLiteral(toGeometry(c0.x, c0.y, c1.x, c1.y, gf));
    }

    private static Geometry toGeometry(double x1, double y1, double x2, double y2, GeometryFactory gf) {
        Coordinate[] shell = {
                new Coordinate(x1, y1),
                new Coordinate(x2, y1),
                new Coordinate(x2, y2),
                new Coordinate(x1, y2),
                new Coordinate(x1, y1)
        };
        return gf.createPolygon(shell);
    }

    @Override
    public SpatialLiteral visitLineStringText(Cql2Parser.LineStringTextContext ctx) {
        Coordinate[] coordinates = ctx.coordinate().stream()
                .map(it -> (CoordinateExpression) it.accept(this))
                .map(ce -> ce.c)
                .collect(Collectors.toList())
                .toArray(new Coordinate[0]);
        return new SpatialLiteral(gf.createLineString(coordinates));
    }

    @Override
    public SpatialLiteral visitPolygonText(Cql2Parser.PolygonTextContext ctx) {
        List<LinearRing> rings = ctx.ring().stream()
                .map(it -> (LinearRingExpression) it.accept(this))
                .map(re -> re.ring)
                .collect(Collectors.toList());
        LinearRing shell = rings.get(0);
        LinearRing[] holes = rings.subList(1, rings.size()).toArray(new LinearRing[0]);
        return new SpatialLiteral(gf.createPolygon(shell, holes));
    }

    @Override
    public LinearRingExpression visitRing(Cql2Parser.RingContext ctx) {
        List<Coordinate> coordinates = ctx.coordinate().stream()
                .map(it -> (CoordinateExpression) it.accept(this))
                .map(ce -> ce.c)
                .collect(Collectors.toList());

        if (!coordinates.get(0).equals3D(coordinates.get(coordinates.size() - 1))) {
            coordinates.add(coordinates.get(0));
        }

        return new LinearRingExpression(gf.createLinearRing(coordinates.toArray(new Coordinate[0])));
    }

    @Override
    public CoordinateExpression visitCoordinate(Cql2Parser.CoordinateContext ctx) {
        double x = Double.parseDouble(ctx.children.get(0).getText());
        double y = Double.parseDouble(ctx.children.get(1).getText());
        double z = Coordinate.NULL_ORDINATE;
        if (ctx.getChildCount() == 3) {
            z = Double.parseDouble(ctx.children.get(2).getText());
        }
        return new CoordinateExpression(new Coordinate(x, y, z));
    }

    @Override
    public DateLiteral visitDateLiteral(Cql2Parser.DateLiteralContext ctx) {
        String text = ctx.getText();
        int i = text.indexOf('\'');
        int j = text.indexOf('\'', i + 1);
        return new DateLiteral(LocalDate.parse(text.subSequence(i + 1, j)));
    }

    @Override
    public TimestampLiteral visitTimestampLiteral(Cql2Parser.TimestampLiteralContext ctx) {
        String text = ctx.getText();
        int i = text.indexOf('\'');
        int j = text.indexOf('\'', i + 1);
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
