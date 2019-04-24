sealed class Token {
    data class Number(val num: String) : Token()
    data class Keyword(val key: String) : Token()
    data class Variable(val varName: String) : Token()
    data class Operand(val op: String) : Token()
    data class Parentheses(val par: String) : Token()
}

fun tokenize(inputStr: String): List<Token> {
    var i = 0
    val tokens = mutableListOf<Token>()
    while (i < inputStr.length) {
        val str = inputStr[i]
        when (str) {
            ' ', '\n' -> i++
            '+', '-', '*', '/' -> {
                tokens.add(Token.Operand(str.toString()))
                i++
            }

            '(', ')' -> {
                tokens.add(Token.Parentheses(str.toString()))
                i++
            }
            '=' -> if (inputStr[i + 1] == '=') {
                tokens.add(Token.Operand((inputStr[i].plus(inputStr[i + 1].toString()))))
                i += 2
            } else {
                tokens.add(Token.Operand((str.toString())))
                i++
            }
            else -> {
                val strFromIToLast = inputStr.slice(i until inputStr.length)
                if (str.isDigit()) {
                    val digit = getDigit(strFromIToLast)
                    tokens.add(Token.Number(digit))
                    i += digit.length
                } else if (isKeyword(strFromIToLast, "let")) {
                    val keyword = strFromIToLast.slice(0 until "let".length)
                    tokens.add(Token.Keyword(keyword))
                    i += "let".length
                } else if (str.isLetter()) {
                    val varName = getVariable(strFromIToLast)
                    tokens.add(Token.Variable(varName))
                    i += varName.length
                } else {
                    i++
                }
            }
        }
    }
    return tokens
}

fun main(args: Array<String>) {
    val inputStr = """
        |let x = 2
        |leti le
    """.trimMargin()
    println(tokenize(inputStr))
}

fun getVariable(string: String): String {
    var i = 0
    var result = ""
    while (i < string.length) {
        val str = string[i]
        if (str.isLetter() || str.isDigit()) {
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

fun isKeyword(string: String, keyword: String): Boolean {
//    letだけ対応してみる
    // 1. 文字数が3以上あるか？
    // 2. 3以上あるなら、それがletと一致するか？
    // 3. 4文字目があるなら、それは変数を構成する文字ではないか？

    // letx let
    val keywordLength = keyword.length

    if (string.length < keywordLength) {
        return false
    }
    val stringEqualsKeyword = string.slice(0 until keywordLength) == keyword
    if (stringEqualsKeyword) {
        return string.length <= keywordLength || !isAlnum(string[keywordLength])
    }
    return false
}

fun isAlnum(ch: Char): Boolean {
    return ch.isLetter() || ch.isDigit()
}