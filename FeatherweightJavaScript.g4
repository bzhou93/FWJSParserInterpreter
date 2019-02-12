grammar FeatherweightJavaScript;


@header { package edu.sjsu.fwjs.parser; }

// Reserved words
IF        : 'if' ;
ELSE      : 'else' ;
WHILE	  : 'while' ;
FUNCTION  : 'function' ;
VAR       : 'var' ;
PRINT     : 'print' ;

// Literals
INT       : [1-9][0-9]* | '0' ;
BOOL      : 'true' | 'false' ;
NULL      : 'null' ;

// Symbols
MUL       : '*' ;
DIV       : '/' ;
ADD       : '+' ;
SUB       : '-' ;
MOD       : '%' ; 
GREATTHN  : '>' ;
LESSTHN   : '<' ; 
GREATEREQ : '>=' ;
LESSEQ    : '<=' ;
EQ        : '==' ;
SEPARATOR : ';' ;


// Whitespace and comments
NEWLINE   : '\r'? '\n' -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ; 
LINE_COMMENT  : '//' ~[\n\r]* -> skip ;
WS            : [ \t]+ -> skip ; // ignore whitespace

IDENTIFIER    : ('a' .. 'z' | 'A' .. 'Z' | '_')('a' .. 'z' | 'A' .. 'Z' | '_' | [0-9])* ;

// ***Parsing rules ***

/** The start rule */
prog: stat+ ;

stat: expr SEPARATOR                                    # bareExpr
    | IF '(' expr ')' block ELSE block                  # ifThenElse
    | IF '(' expr ')' block                             # ifThen
    | WHILE '(' expr ')' block 							# while
    | PRINT stat 										# print
    | SEPARATOR											# empty
    ;

expr: expr op=( '*' | '/' | '%' ) expr                  # MulDivMod
    | expr op=( '+' | '-' ) expr                        # AddSub
    | expr op=( '<' | '>' | '<=' | '>=' | '==' ) expr       # Comp
    | FUNCTION '(' (IDENTIFIER | IDENTIFIER ',')* ')' block   # functDec
    | IDENTIFIER '(' (expr | expr ',')* ')'					# functApp
    | VAR IDENTIFIER '=' expr 							# varDec
    | IDENTIFIER 										# reference
    | IDENTIFIER '=' expr  								# assign
    | INT                                               # int
    | BOOL												# bool
    | NULL 												# null
    | '(' expr ')'                                      # parens
    ;

block: '{' stat* '}'                                    # fullBlock
     | stat                                             # simpBlock
     ;