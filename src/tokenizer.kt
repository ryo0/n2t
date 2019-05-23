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

fun main() {
    val tokens = tokenize("(b+d)")
    println(parseExpression(tokens))

    val tokens2 = tokenize("((true+false) + 1)")
    println(parseExpression(tokens2))

    val tokens3 = tokenize("((true))")
    println(parseExpression(tokens3))

    val tokens4 = tokenize("a + b")
    println(parseExpression(tokens4))

    val testCode = """
        if (true) {
            let x = 1;
        } else {
            let x = 2;
        }
    """.trimIndent()
    val tokens5 = tokenize(testCode)
    println(parseStatements(tokens5))

    val testCode2 = """
        if (x + 1) {
            let x = 3;
            let y = 5;
        } else {
            let z = 1;
            let w = 4;
        }
    """.trimIndent()
    val tokens6 = tokenize(testCode2)
    println(parseStatements(tokens6))

    val testCode3 = """
        if (x + 1) {
            let x[2] = 3;
            let y[3] = 5;
        }
    """.trimIndent()
    val tokens7 = tokenize(testCode3)
    println(parseStatements(tokens7))

    val testCode4 = """
        if (true) {
            if (false) {
                let x = 1;
            }
        }
    """.trimIndent()
    val tokens8 = tokenize(testCode4)
    println(parseStatements(tokens8))

    val testCode5 = """
        while(true) {
            let x = 2;
        }
    """.trimIndent()
    val tokens9 = tokenize(testCode5)
    println(parseStatements(tokens9))

    val testCode6 = """
        if(false) {
            while(true) {
                let x = 2;
            }
        }
    """.trimIndent()
    val tokens10 = tokenize(testCode6)
    println(parseStatements(tokens10))

    val testCode7 = """
        while(true) {
            if(x) {
                let y = 1;
            }
            while(true) {
                let x = 2;
            }
        }
    """.trimIndent()
    val tokens11 = tokenize(testCode7)
    println(parseStatements(tokens11))

    val testCode8 = """
        if(true) {
            if(x) {
                let y = 1;
            } else {
                let z = 3;
            }
            while(true) {
                let x = 2;
            }
        }
    """.trimIndent()
    val tokens12 = tokenize(testCode8)
    println(parseStatements(tokens12))

    val testCode9 = """
        let x = a.b();
    """.trimIndent()
    val tokens13 = tokenize(testCode9)
    println(parseStatements(tokens13))

    val testCode10 = """
        let x = xxx.yyy(1 + 2, true & false, x * 3);
    """.trimIndent()
    val tokens14 = tokenize(testCode10)
    println(parseStatements(tokens14))

    val testCode11 = """
        if(x.y()) {
            let x = _a1._b();
        } else {
            let z = Main.main(a, b, c);
        }

    """.trimIndent()
    val tokens15 = tokenize(testCode11)
    println(parseStatements(tokens15))

    val testCode12 = """
        let x = f();
    """.trimIndent()
    val tokens16 = tokenize(testCode12)
    println(parseStatements(tokens16))

    val testCode13 = """
        if(f()) {
            do g(1, 2, 3);
        }
    """.trimIndent()
    val tokens17 = tokenize(testCode13)
    println(parseStatements(tokens17))

    val testCode14 = """
        if (f()) {
           return;
        }
    """.trimIndent()
    val tokens18 = tokenize(testCode14)
    println(parseStatements(tokens18))

    val testCode15 = """
        if(true) {
           return g(4);
        } else {
            return f();
        }
    """.trimIndent()
    val tokens19 = tokenize(testCode15)
    println(parseStatements(tokens19))

    val testCode16 = """
        let a[2] = 1;
    """.trimIndent()
    val tokens20 = tokenize(testCode16)
    println(parseStatements(tokens20))

    val testCode17 = """
        if(x[2]) {
            let g[f(a[])] = 4;
        }
    """.trimIndent()
    val tokens21 = tokenize(testCode17)
    println(parseStatements(tokens21))

    val testCode18 = """
        if(~false) {
            let y = a - b - 2 + 1;
        } else {
            let z = -2 * x - 1;
        }
    """.trimIndent()
    val tokens22 = tokenize(testCode18)
    println(parseStatements(tokens22))

    val testCode19 = """
         var int i, j;
    """.trimIndent()
    println(parseVarDec(tokenize(testCode19)))

    val testCode20 = """
         var Color green, blue, red, white;
    """.trimIndent()
    println(parseVarDec(tokenize(testCode20)))

    val testCode21 = """
         var boolean t;
    """.trimIndent()
    println(parseVarDec(tokenize(testCode21)))

    val testCode22 = """
         {
            var Color green, blue, red, white;
            var boolean t;
            if(~false) {
                let y = a - b - 2 + 1;
            } else {
                let z = -2 * x - 1;
            }
            while(true) {
                if(x) {
                    let y = 1;
                }
                while(true) {
                    let x = 2;
                }
            }
        }
    """.trimIndent()
    println(parseSubroutineBody(tokenize(testCode22)))

    val testCode23 = """
         int abc, ClassName cn )
    """.trimIndent()
    println(parseParameterListSub(tokenize(testCode23), listOf()).second)

    val testCode24 = """
         do Screen.drawRectangle(x, (y + size) - 1, x + size, y + size);
    """.trimIndent()
    println(parseStatements(tokenize(testCode24)))

    val testCode25 = """
         do a(1 + (2 + x) - 1);
    """.trimIndent()
    println(parseStatements(tokenize(testCode25)))

    val testCode26 = """
         do a(1 + 2 + x - 1);
    """.trimIndent()
    println(parseStatements(tokenize(testCode26)))

    val testCode27 = """
    function void test() {  // Added to test Jack syntax that is not use in
        var int i, j;       // the Square files.
        var String s;
        var Array a;
        if (false) {
            let s = "string constant";
            let s = null;
            let a[1] = a[2];
        }
        else {              // There is no else keyword in the Square files.
            let i = i * (-j);
            let j = j / (-2);   // note: unary negate constant 2
            let i = i | j;
        }
        return;
    }"""
    println(parseSubroutineDec(tokenize(testCode27)).second)

    val testCode28 = """
    constructor Square new(int Ax, int Ay, int Asize) {
      let x = Ax;
      let y = Ay;
      let size = Asize;
      do draw();
      return this;
   }"""
    println(parseSubroutineDec(tokenize(testCode28)).second)

    val testCode29 = """
    method void incSize() {
      if (((y + size) < 254) & ((x + size) < 510)) {
         do erase();
         let size = size + 2;
         do draw();
      }
      return;
   }
    """.trimIndent()
    println(parseSubroutineDec(tokenize(testCode29)).second)

    val testCode30 = """
        field int x, y; // screen location of the square's top-left corner
    """.trimIndent()
    println(parseClassVarDec(tokenize(testCode30)).second)

    val testCode31 = """
        static boolean test;    // Added for testing -- there is no static keyword
    """.trimIndent()
    println(parseClassVarDec(tokenize(testCode31)).second)

    val testCode32 = """
class Main {
    static boolean test;    // Added for testing -- there is no static keyword
    field Test a, b, c;     // in the Square files.
    function Main main() {
      var SquareGame game;
      let game = SquareGame.new();
      do game.run();
      do game.dispose();
      return;
    }

    function void test() {  // Added to test Jack syntax that is not use in
        var int i, j;       // the Square files.
        var String s;
        var Array a;
        if (false) {
            let s = "string constant";
            let s = null;
            let a[1] = a[2];
        }
        else {              // There is no else keyword in the Square files.
            let i = i * (-j);
            let j = j / (-2);   // note: unary negate constant 2
            let i = i | j;
        }
        return;
    }
}
    """.trimIndent()
    println(parseClass(tokenize(testCode32)))

    val testCode33 = """
    while (i < length) {
	    let a[i] = Keyboard.readInt("ENTER THE NEXT NUMBER: ");
	    let i = i + 1;
	}

	let i = 0;
	let sum = 0;

	while (i < length) {
	    let sum = sum + a[i];
	    let i = i + 1;
	}
    """.trimIndent()
    println(parseStatements(tokenize(testCode33)))

//    File("ArrayTest/out_Main.xml").writeText(convertClass(parseClass(tokenize(File("ArrayTest/Main.jack").readText()))))
//
//    File("ExpressionLessSquare/out_Main.xml").writeText(convertClass(parseClass(tokenize(File("ExpressionLessSquare/Main.jack").readText()))))
//    File("ExpressionLessSquare/out_SquareGame.xml").writeText(convertClass(parseClass(tokenize(File("ExpressionLessSquare/SquareGame.jack").readText()))))
//    File("ExpressionLessSquare/out_Square.xml").writeText(convertClass(parseClass(tokenize(File("ExpressionLessSquare/Square.jack").readText()))))
//
//    File("Square/out_Main.xml").writeText(convertClass(parseClass(tokenize(File("Square/Main.jack").readText()))))
//    File("Square/out_Square.xml").writeText(convertClass(parseClass(tokenize(File("Square/Square.jack").readText()))))
//    File("Square/out_SquareGame.xml").writeText(convertClass(parseClass(tokenize(File("Square/SquareGame.jack").readText()))))

    println(SymbolTable(parseClass(tokenize(testCode32))).classTable)

    val code = """// This file is part of www.nand2tetris.org
 // and the book "The Elements of Computing Systems"
 // by Nisan and Schocken, MIT Press.
 // File name: projects/11/Seven/Main.jack

 /**
  * Computes the value of 1 + (2 * 3) and prints the result
  * at the top-left of the screen.
  */
 class Bat {
       field int x, y;           // the bat's screen location
    field int width, height;  // the bat's width and height
    field int direction;      // direction of the bat's movement (1 = left, 2 = right)

    /** Constructs a new bat with the given location and width. */
    constructor Bat new(int Ax, int Ay, int Awidth, int Aheight) {
        let x = Ax;
        let y = Ay;
        let width = Awidth;
        let height = Aheight;
        let direction = 2;
        do show();
        return this;
    }

 }"""
    Compiler(parseClass(tokenize(code))).compileClass()
}

