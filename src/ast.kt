data class Class(val name: String, val varDec: List<ClassVarDec>, val subroutineDec: List<SubroutineDec>)

data class ClassVarDec(val varDec: _ClassVarDec, val type: Type, val varNames: List<String>)

enum class _ClassVarDec {
    Field, Static
}

sealed class Type {
    object Int : Type()
    object Char : Type()
    object Boolean : Type()
    data class ClassName(val name: String) : Type()
}

sealed class VoidOrType {
    data class _Type(val type: Type) : VoidOrType()
    object Void : VoidOrType()
}

enum class MethodDec {
    Constructor, Function, Method
}

data class SubroutineDec(
    val dec: MethodDec, val type: VoidOrType, val name: String, val paramList: ParameterList,
    val body: SubroutineBody
)

data class Parameter(val type: Type, val name: String)

data class ParameterList(val list: List<Parameter>)

data class SubroutineBody(val varDecs: List<VarDec>, val statements: Statements)

data class VarDec(val type: Type, val vars: List<String>)

data class Statements(val statements: List<Stmt>)

sealed class Stmt {
    data class Let(val stmt: LetStatement) : Stmt()
    data class If(val stmt: IfStatement) : Stmt()
    data class While(val stmt: WhileStatement) : Stmt()
    data class Do(val stmt: DoStatement) : Stmt()
    data class Return(val stmt: ReturnStatement) : Stmt()
}

data class LetStatement(val varName: Term.VarName, val index: Expression?, val exp: Expression)
data class IfStatement(val expression: Expression, val ifStmts: Statements, val elseStmts: Statements)
data class WhileStatement(val expression: Expression, val statements: Statements)
data class DoStatement(val subroutineCall: SubroutineCall)
data class ReturnStatement(val expression: Expression?)

sealed class Term {
    data class IntC(val const: Int) : Term()
    data class StrC(val const: String) : Term()
    data class KeyC(val const: Keyword) : Term()
    data class VarName(val name: String) : Term()
    data class ArrayAndIndex(val name: String, val index: Expression) : Term()
    data class _SubroutineCall(val call: SubroutineCall) : Term()
    data class _Expression(val left: Paren, val exp: Expression, val right: Paren) : Term()
    data class UnaryOpTerm(val op: UnaryOp, val term: Term) : Term()
}

sealed class ExpElm {
    data class _Term(val term: Term) : ExpElm()
    data class _Op(val op: Op) : ExpElm()
}

data class Expression(val expElms: List<ExpElm>)

data class ExpressionList(val expList: List<Expression>)

data class Identifier(val name: String)
data class SubroutineName(val name: String)

data class SubroutineCall(
    val subroutineName: Identifier,
    val expList: ExpressionList,
    val classOrVarName: Identifier?
)

enum class Keyword {
    Class, Constructor, Function, Method, Field, Static, Var, Int, Char, Boolean, Void, True, False, Null, This, Let, Do, If, Else
}

val opHash = mapOf(
    Token.Plus to Op.Plus,
    Token.Minus to Op.Minus,
    Token.Asterisk to Op.Asterisk,
    Token.Slash to Op.Slash,
    Token.And to Op.And,
    Token.Pipe to Op.Pipe,
    Token.LessThan to Op.LessThan,
    Token.GreaterThan to Op.GreaterThan,
    Token.Equal to Op.Equal
)

val unaryOpHash = mapOf(
    Token.Minus to UnaryOp.Minus,
    Token.Tilde to UnaryOp.Tilde
)

val subDecHash = mapOf(
    Token.Constructor to MethodDec.Constructor,
    Token.Function to MethodDec.Function,
    Token.Method to MethodDec.Method
)

val typeHash = mapOf(
    Token.Int to Type.Int,
    Token.Char to Type.Char,
    Token.Boolean to Type.Boolean
)

val voidOrTypeHash = mapOf(
    Token.Int to VoidOrType._Type(Type.Int),
    Token.Char to VoidOrType._Type(Type.Char),
    Token.Boolean to VoidOrType._Type(Type.Boolean),
    Token.Void to VoidOrType.Void
)

val classVarDecHash = mapOf(
    Token.Static to _ClassVarDec.Static,
    Token.Field to _ClassVarDec.Field
)

enum class Paren {
    Left, Right
}

enum class Op {
    Plus, Minus, Asterisk, Slash, And, Pipe, LessThan, GreaterThan, Equal
}

enum class UnaryOp {
    Minus, Tilde
}