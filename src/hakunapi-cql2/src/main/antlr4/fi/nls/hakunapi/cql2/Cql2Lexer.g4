lexer grammar Cql2Lexer;

LIKE : 'LIKE';
CASEI : 'CASEI';

BETWEEN : 'BETWEEN';
IN : 'IN';

IS : 'IS';
NULL : 'NULL';

AND : 'AND';
OR : 'OR';
NOT : 'NOT';

DATE : 'DATE';
TIMESTAMP : 'TIMESTAMP';
INTERVAL: 'INTERVAL';

POINT : 'POINT';
LINESTRING : 'LINESTRING';
POLYGON : 'POLYGON';
MULTIPOINT : 'MULTIPOINT';
MULTILINESTRING : 'MULTILINESTRING';
MULTIPOLYGON : 'MULTIPOLYGON';
GEOMETRYCOLLECTION : 'GEOMETRYCOLLECTION';

BBOX : 'BBOX';

COMPARISON_OPERATOR
    : '='
    | '<>'
    | '<'
    | '>'
    | '<='
    | '>='
    ;

SPATIAL_OPERATOR
    : 'S_INTERSECTS'
    | 'S_EQUALS'
    | 'S_DISJOINT'
    | 'S_TOUCHES'
    | 'S_WITHIN'
    | 'S_OVERLAPS'
    | 'S_CROSSES'
    | 'S_CONTAINS'
    ;

COMMA : ',';
DOUBLE_QUOTE: '"';
OPEN_PAREN : '(';
CLOSE_PAREN : ')';

Number
    : UNumber
    | SNumber
    ;

fragment UNumber
    : UDecimal
    | Scientific
    ;

fragment SNumber : [+-]? UNumber;

fragment UDecimal
    : UInt (PERIOD UInt?)?
    | PERIOD UInt
    ;

fragment Scientific : UDecimal 'E' SInt;

fragment UInt : Digit+;
fragment SInt : [+-]? UInt;

Boolean
    : 'TRUE'
    | 'FALSE'
    ;

Identifier : IdentifierStart IdentifierPart*;

fragment IdentifierPart
    : IdentifierStart
    | PERIOD              // "\u002E"
    | Digit               // 0-9
    | [\u0300-\u036F]     // combining and diacritical marks
    | [\u203F-\u2040];    // ‿ and ⁀

fragment IdentifierStart
    : COLON
    | UNDERSCORE
    | [\u0041-\u005A]    // A-Z
    | [\u0061-\u007A]    // a-z
    | [\u00C0-\u00D6]    // À-Ö Latin-1 Supplement Letters
    | [\u00D8-\u00F6]    // Ø-ö Latin-1 Supplement Letters
    | [\u00F8-\u02FF]    // ø-ÿ Latin-1 Supplement Letters
    | [\u0370-\u037D]    // Ͱ-ͽ Greek and Coptic (without ";")
    | [\u037F-\u1FFE]    // See note 1.
    | [\u200C-\u200D]    // zero width non-joiner and joiner
    | [\u2070-\u218F]    // See note 2.
    | [\u2C00-\u2FEF]    // See note 3.
    | [\u3001-\uD7FF]    // See note 4.
    | [\uF900-\uFDCF]    // See note 5.
    | [\uFDF0-\uFFFD]    // See note 6.
    | [\u{10000}-\u{EFFFF}]  // See note 7.
;

StringLiteral
    : QUOTE Character* QUOTE
    ;

fragment Character
    : EscapeQuote
    | Digit
    | Alpha
    | WS
    ;

fragment EscapeQuote
    : QUOTE QUOTE
    | BACKSLASH QUOTE
    ;

fragment Alpha
    : [\u0007-\u0008]     // bell, bs
    | [\u0021-\u0026]     // !, ", #, $, %, &
    | [\u0028-\u002F]     // (, ), *, +, comma, -, ., /
    | [\u003A-\u0084]     // --+
    | [\u0086-\u009F]     //   |
    | [\u00A1-\u167F]     //   |
    | [\u1681-\u1FFF]     //   |
    | [\u200B-\u2027]     //   +-> :,;,<,=,>,?,@,A-Z,[,\,],^,_,`,a-z,...
    | [\u202A-\u202E]     //   |
    | [\u2030-\u205E]     //   |
    | [\u2060-\u2FFF]     //   |
    | [\u3001-\uD7FF]     // --+
    | [\uE000-\uFFFD]     // See note 8.
    | [\u{10000}-\u{10FFFF}]  // See note 9.
    ;

fragment Digit : [0-9];

fragment COLON : ':';
fragment PERIOD : '.';
fragment UNDERSCORE : '_';
fragment QUOTE : '\'';
fragment BACKSLASH: '\\';

WS : [ \t\r\n]+ -> skip;