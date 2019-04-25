import java.io.File

sealed class Token {
    data class IntegerConst(val num: String) : Token()
    data class StringConst(val string: String) : Token()
    data class Keyword(val key: String) : Token()
    data class VarOrKeyword(val name: String) : Token()
    data class Identifier(val name: String) : Token()
    data class Symbol(val symbol: String) : Token()
    data class Operand(val op: String) : Token()
    data class Parentheses(val par: String) : Token()
}

val Keywords = listOf(
    "class", "constructor", "function", "method", "field", "static", "var",
    "int", "char", "boolean", "void", "true", "false", "null", "this", "let",
    "do", "if", "else", "while", "return"
)

fun tokenizeSub(inputStr: String): List<Token> {
    var i = 0
    val tokens = mutableListOf<Token>()
    while (i < inputStr.length) {
        val str = inputStr[i]
        when (str) {
            ' ', '\n' -> i++
            '{', '}', '(', ')', '[', ']', '.', ',', ';', '+', '-', '*', '/', '&', '|', '<', '>', '=', '~' -> {
                tokens.add(Token.Symbol(str.toString()))
                i++
            }
            else -> {
                val strFromIToLast = inputStr.slice(i until inputStr.length)
                if (str.isDigit()) {
                    val digit = getDigit(strFromIToLast)
                    tokens.add(Token.IntegerConst(digit))
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
                Token.Keyword(it.name)
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
    while (i < string.length - 1) {
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
        if (it is Token.IntegerConst) {
            writeXML("integerConstant", it.num)
        } else if (it is Token.StringConst) {
            writeXML("stringConstant", it.string)
        } else if (it is Token.Keyword) {
            writeXML("keyword", it.key)
        } else if (it is Token.Identifier) {
            writeXML("identifier", it.name)
        } else if (it is Token.Symbol) {
            writeXML("symbol", it.symbol)
        } else {
            throw Error("異常なトークン $it")
        }

    }.joinToString("\n") + "\n</tokens>"
}

fun writeXML(tag: String, value: String): String {
    return "<$tag> ${convertXMLValue(value)} </$tag>"
}

fun convertXMLValue(value: String): String {
    if(value == "<") {
        return "&lt;"
    } else if (value == ">") {
        return "&gt;"
    } else if (value == "&") {
        return "&amp;"
    } else {
        return value
    }
}

fun main(args: Array<String>) {
    val text = File("ArrayTest/Main.jack").readText()
    File("ArrayTest/out_Main.xml").writeText(convertToXML(tokenize(text)))

    val text2 = File("ExpressionLessSquare/Main.jack").readText()
    File("ExpressionLessSquare/out_Main.xml").writeText(convertToXML(tokenize(text2)))

    val text3 = File("ExpressionLessSquare/SquareGame.jack").readText()
    File("ExpressionLessSquare/out_SquareGame.xml").writeText(convertToXML(tokenize(text3)))

    val text4 = File("Square/Main.jack").readText()
    File("Square/out_Main.xml").writeText(convertToXML(tokenize(text4)))

    val text5 = File("Square/Square.jack").readText()
    File("Square/out_Square.xml").writeText(convertToXML(tokenize(text5)))

    val text6 = File("Square/SquareGame.jack").readText()
    File("Square/out_SquareGame.xml").writeText(convertToXML(tokenize(text6)))
}