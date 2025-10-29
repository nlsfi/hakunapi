package fi.nls.hakunapi.cql2.text;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import fi.nls.hakunapi.core.FeatureType;
import fi.nls.hakunapi.core.FilterParser;
import fi.nls.hakunapi.core.filter.Filter;
import fi.nls.hakunapi.cql2.Cql2Lexer;
import fi.nls.hakunapi.cql2.Cql2Parser;
import fi.nls.hakunapi.cql2.model.EmptyExpression;
import fi.nls.hakunapi.cql2.model.Expression;
import fi.nls.hakunapi.cql2.model.ExpressionToHakunaFilter;
import fi.nls.hakunapi.cql2.model.FilterContext;
import fi.nls.hakunapi.cql2.model.SimpleFilterContext;

public class CQL2Text implements FilterParser {

    public static final CQL2Text INSTANCE = new CQL2Text();

    private CQL2Text() {}

    @Override
    public String getCode() {
        return "cql2-text";
    }

    @Override
    public Filter parse(FeatureType ft, String filter, int filterSrid) throws IllegalArgumentException {
        try {
            Expression expression = parse(filter, new GeometryFactory(new PrecisionModel(), filterSrid));
            FilterContext context = new SimpleFilterContext(ft, ft.getSrid(filterSrid).get());
            return (Filter) new ExpressionToHakunaFilter().visit(expression, context);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw e;
            }
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public static Expression parse(String filter) {
        return parse(filter, new GeometryFactory());
    }

    public static Expression parse(String filter, GeometryFactory gf) {
        if (filter.isBlank()) {
            return EmptyExpression.INSTANCE;
        }

        CharStream charStream = new CaseChangingCharStream(CharStreams.fromString(filter), true);

        Cql2Lexer lexer = new Cql2Lexer(charStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(ThrowingErrorListener.INSTANCE);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        Cql2Parser parser = new Cql2Parser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(ThrowingErrorListener.INSTANCE);

        ParseTree tree = parser.cqlExpression();

        TextParserVisitor visitor = new TextParserVisitor(gf);
        return visitor.visit(tree);
    }

}
