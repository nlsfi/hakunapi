parser grammar Cql2Parser;

options {
    language = Java;
    tokenVocab = Cql2Lexer;
}

cqlExpression
    : expression EOF
    ;

expression
    : OPEN_PAREN expression CLOSE_PAREN #NestedExpr
    | NOT? predicate                    #PredicateExpr
    | expression AND expression         #AndExpr
    | expression OR expression          #OrExpr
    ;
    
predicate
    : comparisonPredicate
    | spatialPredicate
    ;

comparisonPredicate
    : binaryComparisonPredicate
    | isLikePredicate
    | isBetweenPredicate
    | isInListPredicate
    | isNullPredicate
    ;

binaryComparisonPredicate
    : propertyName COMPARISON_OPERATOR scalarExpression
    ;

isLikePredicate
    : propertyName LIKE stringLiteral
    ;

isBetweenPredicate
    : propertyName NOT? BETWEEN numberLiteral AND numberLiteral
    ;

isInListPredicate
    : propertyName NOT? IN OPEN_PAREN scalarExpression (COMMA scalarExpression)* CLOSE_PAREN
    ;

isNullPredicate
    : propertyName IS NOT? NULL
    ;    

propertyName
    : IDENTIFIER
    ;

scalarExpression
    : stringLiteral
    | numberLiteral
    | booleanLiteral
    | instantLiteral
    | function
    ;

function
    : IDENTIFIER OPEN_PAREN argument (COMMA argument)* CLOSE_PAREN
    ;

argument
    : stringLiteral
    | numberLiteral
    | booleanLiteral
    | spatialLiteral
    | instantLiteral
    | propertyName
    | function
    ;
    
stringLiteral
    : STRING_LITERAL
    ;

numberLiteral
    : NUMBER_LITERAL
    ;

booleanLiteral
    : BOOLEAN_LITERAL
    ;

instantLiteral
    : dateLiteral
    | timestampLiteral
    ;
    
dateLiteral
    : DATE_LITERAL
    ;

timestampLiteral
    : TIMESTAMP_LITERAL
    ;    

spatialPredicate
    : SPATIAL_OPERATOR OPEN_PAREN propertyName COMMA spatialExpression CLOSE_PAREN
    ;

spatialExpression
    : spatialLiteral
    | function
    ;

spatialLiteral
    : geometryLiteral
    | envelope
    ;

geometryLiteral    
    : point
    | lineString
    | polygon
    | multiPoint
    | multiLineString
    | multiPolygon
    | geometryCollection 
    ;

point
    : POINT OPEN_PAREN coordinate CLOSE_PAREN
    ;
    
lineString
    : LINESTRING lineStringText
    ;
    
polygon
    : POLYGON polygonText
    ;

multiPoint
    : MULTIPOINT OPEN_PAREN coordinate (COMMA coordinate)* CLOSE_PAREN
    ;

multiLineString
    : MULTILINESTRING OPEN_PAREN lineStringText (COMMA lineStringText)* CLOSE_PAREN
    ;
    
multiPolygon
    : MULTIPOLYGON OPEN_PAREN polygonText (COMMA polygonText)* CLOSE_PAREN
    ;

geometryCollection
    : GEOMETRYCOLLECTION OPEN_PAREN spatialLiteral (COMMA spatialLiteral)* CLOSE_PAREN
    ;

envelope
    : ENVELOPE OPEN_PAREN coordinate COMMA coordinate CLOSE_PAREN
    ;

lineStringText
    : OPEN_PAREN coordinate (COMMA coordinate)+ CLOSE_PAREN
    ;

polygonText
    : OPEN_PAREN ring (COMMA ring)* CLOSE_PAREN
    ;

ring
    : OPEN_PAREN coordinate COMMA coordinate COMMA coordinate (COMMA coordinate)+ CLOSE_PAREN
    ;

coordinate
    : NUMBER_LITERAL NUMBER_LITERAL NUMBER_LITERAL?
    ;
