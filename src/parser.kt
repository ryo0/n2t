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

enum class Paren {
    Left, Right
}

enum class Op {
    Plus, Minus, Asterisk, Slash, And, Pipe, LessThan, GreaterThan, Equal
}

enum class UnaryOp {
    Minus, Tilde
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
    val ClassOrVarName: Identifier?
)

enum class Keyword {
    Class, Constructor, Function, Method, Field, Static, Var, Int, Char, Boolean, Void, True, False, Null, This, Let, Do, If, Else
}

fun first(tokens: List<Token>): Token {
    return tokens[0]
}

fun rest(tokens: List<Token>): List<Token> {
    if (tokens.count() < 1) {
        throw Error("restに0個のトークンが渡されました")
    }
    return tokens.slice(1 until tokens.count())
}

fun parseExpression(tokens: List<Token>): Expression {
    return Expression(parseExpressionSub(tokens, listOf()).second)
}

fun parseTerm(tokens: List<Token>, _term: Term?): Pair<List<Token>, Term> {
    if (tokens.count() == 0) {
        _term ?: throw Error("termがnull")
        return tokens to _term
    }
    val firstToken = first(tokens)
    val restTokens = rest(tokens)
    when (firstToken) {
        is Token.LParen -> {
            val (newRestTokens, restAcm) = parseExpressionSub(restTokens, listOf())
            if (newRestTokens.count() == 0) {
                return newRestTokens to Term._Expression(Paren.Left, Expression(restAcm), Paren.Right)
            }
            if (first(newRestTokens) is Token.RParen) {
                val term = Term._Expression(Paren.Left, Expression(restAcm), Paren.Right)
                return rest(newRestTokens) to term
            } else {
                throw Error("開きカッコに対して閉じカッコがない")
            }
        }
        is Token.RParen -> {
            _term ?: throw Error("termがnull")
            return tokens to _term
        }
        is Token.Identifier -> {
            if (tokens.count() > 1) {
                val next = first(restTokens)
                if (next == Token.Dot || next == Token.LParen) {
                    val (newRestTokens, subroutineCall) = parseSubroutineCall(tokens)
                    return newRestTokens to Term._SubroutineCall(subroutineCall)
                } else if (next == Token.LSquareBracket) {
                    val (newRestTokens, arrayAndIndex) = parseArrayAndIndex(tokens)
                    return newRestTokens to arrayAndIndex
                }
            }
            val term = Term.VarName(firstToken.name)
            return restTokens to term
        }
        is Token.Minus, is Token.Tilde -> {
            val op = unaryOpHash[firstToken] ?: throw Error("unaryOpHashに不備があります")
            val (newRestTokens, term) = parseTerm(restTokens, null)
            return newRestTokens to Term.UnaryOpTerm(op, term)
        }
        is Token.IntegerConst -> {
            val term = Term.IntC(firstToken.num)
            return restTokens to term
        }
        is Token.StringConst -> {
            val term = Term.StrC(firstToken.string)
            return restTokens to term
        }
        is Token.True -> {
            val term = Term.KeyC(Keyword.True)
            return restTokens to term
        }
        is Token.False -> {
            val term = Term.KeyC(Keyword.False)
            return restTokens to term
        }
        is Token.Null -> {
            val term = Term.KeyC(Keyword.Null)
            return restTokens to term
        }
        is Token.This -> {
            val term = Term.KeyC(Keyword.This)
            return restTokens to term
        }
        else -> {
            _term ?: throw Error("termがnull")
            return tokens to _term
        }
    }
}

fun parseExpressionSub(tokens: List<Token>, acm: List<ExpElm>): Pair<List<Token>, List<ExpElm>> {
    if (tokens.count() == 0) {
        return tokens to acm
    }
    val firstToken = first(tokens)
    val restTokens = rest(tokens)

    when (firstToken) {
        Token.LParen, is Token.Identifier, is Token.IntegerConst,
        Token.True, Token.False, Token.This, Token.Null, Token.Tilde -> {
            val (newRestTokens, term) = parseTerm(tokens, null)
            return parseExpressionSub(newRestTokens, acm + ExpElm._Term(term))
        }
        Token.Minus -> {
            if (acm.count() == 0) {
                val (newRestTokens, term) = parseTerm(tokens, null)
                return parseExpressionSub(newRestTokens, acm + ExpElm._Term(term))
            } else {
                val rawOp = opHash[firstToken] ?: throw Error("opHashに不備がある $opHash")
                val op = ExpElm._Op(rawOp)
                val (newRestTokens, term) = parseTerm(restTokens, null)
                return parseExpressionSub(newRestTokens, acm + op + ExpElm._Term(term))
            }
        }
        in opHash.keys -> {
            val rawOp = opHash[firstToken] ?: throw Error("opHashに不備がある $opHash")
            val op = ExpElm._Op(rawOp)
            val (newRestTokens, term) = parseTerm(restTokens, null)
            return parseExpressionSub(newRestTokens, acm + op + ExpElm._Term(term))
        }
        is Token.RSquareBracket -> {
            return restTokens to acm
        }
        else -> {
            return tokens to acm
        }

    }
}

