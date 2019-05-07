data class Class(val name: String, val varDec: List<ClassVarDec>, val subroutineDec: List<SubroutineDec>)

data class ClassVarDec(val varDec: _ClassVarDec, val type: Type, val varName: List<String>)

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

fun first(tokens: List<Token>): Token {
    if (tokens.count() < 1) {
        throw Error("firstに0個のトークンが渡されました")
    }
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
        Token.LParen, is Token.Identifier, is Token.IntegerConst, is Token.StringConst,
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
    return rest(newRestTokens) to DoStatement(subroutineCall)
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
            val (newRestTokens, expList) = parseExpressionList(restTokens, listOf(), listOf())
            return newRestTokens to SubroutineCall(subroutineName, expList, null)
        }
        is Token.Dot -> {
            if (firstToken !is Token.Identifier) {
                throw Error("SubroutineCallのパース: クラス/変数名が変数の形式ではない $firstToken")
            }
            val classOrVarName = Identifier(firstToken.name)
            val thirdToken = tokens[2] as Token.Identifier

            val subroutineName = Identifier(thirdToken.name)

            val restTokens = rest(rest(rest(rest(tokens))))
            if (restTokens.count() == 0) {
                throw Error("SubroutineCallのパース: トークンが少ない $tokens")
            }
            val (newRestTokens, expList) = parseExpressionList(restTokens, listOf(), listOf())

            return newRestTokens to SubroutineCall(subroutineName, expList, classOrVarName)
        }
        else -> {
            throw Error("SubroutineCallのパース: 2つ目のトークンが(でも.でもない $tokens")
        }
    }
}

fun parseExpressionList(
    tokens: List<Token>,
    acmList: List<ExpElm>,
    acmListList: List<List<ExpElm>>
): Pair<List<Token>, ExpressionList> {
    if (tokens.count() == 0) {
        if (acmListList.count() == 0) {
            return tokens to ExpressionList(listOf())
        }
        val finalList = acmListList + listOf(acmList)
        return tokens to ExpressionList(finalList.map { Expression(it) })
    }
    val restTokens = rest(tokens)
    when (first(tokens)) {
        is Token.Comma -> {
            return parseExpressionList(restTokens, listOf(), acmListList + listOf(acmList))
        }
        is Token.LParen -> {
            val (newRestTokens, exp) = parseExpressionSub(restTokens, listOf())
            if (first(newRestTokens) is Token.RParen) {
                val term = Term._Expression(Paren.Left, Expression(exp), Paren.Right)
                return parseExpressionList(rest(newRestTokens), acmList + listOf(ExpElm._Term(term)), acmListList)
            } else {
                throw Error("開きカッコに対して閉じカッコがない")
            }
        }
        is Token.RParen -> {
            if (acmListList.count() == 0 && acmList.count() == 0) {
                return restTokens to ExpressionList(listOf())
            }
            val finalList = acmListList + listOf(acmList)
            return restTokens to ExpressionList(finalList.map { Expression(it) })
        }
        else -> {
            val (newRestTokens, exp) = parseExpressionSub(tokens, acmList)
            return parseExpressionList(newRestTokens, exp, acmListList)
        }
    }
}

fun parseStatements(tokens: List<Token>): Statements {
    return Statements(parseStatementsSub(tokens, listOf()).second)
}

fun parseIfStatement(tokens: List<Token>): Pair<List<Token>, Stmt> {
    val (newRestTokens, ifStmt, _) = parseIfStatementSub(tokens, listOf(), null, listOf(), listOf())
    return newRestTokens to Stmt.If(ifStmt)
}

fun parseLetStatement(tokens: List<Token>): Pair<List<Token>, Stmt> {
    val (newRestTokens, letStmt) = parseLetStatementSub(tokens, null, null, null)
    return newRestTokens to Stmt.Let(letStmt)
}

fun parseWhileStatement(tokens: List<Token>): Pair<List<Token>, Stmt> {
    val (newRestTokens, whileStmts) = parseWhileStatementSub(tokens, null, listOf())
    return newRestTokens to Stmt.While(whileStmts)
}


