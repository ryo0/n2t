val Keywords = listOf(
    "class", "constructor", "function", "method", "field", "static", "var",
    "int", "char", "boolean", "void", "true", "false", "null", "this", "let",
    "do", "if", "else", "while", "return"
)

val KeywordHash = mapOf(
    "class" to Token.Class,
    "constructor" to Token.Constructor,
    "function" to Token.Function,
    "method" to Token.Method,
    "field" to Token.Field,
    "static" to Token.Static,
    "var" to Token.Var,
    "int" to Token.Int,
    "char" to Token.Char,
    "boolean" to Token.Boolean,
    "void" to Token.Void,
    "true" to Token.True,
    "false" to Token.False,
    "null" to Token.Null,
    "this" to Token.This,
    "let" to Token.Let,
    "do" to Token.Do,
    "if" to Token.If,
    "else" to Token.Else,
    "while" to Token.While,
    "return" to Token.Return
)

val SymbolHash = mapOf(
    '{' to Token.LCurlyBrace,
    '}' to Token.RCurlyBrace,
    '(' to Token.LParen,
    ')' to Token.RParen,
    '[' to Token.LSquareBracket,
    ']' to Token.RSquareBracket,
    '.' to Token.Dot,
    ',' to Token.Comma,
    ';' to Token.Semicolon,
    '+' to Token.Plus,
    '-' to Token.Minus,
    '*' to Token.Asterisk,
    '/' to Token.Slash,
    '&' to Token.And,
    '|' to Token.Pipe,
    '<' to Token.LessThan,
    '>' to Token.GreaterThan,
    '=' to Token.Equal,
    '~' to Token.Tilde
)

val ParenHash = mapOf(
    Token.LParen to Token.RParen,
    Token.LSquareBracket to Token.RSquareBracket,
    Token.LCurlyBrace to Token.RCurlyBrace
)

sealed class Token {
    data class IntegerConst(val num: kotlin.Int) : Token()
    data class StringConst(val string: String) : Token()
    data class Keyword(val key: String) : Token()
    data class VarOrKeyword(val name: String) : Token()
    data class Identifier(val name: String) : Token()
    data class Symbol(val symbol: String) : Token()
    object Class : Token()
    object Constructor : Token()
    object Function : Token()
    object Method : Token()
    object Field : Token()
    object Static : Token()
    object Var : Token()
    object Int : Token()
    object Char : Token()
    object Boolean : Token()
    object Void : Token()
    object True : Token()
    object False : Token()
    object Null : Token()
    object This : Token()
    object Let : Token()
    object Do : Token()
    object If : Token()
    object Else : Token()
    object While : Token()
    object Return : Token()
    object LCurlyBrace : Token()
    object RCurlyBrace : Token()
    object LParen : Token()
    object RParen : Token()
    object LSquareBracket : Token()
    object RSquareBracket : Token()
    object Dot : Token()
    object Comma : Token()
    object Semicolon : Token()
    object Plus : Token()
    object Minus : Token()
    object Asterisk : Token()
    object Slash : Token()
    object And : Token()
    object Pipe : Token()
    object LessThan : Token()
    object GreaterThan : Token()
    object Equal : Token()
    object Tilde : Token()
}

fun tokenizeSub(inputStr: String): List<Token> {
    var i = 0
    val tokens = mutableListOf<Token>()
    while (i < inputStr.length) {
        val str = inputStr[i]
        when (str) {
            ' ', '\n' -> i++
            '{', '}', '(', ')', '[', ']', '.', ',', ';', '+', '-', '*', '/', '&', '|', '<', '>', '=', '~' -> {
//                tokens.add(Token.Symbol(str.toString()))
                val token = SymbolHash[str] ?: throw Error("SymbolHashに不備があるようです。")
                tokens.add(token)
                i++
            }
            else -> {
                val strFromIToLast = inputStr.slice(i until inputStr.length)
                if (str.isDigit()) {
                    val digit = getDigit(strFromIToLast)
                    tokens.add(Token.IntegerConst(digit.toInt()))
                    i += digit.length
                } else if (str == '_' || str.isLetter()) {
                    val varName = getVariable(strFromIToLast)
                    tokens.add(Token.VarOrKeyword(varName))
                    i += varName.length
                } else if (str == '\"') {
                    val stringAfterDoubleQuote = inputStr.slice(i + 1 until inputStr.length)
                    val endOfStringConst = stringAfterDoubleQuote.indexOfFirst { it == '\"' }
                    if (endOfStringConst != -1) {
                        val stringConst = stringAfterDoubleQuote.slice(0 until endOfStringConst)
                        tokens.add(Token.StringConst(stringConst))
                        i += endOfStringConst + 2 // stringConstの長さ+両端のダブルクオーテーション2つ
                    } else {
                        i++
                    }
                } else {
                    i++
                }
            }
        }
    }
    return tokens
}