fun parseArrayAndIndex(tokens: List<Token>): Pair<List<Token>, Term.ArrayAndIndex> {
    val firstToken = first(tokens) as Token.Identifier
    val restTokens = rest(tokens)
    if (first(restTokens) != Token.LSquareBracket) {
        throw Error("配列なのに[で始まってない")
    }
    val arrayName = firstToken.name
    val (newRestTokens, exp) = parseExpressionSub(rest(restTokens), listOf())
    return newRestTokens to Term.ArrayAndIndex(arrayName, Expression(exp))
}

fun parseDo(tokens: List<Token>): Pair<List<Token>, DoStatement> {
    val restTokens = rest(tokens)
    val (newRestTokens, subroutineCall) = parseSubroutineCall(restTokens)
    return newRestTokens to DoStatement(subroutineCall)
}

fun parseReturn(tokens: List<Token>): Pair<List<Token>, ReturnStatement> {
    val restTokens = rest(tokens)
    if (first(restTokens) == Token.Semicolon) {
        return rest(restTokens) to ReturnStatement(null)
    }
    val (newRestTokens, expression) = parseExpressionSub(restTokens, listOf())
    if (first(newRestTokens) != Token.Semicolon) {
        throw Error("Return文で式の後がセミコロンじゃない: tokens")
    }
    return rest(newRestTokens) to ReturnStatement(Expression(expression))
}


fun parseSubroutineCall(tokens: List<Token>): Pair<List<Token>, SubroutineCall> {
    if (tokens.count() <= 2) {
        throw Error("SubroutineCallのパース: トークンが2つ以下 $tokens")
    }
    val firstToken = first(tokens)
    when (first(rest(tokens))) {
        is Token.LParen -> {
            if (firstToken !is Token.Identifier) {
                throw Error("SubroutineCallのパース: subroutineNameが変数の形式ではない $firstToken")
            }
            val subroutineName = Identifier(firstToken.name)

            val restTokens = rest(rest(tokens))
            if (restTokens.count() == 0) {
                throw Error("SubroutineCallのパース: トークンが少ない $tokens")
            }
            val (newRestTokens, expList) = parseExpressionList(restTokens, listOf())
            return newRestTokens to SubroutineCall(subroutineName, expList, null)
        }
        is Token.Dot -> {
            if (firstToken !is Token.Identifier) {
                throw Error("SubroutineCallのパース: クラス/変数名が変数の形式ではない $firstToken")
            }
            val classOrVarName = Identifier(firstToken.name)
            val thirdToken = tokens[2] as Token.Identifier

            val subroutineName = Identifier(thirdToken.name)

            val restTokens = rest(rest(rest(tokens)))
            if (restTokens.count() == 0) {
                throw Error("SubroutineCallのパース: トークンが少ない $tokens")
            }
            val (newRestTokens, expList) = parseExpressionList(restTokens, listOf())

            return newRestTokens to SubroutineCall(subroutineName, expList, classOrVarName)
        }
        else -> {
            throw Error("SubroutineCallのパース: 2つ目のトークンが(でも.でもない $tokens")
        }
    }
}

