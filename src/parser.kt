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


//fun parse(tokens: List<Token>): Statements {
//    var i = 0
//    while(i < tokens.count()) {
//        val token = tokens[i]
//        when(token) {
//            is Token.If -> {
//
//            }
//            is Token.Let -> {
//
//            }
//        }
//    }
//}

//fun parseIfStatement(tokens: List<Token>): Stmt.If {
//    val expEnd = tokens.indexOfFirst { it is Token.RParen }
//    val exp = tokens.slice(1..expEnd)
//
//}

fun findClosingParenIndex(startIndex: Int, tokens: List<Token>): Int {
    val openParen = tokens[startIndex]
    val closeParen = ParenHash[openParen] ?: throw Error("対になるカッコがParenHashにない")
    var parenCounter = 0
    val tokensFromStartIndex = tokens.slice(startIndex until tokens.count())
    tokensFromStartIndex.forEachIndexed { index, token ->
        if (token  == openParen) {
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