fun isKeyword(string: String): Boolean {
    return string in Keywords
}

fun filterVarOrKeyword(tokens: List<Token>): List<Token> {
    return tokens.map {
        if (it is Token.VarOrKeyword) {
            if (isKeyword(it.name)) {
                val keywordToken = KeywordHash[it.name]
                keywordToken ?: throw Error("KeywordHashに不備があるようです")
            } else {
                Token.Identifier(it.name)
            }
        } else {
            it
        }
    }
}

fun removeComments(string: String): String {
    var nowInOneLineComment = false
    var nowInRangeComment = false
    var result = ""
    var i = 0
    while (i < string.length) {
        if (i + 1 >= string.length) {
            // この判定を入れないと最後の文字が無視される
            result += string[i]
            break
        }
        val str1 = string[i]
        val str2 = string[i + 1]
        if (str1 == '/' && str2 == '/') {
            nowInOneLineComment = true
        } else if (str1 == '/' && str2 == '*') {
            nowInRangeComment = true
        } else if (nowInOneLineComment) {
            if (str1 == '\n') {
                nowInOneLineComment = false
            }
        } else if (nowInRangeComment) {
            if (str1 == '*' && str2 == '/') {
                nowInRangeComment = false
                i++ // 二文字分スキップしたいので余計に一つ進めておく
            }
        } else {
            result += str1
        }
        i++
    }
    return result
}


fun getVariable(string: String): String {
    var i = 0
    var result = ""
    while (i < string.length) {
        val str = string[i]
        if (str == '_' || str.isLetter() || str.isDigit()) {
            result += str
            i++
        } else {
            break
        }
    }
    return result
}

fun getDigit(string: String): String {
    var i = 0
    var result = ""
    while (i < string.length) {
        val str = string[i]
        if (str.isDigit()) {
            result += str
            i++
        } else {
            break
        }
    }
    return result
}

fun tokenize(string: String): List<Token> {
    return filterVarOrKeyword(tokenizeSub(removeComments(string)))
}

fun convertToXML(tokens: List<Token>): String {
    return "<tokens>\n" + tokens.map {
        when (it) {
            is Token.IntegerConst ->
                writeXML("integerConstant", it.num.toString())
            is Token.StringConst ->
                writeXML("stringConstant", it.string)
            is Token.Keyword ->
                writeXML("keyword", it.key)
            is Token.Identifier ->
                writeXML("identifier", it.name)
            is Token.Symbol ->
                writeXML("symbol", it.symbol)
            else -> {
                throw Error("異常なトークン $it")
            }

        }

    }.joinToString("\n") + "\n</tokens>"
}

fun writeXML(tag: String, value: String): String {
    return "<$tag> ${convertXMLValue(value)} </$tag>"
}

fun convertXMLValue(value: String): String {
    if (value == "<") {
        return "&lt;"
    } else if (value == ">") {
        return "&gt;"
    } else if (value == "&") {
        return "&amp;"
    } else {
        return value
    }
}

fun main() {
    val testCode = """
    if (true) {
        let x = 1
    } else {
        let x = 2
    }
""".trimIndent()

    val tokens = tokenize("(b+d)")
    println(parseExpression(tokens))

    val tokens2 = tokenize("((true+false) + 1)")
    println(parseExpression(tokens2))

    val tokens3 = tokenize("((true))")
    println(parseExpression(tokens3))
}

// リファクタ前:成功データ
// Expression(expElms=[_Paren(paren=LeftParen), _Expression(exp=Expression(expElms=[_Term(term=Term(constant=VarName(const=b))), _Op(op=Plus), _Term(term=Term(constant=VarName(const=d)))])), _Paren(paren=RightParen)])
// Expression(expElms=[_Paren(paren=LeftParen), _Expression(exp=Expression(expElms=[_Paren(paren=LeftParen), _Expression(exp=Expression(expElms=[_Term(term=Term(constant=KeyCons(const=True))), _Op(op=Plus), _Term(term=Term(constant=KeyCons(const=False)))])), _Paren(paren=RightParen), _Op(op=Plus), _Term(term=Term(constant=IntCons(const=1)))])), _Paren(paren=RightParen)])
// Expression(expElms=[_Paren(paren=LeftParen), _Expression(exp=Expression(expElms=[_Paren(paren=LeftParen), _Expression(exp=Expression(expElms=[_Term(term=Term(constant=KeyCons(const=True)))])), _Paren(paren=RightParen)])), _Paren(paren=RightParen)])
