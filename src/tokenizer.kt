import java.io.File

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
    if (!nowInOneLineComment && !nowInRangeComment) {
        result += string[i]
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

fun convertTokensToXML(tokens: List<Token>): String {
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

fun convertClass(_class: Class): String {
    val insideTag = writeXML("keyword", "class").plus(writeXML("identifier", _class.name))
        .plus(writeXML("symbol", "{"))
        .plus(_class.varDec.map { convertClassVarDec(it) }.joinToString("\n"))
        .plus(_class.subroutineDec.map { convertSubroutineDec(it) }.joinToString("\n"))
        .plus(writeXML("symbol", "}"))
    return writeXML("class", insideTag)
}

fun convertClassVarDec(classVarDec: ClassVarDec): String {
    val insideTag = writeXML("keyword", classVarDec.varDec.toString().toLowerCase())
        .plus(convertType(classVarDec.type))
        .plus(classVarDec.varNames.mapIndexed { index, it ->
            if (index == 0) {
                writeXML("identifier", it)
            } else {
                writeXML("symbol", ",").plus(
                    writeXML("identifier", it)
                )
            }
        }.joinToString("\n")).plus(writeXML("symbol", ";"))

    return writeXML("classVarDec", insideTag)
}

fun convertVoidOrType(type: VoidOrType): String {
    return if (type is VoidOrType._Type) {
        convertType(type.type)
    } else {
        writeXML("keyword", "void")
    }
}

fun convertType(type: Type): String {
    return when (type) {
        is Type.Int -> {
            writeXML("keyword", "int")
        }
        is Type.Char -> {
            writeXML("keyword", "char")
        }
        is Type.Boolean -> {
            writeXML("keyword", "boolean")
        }
        is Type.ClassName -> {
            writeXML("identifier", type.name)
        }
    }
}


fun convertSubroutineDec(subDec: SubroutineDec): String {
    val insideSubDec = writeXML("keyword", subDec.dec.toString().toLowerCase()).plus(
        convertVoidOrType(subDec.type)
    ).plus(writeXML("identifier", subDec.name)).plus(
        writeXML("symbol", "(")
    ).plus(
        convertParameterList(subDec.paramList).plus(
            writeXML("symbol", ")")
        ).plus(
            convertSubroutineBody(subDec.body)
        )
    )
    return writeXML("subroutineDec", insideSubDec)
}

fun convertParameterList(paramList: ParameterList): String {
    val insideParamList = paramList.list.mapIndexed { index, it ->
        if (index == 0) {
            convertType(it.type).plus(
                writeXML("identifier", it.name)
            )
        } else
            writeXML("symbol", ",").plus(
                convertType(it.type).plus(
                    writeXML("identifier", it.name)
                )
            )
    }.joinToString("\n")
    return writeXML("parameterList", insideParamList)
}

fun convertSubroutineBody(subroutineBody: SubroutineBody): String {
    val insideTag = writeXML("symbol", "{").plus(subroutineBody.varDecs.map { convertVarDec(it) }.joinToString("\n"))
        .plus(convertStatements(subroutineBody.statements))
        .plus(writeXML("symbol", "}"))
    return writeXML("subroutineBody", insideTag)
}

fun convertVarDec(varDec: VarDec): String {
    val vars = varDec.vars.mapIndexed { index, it ->
        if (index == 0) {
            writeXML("identifier", it)
        } else
            writeXML("symbol", ",").plus(
                writeXML("identifier", it)
            )
    }.joinToString("\n")

    val insideVarDec = writeXML("keyword", "var")
        .plus(convertType(varDec.type)).plus(vars).plus(writeXML("symbol", ";"))
    return writeXML("varDec", insideVarDec)
}


fun convertLetToXML(let: LetStatement): String {
    val index = let.index
    val insideLet = if (index == null) {
        writeXML("identifier", let.varName.name).plus(writeXML("symbol", "=")).plus(convertExpressionToXML(let.exp))
            .plus(writeXML("symbol", ";"))
    } else {
        writeXML("identifier", let.varName.name).plus(
            writeXML("symbol", "[").plus(
                convertExpressionToXML(index).plus(
                    writeXML("symbol", "]").plus(
                        writeXML("symbol", "=")
                    ).plus(
                        convertExpressionToXML(let.exp).plus(
                            writeXML("symbol", ";")
                        )
                    )
                )
            )
        )
    }
    return writeXML("letStatement", writeXML("keyword", "let").plus(insideLet))
}

fun convertStatements(stmts: Statements): String {
    return writeXML("statements", stmts.statements.map {
        when (it) {
            is Stmt.Let -> {
                convertLetToXML(it.stmt)
            }
            is Stmt.If -> {
                convertIf(it.stmt)
            }
            is Stmt.While -> {
                convertWhile(it.stmt)
            }
            is Stmt.Do -> {
                convertDo(it.stmt)
            }
            is Stmt.Return -> {
                convertReturn(it.stmt)
            }
        }
    }.joinToString("\n"))
}

fun convertWhile(whileStmt: WhileStatement): String {
    val expXML =
        writeXML("symbol", "(").plus(convertExpressionToXML(whileStmt.expression)).plus(writeXML("symbol", ")"))
    val whileStmtXML =
        writeXML("symbol", "{").plus(convertStatements(whileStmt.statements).plus(writeXML("symbol", "}")))
    return writeXML("whileStatement", writeXML("keyword", "while").plus(expXML.plus(whileStmtXML)))
}

fun convertIf(ifStmt: IfStatement): String {
    val expXML = writeXML("symbol", "(").plus(convertExpressionToXML(ifStmt.expression)).plus(writeXML("symbol", ")"))
    val ifStmtsXML = writeXML("symbol", "{").plus(convertStatements(ifStmt.ifStmts).plus(writeXML("symbol", "}")))
    val elseStmts = ifStmt.elseStmts
    if (elseStmts.statements.count() == 0) {
        return writeXML("ifStatement", writeXML("keyword", "if").plus(expXML.plus(ifStmtsXML)))
    } else {
        val elseStmtsXML = writeXML("symbol", "{").plus(convertStatements(elseStmts).plus(writeXML("symbol", "}")))
        return writeXML(
            "ifStatement",
            writeXML("keyword", "if").plus(expXML.plus(ifStmtsXML.plus(writeXML("keyword", "else").plus(elseStmtsXML))))
        )
    }
}

fun convertReturn(returnStmt: ReturnStatement): String {
    val exp = returnStmt.expression

    return writeXML(
        "returnStatement", if (exp != null) {
            writeXML("keyword", "return").plus(convertExpressionToXML(exp))
        } else {
            writeXML("keyword", "return")
        }.plus(writeXML("symbol", ";"))
    )
}

fun convertDo(Dostmt: DoStatement): String {
    return writeXML(
        "doStatement",
        writeXML("keyword", "do").plus(convertSubroutineCallToXML(Dostmt.subroutineCall)).plus(writeXML("symbol", ";"))
    )
}

fun convertTermToXML(term: Term): String {
    val insideTerm = when (term) {
        is Term.IntC -> {
            writeXML("integerConstant", term.const.toString())
        }
        is Term.StrC -> {
            writeXML("stringConstant", term.const)
        }
        is Term.KeyC -> {
            writeXML("keywordConstant", term.const.name)
        }
        is Term.VarName -> {
            writeXML("identifier", term.name)
        }
        is Term.ArrayAndIndex -> {
            writeXML("identifier", term.name).plus(
                writeXML("symbol", "[").plus(
                    (convertExpressionToXML(term.index)).plus(
                        (writeXML("symbol", "]"))
                    )
                )
            )
        }
        is Term._SubroutineCall -> {
            convertSubroutineCallToXML(term.call)
        }
        is Term._Expression -> {
            writeXML("symbol", "(").plus(convertExpressionToXML(term.exp).plus(writeXML("symbol", ")")))
        }
        is Term.UnaryOpTerm -> {
            val unaryOpSymbol = unaryOpSymbolHash[term.op] ?: throw Error("unaryOpSymbolHashに不備: $term")
            writeXML("symbol", unaryOpSymbol) + convertTermToXML(term.term)
        }
    }
    return writeXML(
        "term", insideTerm
    )
}

fun convertSubroutineCallToXML(subroutineCall: SubroutineCall): String {
    var result = writeXML("identifier", subroutineCall.subroutineName.name).plus(writeXML("symbol", "(")).plus(
        convertExpressionListToXML(subroutineCall.expList)
    ).plus(writeXML("symbol", ")"))
    val classOrVarName = subroutineCall.classOrVarName
    if (classOrVarName != null) {
        result = writeXML("identifier", classOrVarName.name).plus(writeXML("symbol", ".").plus(result))
    }
    return result
}

fun convertExpressionListToXML(expList: ExpressionList): String {
    return writeXML("expressionList", expList.expList.map {
        convertExpressionToXML(it)
    }.joinToString("\n"))
}

val opSymbolHash = mapOf(
    Op.Plus to "+",
    Op.Minus to "-",
    Op.Asterisk to "*",
    Op.Slash to "/",
    Op.And to "&",
    Op.Pipe to "|",
    Op.LessThan to "<",
    Op.GreaterThan to ">",
    Op.Equal to "="
)

val unaryOpSymbolHash = mapOf(
    UnaryOp.Tilde to "~",
    UnaryOp.Minus to "-"
)

fun convertExpressionToXML(exp: Expression): String {
    return writeXML("expression", exp.expElms.map {
        when (it) {
            is ExpElm._Term -> {
                convertTermToXML(it.term)
            }
            is ExpElm._Op -> {
                val opSymbol = opSymbolHash[it.op] ?: throw Error("opSymbolHashに不備があります$it")
                writeXML("symbol", opSymbol)
            }
        }
    }.joinToString("\n"))
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
