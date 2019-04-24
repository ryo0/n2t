sealed class Token {
    data class Number(val num: String) : Token()
    data class Keyword(val key: String) : Token()
    data class Variable(val varName: String) : Token()
    data class Operand(val op: String) : Token()
    data class Parentheses(val par: String) : Token()
}

fun main(args: Array<String>) {
    val inputStr = "(1 + 2) * 30 == 90"
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
            }
            else ->
                if (str.isDigit()) {
                    val digit = get_digit(inputStr.slice(i until inputStr.length))
                    tokens.add(Token.Number(digit))
                    i += digit.length
                } else {
                    i++
                }
        }
    }
    println(tokens.toString())
}

fun get_digit(string: String): String {
    var i = 0
    var resultDigit = ""
    while (i < string.length) {
        val str = string[i]
        if (str.isDigit()) {
            resultDigit += str
            i++
        } else {
            break
        }
    }
    return resultDigit
}