fun parseStatementsSub(tokens: List<Token>, acm: List<Stmt>): Pair<List<Token>, List<Stmt>> {
    if (tokens.count() == 0) {
        return tokens to acm
    }
    val firstToken = first(tokens)
    val restTokens = rest(tokens)
    when (firstToken) {
        is Token.If -> {
            val (newRestTokens, ifStmt) = parseIfStatement(tokens)
            return parseStatementsSub(newRestTokens, acm + ifStmt)
        }
        is Token.Let -> {
            val (newRestTokens, letStmt) = parseLetStatement(tokens)
            return parseStatementsSub(newRestTokens, acm + letStmt)
        }
        is Token.While -> {
            val (newRestTokens, whileStmts) = parseWhileStatement(tokens)
            return parseStatementsSub(newRestTokens, acm + whileStmts)
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
            return restTokens to acm
        }
        else -> {
            throw Error("文のパース: 想定外のトークン $firstToken, $tokens")
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
            return rest(newRestTokens) to LetStatement(varName, index, Expression(expression))
        }
        is Token.LSquareBracket -> {
            val (newRestTokens, indexExperession) = parseExpressionSub(restTokens, listOf())
            return parseLetStatementSub(newRestTokens, varName, Expression(indexExperession), exp)
        }
        is Token.RSquareBracket -> {
            return parseLetStatementSub(restTokens, varName, index, exp)
        }
        else -> {
            throw Error("let文のパース: 予期しないトークン $firstToken")
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
        is Token.LCurlyBrace -> {
            val (newRestTokens, stmtsAcm) = parseStatementsSub(restTokens, listOf())
            return parseWhileStatementSub(newRestTokens, exp, stmts + stmtsAcm)
        }
        is Token.RCurlyBrace -> {
            exp ?: throw Error("whileのパース: expがnull $stmts")
            val whileStatement = WhileStatement(exp, Statements(stmts))
            return tokens to whileStatement
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
            exp ?: throw Error("if文のパース: expがnull")
            val (newRestTokens, stmts) = parseStatementsSub(restTokens, acm)
            return Triple(newRestTokens, IfStatement(exp, Statements(ifStmts), Statements(elseStmts + stmts)), acm)
        }
        is Token.LParen -> {
            val (newRestTokens, expression) = parseExpressionSub(restTokens, listOf())
            return parseIfStatementSub(newRestTokens, acm, Expression(expression), ifStmts, elseStmts)
        }
        is Token.RParen -> {
            exp ?: throw Error("if文のパース: expがnull")
            val (newRestTokens, stmts) = parseStatementsSub(restTokens, acm)
            return parseIfStatementSub(newRestTokens, acm, exp, ifStmts + stmts, elseStmts)
        }
        is Token.RCurlyBrace -> {
            exp ?: throw Error("if文のパース: expがnull")
            return Triple(tokens, IfStatement(exp, Statements(ifStmts), Statements(elseStmts)), acm)
        }
        else -> {
            exp ?: throw Error("if文のパース: expがnull")
            return Triple(tokens, IfStatement(exp, Statements(ifStmts), Statements(elseStmts)), acm)
        }
    }
}

fun parseClass(tokens: List<Token>): Class {
    val className = tokens[1] as Token.Identifier
    return parseClassSub(rest(rest(rest(tokens))), className.name, listOf(), listOf()).second

}

fun parseClassSub(
    tokens: List<Token>,
    className: String,
    classVarDecs: List<ClassVarDec>,
    subDecs: List<SubroutineDec>
): Pair<List<Token>, Class> {
    if (tokens.count() == 0) {
        return tokens to Class(className, classVarDecs, subDecs)
    }
    val firstToken = first(tokens)
    val restTokens = rest(tokens)
    when (firstToken) {
        Token.Static, Token.Field -> {
            val (newRestTokens, classVarDec) = parseClassVarDec(tokens)
            return parseClassSub(newRestTokens, className, classVarDecs + classVarDec, subDecs)
        }
        Token.Constructor, Token.Function, Token.Method -> {
            val (newRestTokens, subDec) = parseSubroutineDec(tokens)
            return parseClassSub(newRestTokens, className, classVarDecs, subDecs + subDec)
        }
        else -> {
            return restTokens to Class(className, classVarDecs, subDecs)
        }
    }
}

fun parseClassVarDec(tokens: List<Token>): Pair<List<Token>, ClassVarDec> {
    val classVarDec = classVarDecHash[first(tokens)] ?: throw Error("classVarDecHashか${first(tokens)}が異常")
    val secondToken = first(rest(tokens))
    val type = if(secondToken is Token.Identifier) {
        Type.ClassName(secondToken.name)
    } else {
        typeHash[first(rest(tokens))] ?: throw Error("typeHashか${first(rest(tokens))}が異常")
    }
    return parseClassVarDecSub(rest(rest(tokens)), classVarDec, type, listOf())
}

fun parseClassVarDecSub(
    tokens: List<Token>,
    classVarDec: _ClassVarDec,
    type: Type,
    vars: List<String>
): Pair<List<Token>, ClassVarDec> {
    val firstToken = first(tokens)
    val restTokens = rest(tokens)
    when (firstToken) {
        is Token.Identifier -> {
            return parseClassVarDecSub(restTokens, classVarDec, type, vars + firstToken.name)
        }
        Token.Comma -> {
            return parseClassVarDecSub(restTokens, classVarDec, type, vars)
        }
        Token.Semicolon -> {
            return restTokens to ClassVarDec(classVarDec, type, vars)
        }
        else -> {
            throw Error("予期しないトークン $tokens")
        }
    }
}

fun parseSubroutineDec(tokens: List<Token>): Pair<List<Token>, SubroutineDec> {
    val subDec = subDecHash[tokens[0]] ?: throw Error("SubroutineDecのパース: このトークンがおかしい ${tokens[0]}")
    val secondToken = tokens[1]
    val subroutineType = if (secondToken is Token.Identifier) {
        VoidOrType._Type(Type.ClassName(secondToken.name))
    } else {
        voidOrTypeHash[secondToken] ?: throw Error("SubroutineDecのパース: このトークンがおかしい ${secondToken}")
    }
    val subroutineName = tokens[2] as Token.Identifier
    val (restTokens, paramList) = parseParameterListSub(tokens.slice(4 until tokens.count()), listOf())
    val (newRestTokens, subroutineBody) = parseSubroutineBodySub(restTokens, listOf(), null)
    return newRestTokens to SubroutineDec(subDec, subroutineType, subroutineName.name, paramList, subroutineBody)
}

fun parseParameterListSub(tokens: List<Token>, params: List<Parameter>): Pair<List<Token>, ParameterList> {
    val firstToken = first(tokens)
    val restTokens = rest(tokens)
    when (firstToken) {
        Token.Int, Token.Char, Token.Boolean -> {
            val type = typeHash[firstToken] ?: throw Error("typeHashに不備があります")
            val secondToken = first(restTokens) as Token.Identifier
            val param = Parameter(type, secondToken.name)
            return parseParameterListSub(rest(restTokens), params + param)
        }
        is Token.Identifier -> {
            val type = Type.ClassName(firstToken.name)
            val secondToken = first(restTokens) as Token.Identifier
            val param = Parameter(type, secondToken.name)
            return parseParameterListSub(rest(restTokens), params + param)
        }
        Token.Comma -> {
            return parseParameterListSub(restTokens, params)
        }
        is Token.RParen -> {
            return restTokens to ParameterList(params)
        }

        else -> {
            throw Error("ParameterListのパース: 予期しないトークン $firstToken")
        }
    }
}

fun parseSubroutineBody(tokens: List<Token>): SubroutineBody {
    return parseSubroutineBodySub(tokens, listOf(), null).second
}

fun parseSubroutineBodySub(
    tokens: List<Token>,
    varDecs: List<VarDec>,
    statements: Statements?
): Pair<List<Token>, SubroutineBody> {
    val firstToken = first(tokens)
    val restTokens = rest(tokens)
    when (firstToken) {
        Token.LCurlyBrace -> {
            return parseSubroutineBodySub(restTokens, varDecs, statements)
        }
        Token.Var -> {
            val (newRestTokens, varDec) = parseVarDecSub(tokens, null, listOf())
            return parseSubroutineBodySub(newRestTokens, varDecs + varDec, statements)
        }
        else -> {
            val (newRestTokens, stmts) = parseStatementsSub(tokens, listOf())
            return newRestTokens to SubroutineBody(varDecs, Statements(stmts))
        }
    }
}

fun parseVarDec(tokens: List<Token>): VarDec {
    return parseVarDecSub(tokens, null, listOf()).second
}

fun parseVarDecSub(tokens: List<Token>, type: Type?, vars: List<String>): Pair<List<Token>, VarDec> {
    val firstToken = first(tokens)
    val restTokens = rest(tokens)
    when (firstToken) {
        Token.Var -> {
            return parseVarDecSub(restTokens, type, vars)
        }
        Token.Int, Token.Char, Token.Boolean -> {
            val _type = typeHash[firstToken] ?: throw Error("typeHashに不備があります")
            return parseVarDecSub(restTokens, _type, vars)
        }
        is Token.Identifier -> {
            if (type == null) {
                val _type = Type.ClassName(firstToken.name)
                return parseVarDecSub(restTokens, _type, vars)
            } else {
                return parseVarDecSub(restTokens, type, vars + firstToken.name)
            }
        }
        Token.Comma -> {
            return parseVarDecSub(restTokens, type, vars)
        }
        Token.Semicolon -> {
            type ?: throw Error("typeがnull")
            return restTokens to VarDec(type, vars)
        }
        else -> {
            throw Error("VarDecのパース:予期しないトークン $firstToken")
        }
    }
}