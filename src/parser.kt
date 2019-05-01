import sun.tools.java.Identifier
import javax.swing.plaf.nimbus.State

data class Statements(val statements: List<Stmt>)

sealed class Stmt {
    data class Let(val stmt: LetStatement) : Stmt()
    data class If(val stmt: IfStatement) : Stmt()
}

data class LetStatement(val varName: Constant.VarName, val exp: Expression)
data class IfStatement(val expression: Expression, val ifStmts: Statements, val elseStmts: Statements?)

data class Term(val constant: Constant)

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

sealed class Constant {
    data class IntCons(val const: Int) : Constant()
    data class StrCons(val const: String) : Constant()
    data class KeyCons(val const: Keyword) : Constant()
    data class VarName(val const: String) : Constant()
}

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
            val rawTerm = Term(Constant.VarName(firstToken.name))
            val term = ExpElm._Term(rawTerm)
            return parseExpressionSub(restTokens, acm + term)
        }
        is Token.IntegerConst -> {
            val rawTerm = Term(Constant.IntCons(firstToken.num))
            val term = ExpElm._Term(rawTerm)
            return parseExpressionSub(restTokens, acm + term)
        }
        is Token.StringConst -> {
            val rawTerm = Term(Constant.StrCons(firstToken.string))
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
            val rawTerm = Term(Constant.KeyCons(Keyword.True))
            val term = ExpElm._Term(rawTerm)
            return parseExpressionSub(restTokens, acm + term)
        }
        is Token.False -> {
            val rawTerm = Term(Constant.KeyCons(Keyword.False))
            val term = ExpElm._Term(rawTerm)
            return parseExpressionSub(restTokens, acm + term)
        }
        is Token.Null -> {
            val rawTerm = Term(Constant.KeyCons(Keyword.Null))
            val term = ExpElm._Term(rawTerm)
            return parseExpressionSub(restTokens, acm + term)
        }
        is Token.This -> {
            val rawTerm = Term(Constant.KeyCons(Keyword.This))
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
            val (restTkns, ifStmt) = parseIfStatementSub(tokens, listOf(), listOf(), null)
            return parseStatementsSub(restTkns, acm + Stmt.If(ifStmt))
        }
        is Token.LCurlyBrace -> {
            return parseStatementsSub(restTokens, acm)
        }
        is Token.Let -> {
            val (restTkns, letStmt) = parseLetStatementSub(tokens, null, null)
            return parseStatementsSub(restTkns, acm + Stmt.Let(letStmt))
        }
        else -> {
            return tokens to acm
        }
    }
}

//fun parseLetStatement(tokens: List<Token>) :LetStatement {
//
//}

fun parseLetStatementSub(tokens: List<Token>, varName: Constant.VarName?, exp: Expression?): Pair<List<Token> , LetStatement>{
    val firstToken = tokens[0]
    val restTokens = tokens.slice(1 until tokens.count())
    when (firstToken) {
        is Token.Let -> {
            return parseLetStatementSub(restTokens, varName, exp)
        }
        is Token.Identifier -> {
            return parseLetStatementSub(restTokens, Constant.VarName(firstToken.name), exp)
        }
        is Token.Equal -> {
            return parseLetStatementSub(restTokens, varName, exp)
        }
        else -> {
            val (restTokens2, expression) = parseExpressionSub(tokens, listOf())
            varName ?: throw Error("letのパース: 左辺がない状態で右辺が呼ばれている")
            return restTokens2 to LetStatement(varName, Expression(expression))
        }
    }
}

