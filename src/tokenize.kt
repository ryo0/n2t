sealed class Token {
    data class Number(val num: String) : Token()
    data class Keyword(val key: String) : Token()
    data class VarOrKeyword(val name: String): Token()
    data class Variable(val varName: String) : Token()
    data class Operand(val op: String) : Token()
    data class Parentheses(val par: String) : Token()
}

val Keywords = listOf("let", "class", "var")

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
                } else if (str.isLetter()) {
                    val varName = getVariable(strFromIToLast)
                    tokens.add(Token.VarOrKeyword(varName))
                    i += varName.length
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
    return tokens.map{
        if(it is Token.VarOrKeyword){
            if (isKeyword(it.name)) {
                Token.Keyword(it.name)
            } else {
               Token.Variable(it.name)
            }
        } else {
            it
        }
    }
}

fun main(args: Array<String>) {
    val inputStr = """
        |let x = 2
        |let3 le class
        |(3 + 2 * let) = 3
    """.trimMargin()
    println(filterVarOrKeyword(tokenize(inputStr)))
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