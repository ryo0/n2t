data class Statements(val statements: List<Stmt>)

sealed class Stmt {
    data class Let(val stmt: LetStatement) : Stmt()
    data class If(val stmt: IfStatement) : Stmt()
}

data class LetStatement(val varName: String, val index: Expression?, val exp: Expression)
data class IfStatement(val expression: Expression, val ifStmt: Statements, val elseStmt: Statements?)

enum class Op {
    Plus, Minus, Asterisk, Slash, And, Pipe, LessThan, GreaterThan, Equal
}

data class OpAndTerm(val op: Op, val term: Term)

data class Expression(val term: Term, val opsAndTerms: List<OpAndTerm>)

data class Term(val constant: Constant)

sealed class Constant {
    data class IntCons(val const: Int) : Constant()
    data class StrCons(val const: String) : Constant()
    data class KeyCons(val const: Keyword) : Constant()
}

enum class Keyword {
    True, False, Null, This
}