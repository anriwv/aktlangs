grammar Sqelite;
@header { package toylangs.sqelite; }

init : prog EOF;

prog : (assignement ',')* expr;

expr
    : VAR                                   # SqliteVar
    | '(' expr ')'                          # Paren
    | FROM '[' (INT (',' INT)*)? ']'        # SqliteSource
    | MIN '(' expr? ')'                     # SqeliteMin
    | MAX '(' expr? ')'                     # SqeliteMax
    | AVG '(' expr? ')'                     # SqeliteAvg
    | INT                                   # SqliteNum
    | left=expr op=('*'|'/') right=expr     # SqliteBinOp
    | left=expr op=('+'|'-') right=expr     # SqliteBinOp
    | left=expr op=('>'|'<'|'=='|'!=') right=expr # SqliteBinOp
    | expr WHERE expr                       # SqliteQuery
    | expr SELECT expr                      # SqliteQuery
    ;

assignement: VAR '=' expr;

FROM   : 'from';
WHERE  : 'where';
SELECT : 'select';
MIN    : 'min';
MAX    : 'max';
AVG    : 'avg';

VAR : [a-zA-Z_][a-zA-Z0-9_]*;
INT : [0-9]+;

WS : [ \t\r\n]+ -> skip;