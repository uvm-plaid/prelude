grammar Prelude;

program :
    (
        ('constraintfunctions:' constraintFunc*) |
        ('exprfunctions:' exprFunc*) |
        ('cmdfunctions:' cmdFunc*)
    )+
    EOF ;

exprFunc : ident '(' (ident (',' ident)*)? ')' '{' expr '}' ;
constraintFunc : ident '(' (ident (',' ident)*)? ')' '{' constraint '}' ;

cmdFunc :
    ('precondition: (' pre ')')?
    ident '(' (typedIdent (',' typedIdent)*)? ')'
    ('{' cmd (';' cmd)* '}')?
    ('postcondition: (' post ')')? ;

pre : constraint ;
post : constraint ;

constraint
    : '(' constraint ')' #ParenConstraint
    | expr '==' expr #EqualConstraint
    | 'NOT' constraint #NotConstraint
    | constraint 'IFF' constraint  #IffConstraint
    | constraint 'AND' constraint #AndConstraint //left associative
    | constraint 'OR' constraint #OrConstraint
    | constraint 'IMPLIES' constraint #ImpliesConstraint
    | ident '(' (expr (',' expr)*)? ')' #FunctionCallConstraint
    | 'T' #TrueConstraint
    ;

expr
    : expr '.' ident #FieldSelectExpr
    | expr '@' expr #AtExpr
    | '-' expr #MinusExpr
    | expr '*' expr #TimesExpr
    | expr '+' expr #PlusExpr
    | expr '++' expr #ConcatExpr
    | '(' expr ')' #ParenExpr
    | 'let' ident '=' expr 'in' expr #LetExpr
    | ident '(' (expr (',' expr)*)? ')' #FunctionCallExpr
    | '|' (expr (',' expr)*)? '|' #VectorExpr
    | 's[' expr ']' #SecretExpr
    | 'r[' expr ']' #RandomExpr
    | 'm[' expr ']' #MessageExpr
    | 'p[' expr ']' #PublicExpr
    | 'out' ('[' expr ']')? #OutputExpr
    | '{' (flddecl (';' flddecl)*)? '}' #FieldExpr
    | ident #IdentExpr
    | STRING #Str
    | VALUE #Num
    ;

cmd
    : 'let' ident '=' expr 'in' cmd (';' cmd)* #LetCmd
    | 'assert' '(' expr '=' expr ')' '@' expr #AssertCmd
    | expr ':=' expr #AssignCmd
    | ident '(' (expr (',' expr)*)? ')' #FunctionCallCmd
    ;

type
    : 'cid' #PartyIndexType
    | 'string' #StringType
    | '{' (typedIdent (';' typedIdent)*)? '}' #RecordType ;

typedIdent : ident ':' type;
flddecl : ident '=' expr ;
ident : IDENTIFIER ;

VALUE : [0-9]+;
IDENTIFIER : [_a-zA-Z][_a-zA-Z0-9]*;
STRING : '"' ~('"')+ '"';
WS : [ \t\n\r\f]+ -> skip;
COMMENT : '//' ~[\r\n]* -> skip;
