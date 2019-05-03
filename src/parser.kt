data class Statements(val statements: List<Stmt>)

sealed class Stmt {
    data class Let(val stmt: LetStatement) : Stmt()
    data class If(val stmt: IfStatement) : Stmt()
    data class While(val stmt: WhileStatement) : Stmt()
}

data class LetStatement(val varName: Term.VarName, val index: Expression?, val exp: Expression)
data class IfStatement(val expression: Expression, val ifStmts: Statements, val elseStmts: Statements)
data class WhileStatement(val expression: Expression, val statements: Statements)

sealed class Term {
    data class Const(val const: C): Term()
    data class VarName(val name: String) : Term()
    data class SubroutineCall(val call: SubroutineCall): Term()
}

enum class Op {
    Plus, Minus, Asterisk, Slash, And, Pipe, LessThan, GreaterThan, Equal
}

enum class Paren {
    LeftParen, RightParen
}

sealed class ExpElm {
    data class _Term(val term: Term) : ExpElm()
    data class _Op(val op: Op) : ExpElm()
    data class _Paren(val paren: Paren) : ExpElm()
    data class _Expression(val exp: Expression) : ExpElm()
}

data class Expression(val expElms: List<ExpElm>)

data class ExpressionList(val expList: List<Expression>)

sealed class C {
    data class IntC(val const: Int) : C()
    data class StrC(val const: String) : C()
    data class KeyC(val const: Keyword) : C()
}

data class ClassName(val name: String)
data class VarName(val name: String)
data class SubroutineName(val name: String)

data class SubroutineCall(
    val subroutineName: SubroutineName,
    val expList: ExpressionList,
    val ClassName: ClassName?,
    val varName: VarName?
)

enum class Keyword {
    True, False, Null, This
}

fun parseExpression(tokens: List<Token>): Expression {
    return Expression(parseExpressionSub(tokens, listOf()).second)
}

fun parseExpressionSub(tokens: List<Token>, acm: List<ExpElm>): Pair<List<Token>, List<ExpElm>> {
    if (tokens.count() == 0) {
        return tokens to acm
    }
    val firstToken = tokens[0]
    val restTokens = tokens.slice(1 until tokens.count())
    when (firstToken) {
        is Token.LParen -> {
            val (restTkns, restAcm) = parseExpressionSub(restTokens, listOf())
            val firstOfRest = restTkns[0]
            if (firstOfRest == Token.RParen) {
                val leftParen = ExpElm._Paren(Paren.LeftParen)
                val rightParen = ExpElm._Paren(Paren.RightParen)
                val restRestTkns = restTkns.slice(1 until restTkns.count())
                val newAcm = acm + leftParen + ExpElm._Expression(Expression(restAcm)) + rightParen
                return parseExpressionSub(restRestTkns, newAcm)
            } else {
                throw Error("開きカッコに対して閉じカッコがない")
            }
        }
        is Token.RParen -> {
            return tokens to acm
        }
        is Token.Identifier -> {
            val rawTerm = Term.VarName(firstToken.name)
            val term = ExpElm._Term(rawTerm)
            return parseExpressionSub(restTokens, acm + term)
        }
        is Token.IntegerConst -> {
            val rawTerm = Term.Const(C.IntC(firstToken.num))
            val term = ExpElm._Term(rawTerm)
            return parseExpressionSub(restTokens, acm + term)
        }
        is Token.StringConst -> {
            val rawTerm = Term.Const(C.StrC(firstToken.string))
            val term = ExpElm._Term(rawTerm)
            return parseExpressionSub(restTokens, acm + term)
        }
        is Token.Plus -> {
            val rawOp = Op.Plus
            val op = ExpElm._Op(rawOp)
            return parseExpressionSub(restTokens, acm + op)
        }
        is Token.Minus -> {
            val rawOp = Op.Minus
            val op = ExpElm._Op(rawOp)
            return parseExpressionSub(restTokens, acm + op)
        }
        is Token.True -> {
            val rawTerm = Term.Const(C.KeyC(Keyword.True))
            val term = ExpElm._Term(rawTerm)
            return parseExpressionSub(restTokens, acm + term)
        }
        is Token.False -> {
            val rawTerm = Term.Const(C.KeyC(Keyword.False))
            val term = ExpElm._Term(rawTerm)
            return parseExpressionSub(restTokens, acm + term)
        }
        is Token.Null -> {
            val rawTerm = Term.Const(C.KeyC(Keyword.Null))
            val term = ExpElm._Term(rawTerm)
            return parseExpressionSub(restTokens, acm + term)
        }
        is Token.This -> {
            val rawTerm = Term.Const(C.KeyC(Keyword.This))
            val term = ExpElm._Term(rawTerm)
            return parseExpressionSub(restTokens, acm + term)
        }
        else -> {
            return tokens to acm
        }
    }
}

fun parseStatements(tokens: List<Token>): Statements {
    return Statements(parseStatementsSub(tokens, listOf()).second)
}

fun parseStatementsSub(tokens: List<Token>, acm: List<Stmt>): Pair<List<Token>, List<Stmt>> {
    if (tokens.count() == 0) {
        return tokens to acm
    }
    val firstToken = tokens[0]
    val restTokens = tokens.slice(1 until tokens.count())
    when (firstToken) {
        is Token.If -> {
            val (restTkns, ifStmt, _Acm) = parseIfStatementSub(tokens, listOf(), null, listOf(), listOf())
            return parseStatementsSub(restTkns, acm + Stmt.If(ifStmt))
        }
        is Token.Let -> {
            val (restTkns, letStmt) = parseLetStatementSub(tokens, null, null, null)
            return parseStatementsSub(restTkns, acm + Stmt.Let(letStmt))
        }
        is Token.While -> {
            val (restTkns, whileStmts) = parseWhileStatementSub(tokens, null, listOf())
            return parseStatementsSub(restTkns, acm + Stmt.While(whileStmts))
        }
        is Token.LCurlyBrace -> {
            return parseStatementsSub(restTokens, acm)
        }
        is Token.RCurlyBrace -> {
            return tokens to acm
        }
        else -> {
            return tokens to acm
        }
    }
}

