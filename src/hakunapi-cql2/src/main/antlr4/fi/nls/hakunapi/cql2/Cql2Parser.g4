parser grammar Cql2Parser;

options {
    language = Java;
    tokenVocab = Cql2Lexer;
}

cqlExpression
    : booleanExpression EOF
    ;

booleanExpression
    : booleanTerm ((OR booleanTerm)*)?
    ;

booleanTerm
    : booleanFactor ((AND booleanFactor)*)?
    ;

booleanFactor
    : NOT? booleanPrimary
    ;

booleanPrimary
    : OPEN_PAREN booleanExpression CLOSE_PAREN
    | function
    | predicate
    | booleanLiteral
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
    : maybeCaseiProperty COMPARISON_OPERATOR scalarExpression
    ;

scalarExpression
    : function
    | temporalLiteral
    | booleanLiteral
    | numericLiteral
    | maybeCaseiValue
    ;

isLikePredicate
    : maybeCaseiProperty NOT? LIKE maybeCaseiValue
    ;

maybeCaseiProperty
    : CASEI OPEN_PAREN propertyName CLOSE_PAREN
    | propertyName
    ;

maybeCaseiValue
    : CASEI OPEN_PAREN stringLiteral CLOSE_PAREN
    | stringLiteral
    ;

isBetweenPredicate
    : propertyName NOT? BETWEEN numericLiteral AND numericLiteral
    ;

isInListPredicate
    : maybeCaseiProperty NOT? IN OPEN_PAREN scalarExpression (COMMA scalarExpression)* CLOSE_PAREN
    ;

isNullPredicate
    : propertyName IS NOT? NULL
    ;    

function
    : Identifier OPEN_PAREN (argument (COMMA argument)*)? CLOSE_PAREN
    ;

argument
    : function
    | propertyName
    | spatialLiteral
    | temporalLiteral
    | booleanLiteral
    | numericLiteral
    | stringLiteral
    ;

spatialPredicate
    : SPATIAL_OPERATOR OPEN_PAREN propertyName COMMA spatialExpression CLOSE_PAREN
    ;

spatialExpression
    : function
    | spatialLiteral
    ;

spatialLiteral
    : geometryLiteral
    | geometryCollection
    | bboxLiteral
    ;

geometryLiteral
    : point
    | lineString
    | polygon
    | multiPoint
    | multiLineString
    | multiPolygon
    ;

coordinate
    : numericLiteral numericLiteral numericLiteral?
    ;

point
    : POINT pointText
    ;

pointText
    : OPEN_PAREN coordinate CLOSE_PAREN
    ;

lineString
    : LINESTRING lineStringText
    ;

lineStringText
    : OPEN_PAREN coordinate (COMMA coordinate)+ CLOSE_PAREN
    ;

polygon
    : POLYGON polygonText
    ;

polygonText
    : OPEN_PAREN ring (COMMA ring)* CLOSE_PAREN
    ;

ring
    : OPEN_PAREN coordinate COMMA coordinate COMMA coordinate (COMMA coordinate)+ CLOSE_PAREN
    ;

multiPoint
    : MULTIPOINT OPEN_PAREN pointText (COMMA pointText)* CLOSE_PAREN
    ;

multiLineString
    : MULTILINESTRING OPEN_PAREN lineStringText (COMMA lineStringText)* CLOSE_PAREN
    ;
    
multiPolygon
    : MULTIPOLYGON OPEN_PAREN polygonText (COMMA polygonText)* CLOSE_PAREN
    ;

geometryCollection
    : GEOMETRYCOLLECTION OPEN_PAREN geometryLiteral (COMMA geometryLiteral)* CLOSE_PAREN
    ;

bboxLiteral
    : BBOX OPEN_PAREN numericLiteral COMMA numericLiteral COMMA numericLiteral COMMA numericLiteral (COMMA numericLiteral COMMA numericLiteral)? CLOSE_PAREN
    ;

temporalLiteral
    : dateLiteral
    | timestampLiteral
    ;

dateLiteral
    : DATE OPEN_PAREN StringLiteral CLOSE_PAREN
    ;

timestampLiteral
    : TIMESTAMP OPEN_PAREN StringLiteral CLOSE_PAREN
    ;

booleanLiteral
    : Boolean;

numericLiteral
    : Number;

stringLiteral
    : StringLiteral;

propertyName
    : Identifier
    | DOUBLE_QUOTE Identifier DOUBLE_QUOTE
    ;