fun parseExpressionList(tokens: List<Token>, acm: List<Expression>): Pair<List<Token>, ExpressionList> {
    if (tokens.count() == 0) {
        return tokens to ExpressionList(acm)
    }
    val restTokens = rest(tokens)
    when (first(tokens)) {
        is Token.Comma, Token.LParen -> {
            return parseExpressionList(restTokens, acm)
        }
        is Token.RParen -> {
            return restTokens to ExpressionList(acm)
        }
        else -> {
            val (newRestTokens, exps) = parseExpressionSub(tokens, listOf())
            return parseExpressionList(newRestTokens, acm + Expression(exps))
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
    val firstToken = first(tokens)
    val restTokens = rest(tokens)
    when (firstToken) {
        is Token.If -> {
            val (newRestTokens, ifStmt, _) = parseIfStatementSub(tokens, listOf(), null, listOf(), listOf())
            return parseStatementsSub(newRestTokens, acm + Stmt.If(ifStmt))
        }
        is Token.Let -> {
            val (newRestTokens, letStmt) = parseLetStatementSub(tokens, null, null, null)
            return parseStatementsSub(newRestTokens, acm + Stmt.Let(letStmt))
        }
        is Token.While -> {
            val (newRestTokens, whileStmts) = parseWhileStatementSub(tokens, null, listOf())
            return parseStatementsSub(newRestTokens, acm + Stmt.While(whileStmts))
        }
        is Token.Do -> {
            val (newRestTokens, doStmt) = parseDo(tokens)
            return parseStatementsSub(newRestTokens, acm + Stmt.Do(doStmt))
        }
        is Token.Return -> {
            val (newRestTokens, returnStmt) = parseReturn(tokens)
            return parseStatementsSub(newRestTokens, acm + Stmt.Return(returnStmt))
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
    val firstToken = first(tokens)
    val restTokens = rest(tokens)
    when (firstToken) {
        is Token.Let -> {
            return parseLetStatementSub(restTokens, varName, index, exp)
        }
        is Token.Identifier -> {
            return parseLetStatementSub(restTokens, Term.VarName(firstToken.name), index, exp)
        }
        is Token.Equal -> {
            val (newRestTokens, expression) = parseExpressionSub(restTokens, listOf())
            varName ?: throw Error("letのパース: 左辺がない状態で右辺が呼ばれている")
            return newRestTokens to LetStatement(varName, index, Expression(expression))
        }
        is Token.LSquareBracket -> {
            val (newRestTokens, indexExperession) = parseExpressionSub(restTokens, listOf())
            return parseLetStatementSub(newRestTokens, varName, Expression(indexExperession), exp)
        }
        is Token.RSquareBracket -> {
            return parseLetStatementSub(restTokens, varName, index, exp)
        }
        else -> {
            val (newRestTokens, expression) = parseExpressionSub(tokens, listOf())
            varName ?: throw Error("letのパース: 左辺がない状態で右辺が呼ばれている")
            return newRestTokens to LetStatement(varName, index, Expression(expression))
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
    val firstToken = first(tokens)
    val restTokens = rest(tokens)

    when (firstToken) {
        is Token.While -> {
            return parseWhileStatementSub(restTokens, exp, stmts)
        }
        is Token.LParen -> {
            val (newRestTokens, expression) = parseExpressionSub(restTokens, listOf())
            return parseWhileStatementSub(newRestTokens, Expression(expression), stmts)
        }
        is Token.RParen -> {
            return parseWhileStatementSub(restTokens, exp, stmts)
        }
        is Token.LCurlyBrace, Token.RCurlyBrace -> {
            val (newRestTokens, stmtsAcm) = parseStatementsSub(restTokens, listOf())
            return parseWhileStatementSub(newRestTokens, exp, stmts + stmtsAcm)
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
    val firstToken = first(tokens)
    val restTokens = rest(tokens)
    when (firstToken) {
        is Token.If -> {
            val (newRestTokens, newIf, newAcm) = parseIfStatementSub(restTokens, acm, exp, ifStmts, elseStmts)
            return parseIfStatementSub(
                newRestTokens,
                listOf(),
                newIf.expression,
                newIf.ifStmts.statements + newAcm,
                newIf.elseStmts.statements
            )
        }
        is Token.Else -> {
            val (newRestTokens, newIf, newAcm) = parseIfStatementSub(restTokens, acm, exp, ifStmts, elseStmts)
            return parseIfStatementSub(
                newRestTokens,
                listOf(),
                newIf.expression,
                newIf.ifStmts.statements,
                newIf.elseStmts.statements + newAcm
            )
        }
        is Token.LParen -> {
            val (newRestTokens, expression) = parseExpressionSub(restTokens, listOf())
            return parseIfStatementSub(newRestTokens, listOf(), Expression(expression), ifStmts, elseStmts)
        }
        is Token.RParen -> {
            exp ?: throw Error("if文のパース: expがnull")
            return parseIfStatementSub(restTokens, acm, exp, ifStmts, elseStmts)
        }
        is Token.LCurlyBrace -> {
            val (newRestTokens, stmts) = parseStatementsSub(restTokens, listOf())
            return parseIfStatementSub(newRestTokens, acm + stmts, exp, ifStmts, elseStmts)
        }
        is Token.RCurlyBrace -> {
            // 次がelse節なら、parseIf内で処理する
            if (restTokens.count() != 0 && first(restTokens) is Token.Else) {
                exp ?: throw Error("if文のパース: expがnull")
                return Triple(restTokens, IfStatement(exp, Statements(ifStmts), Statements(elseStmts)), acm)
            }
            // else節でないなら、parseStatementsに投げる
            val (newRestTokens, stmts) = parseStatementsSub(restTokens, listOf())
            return parseIfStatementSub(newRestTokens, acm + stmts, exp, ifStmts, elseStmts)
        }
        else -> {
            throw Error("if文のパース: 想定外のトークン $firstToken ")
        }
    }
}