fun parseLetStatementSub(
    tokens: List<Token>,
    varName: Term.VarName?,
    index: Expression?,
    exp: Expression?
): Pair<List<Token>, LetStatement> {
    val firstToken = tokens[0]
    val restTokens = tokens.slice(1 until tokens.count())
    when (firstToken) {
        is Token.Let -> {
            return parseLetStatementSub(restTokens, varName, index, exp)
        }
        is Token.Identifier -> {
            return parseLetStatementSub(restTokens, Term.VarName(firstToken.name), index, exp)
        }
        is Token.Equal -> {
            return parseLetStatementSub(restTokens, varName, index, exp)
        }
        is Token.LSquareBracket -> {
            val (restTokens2, indexExperession) = parseExpressionSub(restTokens, listOf())
            return parseLetStatementSub(restTokens2, varName, Expression(indexExperession), exp)
        }
        is Token.RSquareBracket -> {
            return parseLetStatementSub(restTokens, varName, index, exp)
        }
        else -> {
            val (restTokens2, expression) = parseExpressionSub(tokens, listOf())
            varName ?: throw Error("letのパース: 左辺がない状態で右辺が呼ばれている")
            return restTokens2 to LetStatement(varName, index, Expression(expression))
        }
    }
}


fun parseWhileStatementSub(
    tokens: List<Token>,
    exp: Expression?,
    stmts: List<Stmt>
): Pair<List<Token>, WhileStatement> {
    if (tokens.count() == 0) {
        exp ?: throw Error("whileのパース: expがnull $stmts")
        val whileStatement = WhileStatement(exp, Statements(stmts))
        return tokens to whileStatement
    }
    val firstToken = tokens[0]
    val restTokens = tokens.slice(1 until tokens.count())

    when (firstToken) {
        is Token.While -> {
            return parseWhileStatementSub(restTokens, exp, stmts)
        }
        is Token.LParen -> {
            val (restTkns, expression) = parseExpressionSub(restTokens, listOf())
            return parseWhileStatementSub(restTkns, Expression(expression), stmts)
        }
        is Token.RParen -> {
            return parseWhileStatementSub(restTokens, exp, stmts)
        }
        is Token.LCurlyBrace, Token.RCurlyBrace -> {
            val (restTkns, stmtsAcm) = parseStatementsSub(restTokens, listOf())
            return parseWhileStatementSub(restTkns, exp, stmts + stmtsAcm)
        }
        else -> {
            throw Error("while文のパース: 想定外のトークン $firstToken ")
        }
    }
}

fun parseIfStatementSub(
    tokens: List<Token>,
    acm: List<Stmt>,
    exp: Expression?,
    ifStmts: List<Stmt>,
    elseStmts: List<Stmt>
): Triple<List<Token>, IfStatement, List<Stmt>> {
    if (tokens.count() == 0) {
        exp ?: throw Error("ifのパース: expがnull $acm")
        val ifStatement = IfStatement(exp, Statements(ifStmts), Statements(elseStmts))
        return Triple(tokens, ifStatement, acm)
    }
    val firstToken = tokens[0]
    val restTokens = tokens.slice(1 until tokens.count())
    when (firstToken) {
        is Token.If -> {
            val (restTkns, newIf, newAcm) = parseIfStatementSub(restTokens, acm, exp, ifStmts, elseStmts)
            return parseIfStatementSub(
                restTkns,
                listOf(),
                newIf.expression,
                newIf.ifStmts.statements + newAcm,
                newIf.elseStmts.statements
            )
        }
        is Token.Else -> {
            val (restTkns, newIf, newAcm) = parseIfStatementSub(restTokens, acm, exp, ifStmts, elseStmts)
            return parseIfStatementSub(
                restTkns,
                listOf(),
                newIf.expression,
                newIf.ifStmts.statements,
                newIf.elseStmts.statements + newAcm
            )
        }
        is Token.LParen -> {
            val (restTkns, expression) = parseExpressionSub(tokens, listOf())
            return parseIfStatementSub(restTkns, listOf(), Expression(expression), ifStmts, elseStmts)
        }
        is Token.RParen -> {
            exp ?: throw Error("if文のパース: expがnull")
            return parseIfStatementSub(restTokens, acm, exp, ifStmts, elseStmts)
        }
        is Token.LCurlyBrace -> {
            val (restTkns, stmts) = parseStatementsSub(restTokens, listOf())
            return parseIfStatementSub(restTkns, acm + stmts, exp, ifStmts, elseStmts)
        }
        is Token.RCurlyBrace -> {
            // 次がelse節なら、parseIf内で処理する
            if (restTokens.count() != 0 && restTokens[0] is Token.Else) {
                exp ?: throw Error("if文のパース: expがnull")
                return Triple(restTokens, IfStatement(exp, Statements(ifStmts), Statements(elseStmts)), acm)
            }
            // else節でないなら、parseStatementsに投げる
            val (restTkns, stmts) = parseStatementsSub(restTokens, listOf())
            return parseIfStatementSub(restTkns, acm + stmts, exp, ifStmts, elseStmts)
        }
        else -> {
            throw Error("if文のパース: 想定外のトークン $firstToken ")
        }
    }
}