// ボツネタ
//fun parseIfStatementSub(tokens: List<Token>): IfStatement {
//    val firstToken = tokens[0]
//
//    if (firstToken != Token.If) throw Error("Ifから始まらないIf文")
//
//    val leftParen = tokens[1]
//    if (leftParen != Token.LParen) throw Error("If文の後に ( がない")
//
//    val rightParenIndex = findClosingParenIndex(1, tokens)
//    if (rightParenIndex == -1) throw Error("If文の後のカッコが閉じてない")
//
//    val tokensInIfExp = tokens.slice(1 until rightParenIndex)
//    val exp = parseExpression(tokensInIfExp)
//
//    val token = tokens[rightParenIndex + 1]
//    if (token != Token.LCurlyBrace) throw Error("If文の条件の後にIf節がない")
//
//    val rightCurlyBraceIndex =
//        findClosingParenIndex(rightParenIndex + 1, tokens)
//    val tokensInIfStatements = tokens.slice(rightParenIndex + 1 until rightCurlyBraceIndex + 1)
//    val ifStmts = parseStatements(tokensInIfStatements)
//
//    if (rightCurlyBraceIndex + 1 >= tokens.count()) {
//        return IfStatement(expression = exp, ifStmts = ifStmts, elseStmts = null)
//    }
//    val elseToken = tokens[rightCurlyBraceIndex + 1]
//    val leftCurlyBrace2 = tokens[rightCurlyBraceIndex + 2]
//    if (elseToken is Token.Else && leftCurlyBrace2 is Token.LCurlyBrace){
//        val rightCurlyBrace2Index = findClosingParenIndex(rightCurlyBraceIndex + 2, tokens)
//        val tokensInElseStatements = tokens.slice(rightCurlyBraceIndex + 1 until rightCurlyBrace2Index)
//        val elseStmts = parseStatements(tokensInElseStatements)
//        return IfStatement(expression = exp, ifStmts = ifStmts, elseStmts = elseStmts)
//    }
//}

//
fun parseIfStatement(tokens: List<Token>): IfStatement {
    return parseIfStatementSub(tokens, listOf(), listOf(), null).second
}

fun parseIfStatementSub(
    tokens: List<Token>,
    ifStmts: List<Stmt>,
    elseStmts: List<Stmt>,
    exp: Expression?
): Pair<List<Token>, IfStatement> {
    if (tokens.count() == 0) {
        exp ?: throw Error("if文のパース: expがnull")
        val ifStmt = IfStatement(expression = exp, ifStmts = Statements(ifStmts), elseStmts = Statements(elseStmts))
        return tokens to ifStmt
    }
    val firstToken = tokens[0]
    val restTokens = tokens.slice(1 until tokens.count())
    when (firstToken) {
        is Token.If -> {
            val (restTkns, expression) = parseExpressionSub(restTokens, listOf())
            val (restRestTkns, stmts) = parseStatementsSub(restTkns, listOf())
            return parseIfStatementSub(restRestTkns, ifStmts + stmts, elseStmts, Expression(expression))
        }
        is Token.Else -> {
            val (restTkns, stmts) = parseStatementsSub(restTokens, listOf())
            return parseIfStatementSub(restTkns, ifStmts, elseStmts + stmts, exp)
        }
        is Token.LCurlyBrace -> {
            val (restTkns, stmts) = parseStatementsSub(restTokens, listOf())
            return parseIfStatementSub(restTkns, ifStmts + stmts, elseStmts, exp)
        }
        is Token.RCurlyBrace -> {
            return parseIfStatementSub(restTokens, ifStmts, elseStmts, exp)
        }
        else -> {
            exp ?: throw Error("if文のパース: expがnull")
            val ifStmt = IfStatement(expression = exp, ifStmts = Statements(ifStmts), elseStmts = Statements(elseStmts))
            return tokens to ifStmt
        }
    }
}

fun findClosingParenIndex(startIndex: Int, tokens: List<Token>): Int {
    return findClosingParenIndexSub(startIndex, tokens.slice(startIndex until tokens.count()))
}

fun findClosingParenIndexSub(startIndex: Int, tokens: List<Token>): Int {
    val openParen = tokens[startIndex]
    val closeParen = ParenHash[openParen] ?: throw Error("対になるカッコがParenHashにない")
    var parenCounter = 0
    val tokensFromStartIndex = tokens.slice(startIndex until tokens.count())
    tokensFromStartIndex.forEachIndexed { index, token ->
        if (token == openParen) {
            parenCounter += 1
        } else if (token == closeParen) {
            parenCounter -= 1
        }
        if (parenCounter == 0) {
            return index + startIndex
        }
    }
    return -1
}