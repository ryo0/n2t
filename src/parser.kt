data class Statements(val statements: List<Stmt>)

sealed class Stmt {
    data class Let(val stmt: LetStatement) : Stmt()
    data class If(val stmt: IfStatement) : Stmt()
}

data class LetStatement(val varName: String, val index: Expression?, val exp: Expression)
data class IfStatement(val expression: Expression, val ifStmt: Statements, val elseStmt: Statements?)

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

//fun parse(tokens: List<Token>): Statements {
//    val token = tokens[0]
//    when (token) {
//        is Token.If -> {
//
//        }
//        is Token.Let -> {
//
//        }
//
//    }
//}

fun parseExpression(tokens: List<Token>, acm: List<ExpElm>): Expression {
    if (tokens.count() == 0) {
        return Expression(acm)
    }
    val firstToken = tokens[0]
    val restToken = tokens.slice(1 until tokens.count())
    when (firstToken) {
        is Token.LParen -> {
            val leftParen = ExpElm._Paren(Paren.LeftParen)
            val rightParen = ExpElm._Paren(Paren.RightParen)
            return Expression( (acm + leftParen) + listOf(ExpElm._Expression(parseExpression(restToken, listOf())), rightParen))
        }
        is Token.RParen -> {
            return parseExpression(restToken, acm)
        }
        is Token.Identifier -> {
            val rawTerm = Term(Constant.VarName(firstToken.name))
            val term = ExpElm._Term(rawTerm)
            return parseExpression(restToken, acm + term)
        }
        is Token.IntegerConst -> {
            val rawTerm = Term(Constant.IntCons(firstToken.num))
            val term = ExpElm._Term(rawTerm)
            return parseExpression(restToken, acm + term)
        }
        is Token.StringConst -> {
            val rawTerm = Term(Constant.StrCons(firstToken.string))
            val term = ExpElm._Term(rawTerm)
            return parseExpression(restToken, acm + term)
        }
        is Token.Plus -> {
            val rawOp = Op.Plus
            val op = ExpElm._Op(rawOp)
            return parseExpression(restToken, acm + op)
        }
        is Token.Minus -> {
            val rawOp = Op.Minus
            val op = ExpElm._Op(rawOp)
            return parseExpression(restToken, acm + op)
        }
        is Token.True -> {
            val rawTerm = Term(Constant.KeyCons(Keyword.True))
            val term = ExpElm._Term(rawTerm)
            return parseExpression(restToken, acm + term)
        }
        is Token.False -> {
            val rawTerm = Term(Constant.KeyCons(Keyword.False))
            val term = ExpElm._Term(rawTerm)
            return parseExpression(restToken, acm + term)
        }
        is Token.Null -> {
            val rawTerm = Term(Constant.KeyCons(Keyword.Null))
            val term = ExpElm._Term(rawTerm)
            return parseExpression(restToken, acm + term)
        }
        is Token.This -> {
            val rawTerm = Term(Constant.KeyCons(Keyword.This))
            val term = ExpElm._Term(rawTerm)
            return parseExpression(restToken, acm + term)
        }
        else
        -> throw Error("まだ想定外@parseExpression: $firstToken")

    }
}
//fun parseIfStatement(tokens: List<Token>): Stmt.If {
//    val expEnd = tokens.indexOfFirst { it is Token.RParen }
//    val exp = tokens.slice(1..expEnd)
//
//}

//fun findClosingParenIndex(startIndex: Int, tokens: List<Token>): Int {
//    val openParen = tokens[startIndex]
//    val closeParen = ParenHash[openParen] ?: throw Error("対になるカッコがParenHashにない")
//    var parenCounter = 0
//    val tokensFromStartIndex = tokens.slice(startIndex until tokens.count())
//    tokensFromStartIndex.forEachIndexed { index, token ->
//        if (token  == openParen) {
//            parenCounter += 1
//        } else if (token == closeParen) {
//            parenCounter -= 1
//        }
//        if (parenCounter == 0) {
//            return index + startIndex
//        }
//    }
//    return -1
//}