// テスト用:成功データ データ作るの面倒なので標準出力と下のデータとでdiffとって調べてテストする


//Expression(expElms=[_Term(term=_Expression(left=Left, exp=Expression(expElms=[_Term(term=VarName(name=b)), _Op(op=Plus), _Term(term=VarName(name=d))]), right=Right))])
//Expression(expElms=[_Term(term=_Expression(left=Left, exp=Expression(expElms=[_Term(term=_Expression(left=Left, exp=Expression(expElms=[_Term(term=KeyC(const=True)), _Op(op=Plus), _Term(term=KeyC(const=False))]), right=Right)), _Op(op=Plus), _Term(term=IntC(const=1))]), right=Right))])
//Expression(expElms=[_Term(term=_Expression(left=Left, exp=Expression(expElms=[_Term(term=_Expression(left=Left, exp=Expression(expElms=[_Term(term=KeyC(const=True))]), right=Right))]), right=Right))])
//Expression(expElms=[_Term(term=VarName(name=a)), _Op(op=Plus), _Term(term=VarName(name=b))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=KeyC(const=True))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=IntC(const=1))])))]), elseStmts=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=IntC(const=2))])))])))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=VarName(name=x)), _Op(op=Plus), _Term(term=IntC(const=1))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=IntC(const=3))]))), Let(stmt=LetStatement(varNames=VarName(name=y), index=null, exp=Expression(expElms=[_Term(term=IntC(const=5))])))]), elseStmts=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=z), index=null, exp=Expression(expElms=[_Term(term=IntC(const=1))]))), Let(stmt=LetStatement(varNames=VarName(name=w), index=null, exp=Expression(expElms=[_Term(term=IntC(const=4))])))])))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=VarName(name=x)), _Op(op=Plus), _Term(term=IntC(const=1))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=x), index=Expression(expElms=[_Term(term=IntC(const=2))]), exp=Expression(expElms=[_Term(term=IntC(const=3))]))), Let(stmt=LetStatement(varNames=VarName(name=y), index=Expression(expElms=[_Term(term=IntC(const=3))]), exp=Expression(expElms=[_Term(term=IntC(const=5))])))]), elseStmts=Statements(statements=[])))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=KeyC(const=True))]), ifStmts=Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=KeyC(const=False))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=IntC(const=1))])))]), elseStmts=Statements(statements=[])))]), elseStmts=Statements(statements=[])))])
//Statements(statements=[While(stmt=WhileStatement(expression=Expression(expElms=[_Term(term=KeyC(const=True))]), statements=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=IntC(const=2))])))])))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=KeyC(const=False))]), ifStmts=Statements(statements=[While(stmt=WhileStatement(expression=Expression(expElms=[_Term(term=KeyC(const=True))]), statements=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=IntC(const=2))])))])))]), elseStmts=Statements(statements=[])))])
//Statements(statements=[While(stmt=WhileStatement(expression=Expression(expElms=[_Term(term=KeyC(const=True))]), statements=Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=VarName(name=x))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=y), index=null, exp=Expression(expElms=[_Term(term=IntC(const=1))])))]), elseStmts=Statements(statements=[]))), While(stmt=WhileStatement(expression=Expression(expElms=[_Term(term=KeyC(const=True))]), statements=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=IntC(const=2))])))])))])))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=KeyC(const=True))]), ifStmts=Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=VarName(name=x))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=y), index=null, exp=Expression(expElms=[_Term(term=IntC(const=1))])))]), elseStmts=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=z), index=null, exp=Expression(expElms=[_Term(term=IntC(const=3))])))]))), While(stmt=WhileStatement(expression=Expression(expElms=[_Term(term=KeyC(const=True))]), statements=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=IntC(const=2))])))])))]), elseStmts=Statements(statements=[])))])
//Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=_SubroutineCall(call=SubroutineCall(subroutineName=Identifier(name=b), expList=ExpressionList(expList=[]), classOrVarName=Identifier(name=a))))])))])
//Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=_SubroutineCall(call=SubroutineCall(subroutineName=Identifier(name=yyy), expList=ExpressionList(expList=[Expression(expElms=[_Term(term=IntC(const=1)), _Op(op=Plus), _Term(term=IntC(const=2))]), Expression(expElms=[_Term(term=KeyC(const=True)), _Op(op=And), _Term(term=KeyC(const=False))]), Expression(expElms=[_Term(term=VarName(name=x)), _Op(op=Asterisk), _Term(term=IntC(const=3))])]), classOrVarName=Identifier(name=xxx))))])))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=_SubroutineCall(call=SubroutineCall(subroutineName=Identifier(name=y), expList=ExpressionList(expList=[]), classOrVarName=Identifier(name=x))))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=_SubroutineCall(call=SubroutineCall(subroutineName=Identifier(name=_b), expList=ExpressionList(expList=[]), classOrVarName=Identifier(name=_a1))))])))]), elseStmts=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=z), index=null, exp=Expression(expElms=[_Term(term=_SubroutineCall(call=SubroutineCall(subroutineName=Identifier(name=main), expList=ExpressionList(expList=[Expression(expElms=[_Term(term=VarName(name=a))]), Expression(expElms=[_Term(term=VarName(name=b))]), Expression(expElms=[_Term(term=VarName(name=c))])]), classOrVarName=Identifier(name=Main))))])))])))])
//Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=_SubroutineCall(call=SubroutineCall(subroutineName=Identifier(name=f), expList=ExpressionList(expList=[]), classOrVarName=null)))])))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=_SubroutineCall(call=SubroutineCall(subroutineName=Identifier(name=f), expList=ExpressionList(expList=[]), classOrVarName=null)))]), ifStmts=Statements(statements=[Do(stmt=DoStatement(subroutineCall=SubroutineCall(subroutineName=Identifier(name=g), expList=ExpressionList(expList=[Expression(expElms=[_Term(term=IntC(const=1))]), Expression(expElms=[_Term(term=IntC(const=2))]), Expression(expElms=[_Term(term=IntC(const=3))])]), classOrVarName=null)))]), elseStmts=Statements(statements=[])))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=_SubroutineCall(call=SubroutineCall(subroutineName=Identifier(name=f), expList=ExpressionList(expList=[]), classOrVarName=null)))]), ifStmts=Statements(statements=[Return(stmt=ReturnStatement(expression=null))]), elseStmts=Statements(statements=[])))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=KeyC(const=True))]), ifStmts=Statements(statements=[Return(stmt=ReturnStatement(expression=Expression(expElms=[_Term(term=_SubroutineCall(call=SubroutineCall(subroutineName=Identifier(name=g), expList=ExpressionList(expList=[Expression(expElms=[_Term(term=IntC(const=4))])]), classOrVarName=null)))])))]), elseStmts=Statements(statements=[Return(stmt=ReturnStatement(expression=Expression(expElms=[_Term(term=_SubroutineCall(call=SubroutineCall(subroutineName=Identifier(name=f), expList=ExpressionList(expList=[]), classOrVarName=null)))])))])))])
//Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=a), index=Expression(expElms=[_Term(term=IntC(const=2))]), exp=Expression(expElms=[_Term(term=IntC(const=1))])))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=ArrayAndIndex(name=x, index=Expression(expElms=[_Term(term=IntC(const=2))])))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=g), index=Expression(expElms=[_Term(term=_SubroutineCall(call=SubroutineCall(subroutineName=Identifier(name=f), expList=ExpressionList(expList=[Expression(expElms=[_Term(term=ArrayAndIndex(name=a, index=Expression(expElms=[])))])]), classOrVarName=null)))]), exp=Expression(expElms=[_Term(term=IntC(const=4))])))]), elseStmts=Statements(statements=[])))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=UnaryOpTerm(op=Tilde, term=KeyC(const=False)))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=y), index=null, exp=Expression(expElms=[_Term(term=VarName(name=a)), _Op(op=Minus), _Term(term=VarName(name=b)), _Op(op=Minus), _Term(term=IntC(const=2)), _Op(op=Plus), _Term(term=IntC(const=1))])))]), elseStmts=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=z), index=null, exp=Expression(expElms=[_Term(term=UnaryOpTerm(op=Minus, term=IntC(const=2))), _Op(op=Asterisk), _Term(term=VarName(name=x)), _Op(op=Minus), _Term(term=IntC(const=1))])))])))])
//VarDec(type=Type$Int@53bd815b, vars=[i, j])
//VarDec(type=ClassName(name=Color), vars=[green, blue, red, white])
//VarDec(type=Type$Boolean@7637f22, vars=[t])
//SubroutineBody(varDecs=[VarDec(type=ClassName(name=Color), vars=[green, blue, red, white]), VarDec(type=Type$Boolean@7637f22, vars=[t])], statements=Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=UnaryOpTerm(op=Tilde, term=KeyC(const=False)))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=y), index=null, exp=Expression(expElms=[_Term(term=VarName(name=a)), _Op(op=Minus), _Term(term=VarName(name=b)), _Op(op=Minus), _Term(term=IntC(const=2)), _Op(op=Plus), _Term(term=IntC(const=1))])))]), elseStmts=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=z), index=null, exp=Expression(expElms=[_Term(term=UnaryOpTerm(op=Minus, term=IntC(const=2))), _Op(op=Asterisk), _Term(term=VarName(name=x)), _Op(op=Minus), _Term(term=IntC(const=1))])))]))), While(stmt=WhileStatement(expression=Expression(expElms=[_Term(term=KeyC(const=True))]), statements=Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=VarName(name=x))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=y), index=null, exp=Expression(expElms=[_Term(term=IntC(const=1))])))]), elseStmts=Statements(statements=[]))), While(stmt=WhileStatement(expression=Expression(expElms=[_Term(term=KeyC(const=True))]), statements=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=IntC(const=2))])))])))])))]))
//ParameterList(list=[Parameter(type=Type$Int@41a4555e, name=abc), Parameter(type=ClassName(name=ClassName), name=cn)])
//Statements(statements=[Do(stmt=DoStatement(subroutineCall=SubroutineCall(subroutineName=Identifier(name=drawRectangle), expList=ExpressionList(expList=[Expression(expElms=[_Term(term=VarName(name=x))]), Expression(expElms=[_Term(term=_Expression(left=Left, exp=Expression(expElms=[_Term(term=VarName(name=y)), _Op(op=Plus), _Term(term=VarName(name=size))]), right=Right)), _Op(op=Minus), _Term(term=IntC(const=1))]), Expression(expElms=[_Term(term=VarName(name=x)), _Op(op=Plus), _Term(term=VarName(name=size))]), Expression(expElms=[_Term(term=VarName(name=y)), _Op(op=Plus), _Term(term=VarName(name=size))])]), classOrVarName=Identifier(name=Screen))))])
//Statements(statements=[Do(stmt=DoStatement(subroutineCall=SubroutineCall(subroutineName=Identifier(name=a), expList=ExpressionList(expList=[Expression(expElms=[_Term(term=IntC(const=1)), _Op(op=Plus), _Term(term=_Expression(left=Left, exp=Expression(expElms=[_Term(term=IntC(const=2)), _Op(op=Plus), _Term(term=VarName(name=x))]), right=Right)), _Op(op=Minus), _Term(term=IntC(const=1))])]), classOrVarName=null)))])
//Statements(statements=[Do(stmt=DoStatement(subroutineCall=SubroutineCall(subroutineName=Identifier(name=a), expList=ExpressionList(expList=[Expression(expElms=[_Term(term=IntC(const=1)), _Op(op=Plus), _Term(term=IntC(const=2)), _Op(op=Plus), _Term(term=VarName(name=x)), _Op(op=Minus), _Term(term=IntC(const=1))])]), classOrVarName=null)))])
//SubroutineDec(dec=Function, type=VoidOrType$Void@6f75e721, name=test, paramList=ParameterList(list=[]), body=SubroutineBody(varDecs=[VarDec(type=Type$Int@41a4555e, vars=[i, j]), VarDec(type=ClassName(name=String), vars=[s]), VarDec(type=ClassName(name=Array), vars=[a])], statements=Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=KeyC(const=False))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=s), index=null, exp=Expression(expElms=[_Term(term=StrC(const=string constant))]))), Let(stmt=LetStatement(varNames=VarName(name=s), index=null, exp=Expression(expElms=[_Term(term=KeyC(const=Null))]))), Let(stmt=LetStatement(varNames=VarName(name=a), index=Expression(expElms=[_Term(term=IntC(const=1))]), exp=Expression(expElms=[_Term(term=ArrayAndIndex(name=a, index=Expression(expElms=[_Term(term=IntC(const=2))])))])))]), elseStmts=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=i), index=null, exp=Expression(expElms=[_Term(term=VarName(name=i)), _Op(op=Asterisk), _Term(term=_Expression(left=Left, exp=Expression(expElms=[_Term(term=UnaryOpTerm(op=Minus, term=VarName(name=j)))]), right=Right))]))), Let(stmt=LetStatement(varNames=VarName(name=j), index=null, exp=Expression(expElms=[_Term(term=VarName(name=j)), _Op(op=Slash), _Term(term=_Expression(left=Left, exp=Expression(expElms=[_Term(term=UnaryOpTerm(op=Minus, term=IntC(const=2)))]), right=Right))]))), Let(stmt=LetStatement(varNames=VarName(name=i), index=null, exp=Expression(expElms=[_Term(term=VarName(name=i)), _Op(op=Pipe), _Term(term=VarName(name=j))])))]))), Return(stmt=ReturnStatement(expression=null))])))
//SubroutineDec(dec=Constructor, type=_Type(type=ClassName(name=Square)), name=new, paramList=ParameterList(list=[Parameter(type=Type$Int@41a4555e, name=Ax), Parameter(type=Type$Int@41a4555e, name=Ay), Parameter(type=Type$Int@41a4555e, name=Asize)]), body=SubroutineBody(varDecs=[], statements=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=VarName(name=Ax))]))), Let(stmt=LetStatement(varNames=VarName(name=y), index=null, exp=Expression(expElms=[_Term(term=VarName(name=Ay))]))), Let(stmt=LetStatement(varNames=VarName(name=size), index=null, exp=Expression(expElms=[_Term(term=VarName(name=Asize))]))), Do(stmt=DoStatement(subroutineCall=SubroutineCall(subroutineName=Identifier(name=draw), expList=ExpressionList(expList=[]), classOrVarName=null))), Return(stmt=ReturnStatement(expression=Expression(expElms=[_Term(term=KeyC(const=This))])))])))
//SubroutineDec(dec=Method, type=VoidOrType$Void@6f75e721, name=incSize, paramList=ParameterList(list=[]), body=SubroutineBody(varDecs=[], statements=Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=_Expression(left=Left, exp=Expression(expElms=[_Term(term=_Expression(left=Left, exp=Expression(expElms=[_Term(term=VarName(name=y)), _Op(op=Plus), _Term(term=VarName(name=size))]), right=Right)), _Op(op=LessThan), _Term(term=IntC(const=254))]), right=Right)), _Op(op=And), _Term(term=_Expression(left=Left, exp=Expression(expElms=[_Term(term=_Expression(left=Left, exp=Expression(expElms=[_Term(term=VarName(name=x)), _Op(op=Plus), _Term(term=VarName(name=size))]), right=Right)), _Op(op=LessThan), _Term(term=IntC(const=510))]), right=Right))]), ifStmts=Statements(statements=[Do(stmt=DoStatement(subroutineCall=SubroutineCall(subroutineName=Identifier(name=erase), expList=ExpressionList(expList=[]), classOrVarName=null))), Let(stmt=LetStatement(varNames=VarName(name=size), index=null, exp=Expression(expElms=[_Term(term=VarName(name=size)), _Op(op=Plus), _Term(term=IntC(const=2))]))), Do(stmt=DoStatement(subroutineCall=SubroutineCall(subroutineName=Identifier(name=draw), expList=ExpressionList(expList=[]), classOrVarName=null)))]), elseStmts=Statements(statements=[]))), Return(stmt=ReturnStatement(expression=null))])))
//ClassVarDec(varDec=Field, type=Type$Int@71dac704, varNames=[x, y])
//ClassVarDec(varDec=Static, type=Type$Boolean@2d363fb3, varNames=[test])
//Class(name=Main, varDec=[ClassVarDec(varDec=Static, type=Type$Boolean@2d363fb3, varNames=[test]), ClassVarDec(varDec=Field, type=ClassName(name=Test), varNames=[a, b, c])], subroutineDec=[SubroutineDec(dec=Function, type=_Type(type=ClassName(name=Main)), name=main, paramList=ParameterList(list=[]), body=SubroutineBody(varDecs=[VarDec(type=ClassName(name=SquareGame), vars=[game])], statements=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=game), index=null, exp=Expression(expElms=[_Term(term=_SubroutineCall(call=SubroutineCall(subroutineName=Identifier(name=new), expList=ExpressionList(expList=[]), classOrVarName=Identifier(name=SquareGame))))]))), Do(stmt=DoStatement(subroutineCall=SubroutineCall(subroutineName=Identifier(name=run), expList=ExpressionList(expList=[]), classOrVarName=Identifier(name=game)))), Do(stmt=DoStatement(subroutineCall=SubroutineCall(subroutineName=Identifier(name=dispose), expList=ExpressionList(expList=[]), classOrVarName=Identifier(name=game)))), Return(stmt=ReturnStatement(expression=null))]))), SubroutineDec(dec=Function, type=VoidOrType$Void@782830e, name=test, paramList=ParameterList(list=[]), body=SubroutineBody(varDecs=[VarDec(type=Type$Int@71dac704, vars=[i, j]), VarDec(type=ClassName(name=String), vars=[s]), VarDec(type=ClassName(name=Array), vars=[a])], statements=Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=KeyC(const=False))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=s), index=null, exp=Expression(expElms=[_Term(term=StrC(const=string constant))]))), Let(stmt=LetStatement(varNames=VarName(name=s), index=null, exp=Expression(expElms=[_Term(term=KeyC(const=Null))]))), Let(stmt=LetStatement(varNames=VarName(name=a), index=Expression(expElms=[_Term(term=IntC(const=1))]), exp=Expression(expElms=[_Term(term=ArrayAndIndex(name=a, index=Expression(expElms=[_Term(term=IntC(const=2))])))])))]), elseStmts=Statements(statements=[Let(stmt=LetStatement(varNames=VarName(name=i), index=null, exp=Expression(expElms=[_Term(term=VarName(name=i)), _Op(op=Asterisk), _Term(term=_Expression(left=Left, exp=Expression(expElms=[_Term(term=UnaryOpTerm(op=Minus, term=VarName(name=j)))]), right=Right))]))), Let(stmt=LetStatement(varNames=VarName(name=j), index=null, exp=Expression(expElms=[_Term(term=VarName(name=j)), _Op(op=Slash), _Term(term=_Expression(left=Left, exp=Expression(expElms=[_Term(term=UnaryOpTerm(op=Minus, term=IntC(const=2)))]), right=Right))]))), Let(stmt=LetStatement(varNames=VarName(name=i), index=null, exp=Expression(expElms=[_Term(term=VarName(name=i)), _Op(op=Pipe), _Term(term=VarName(name=j))])))]))), Return(stmt=ReturnStatement(expression=null))])))])