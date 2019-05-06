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
}

// テスト用:成功データ データ作るの面倒なので標準出力と下のデータとでdiffとって調べてテストする


//Expression(expElms=[_Term(term=_Expression(left=Left, exp=Expression(expElms=[_Term(term=VarName(name=b)), _Op(op=Plus), _Term(term=VarName(name=d))]), right=Right))])
//Expression(expElms=[_Term(term=_Expression(left=Left, exp=Expression(expElms=[_Term(term=_Expression(left=Left, exp=Expression(expElms=[_Term(term=KeyC(const=True)), _Op(op=Plus), _Term(term=KeyC(const=False))]), right=Right)), _Op(op=Plus), _Term(term=IntC(const=1))]), right=Right))])
//Expression(expElms=[_Term(term=_Expression(left=Left, exp=Expression(expElms=[_Term(term=_Expression(left=Left, exp=Expression(expElms=[_Term(term=KeyC(const=True))]), right=Right))]), right=Right))])
//Expression(expElms=[_Term(term=VarName(name=a)), _Op(op=Plus), _Term(term=VarName(name=b))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=KeyC(const=True))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=IntC(const=1))])))]), elseStmts=Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=IntC(const=2))])))])))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=VarName(name=x)), _Op(op=Plus), _Term(term=IntC(const=1))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=IntC(const=3))]))), Let(stmt=LetStatement(varName=VarName(name=y), index=null, exp=Expression(expElms=[_Term(term=IntC(const=5))])))]), elseStmts=Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=z), index=null, exp=Expression(expElms=[_Term(term=IntC(const=1))]))), Let(stmt=LetStatement(varName=VarName(name=w), index=null, exp=Expression(expElms=[_Term(term=IntC(const=4))])))])))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=VarName(name=x)), _Op(op=Plus), _Term(term=IntC(const=1))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=x), index=Expression(expElms=[_Term(term=IntC(const=2))]), exp=Expression(expElms=[_Term(term=IntC(const=3))]))), Let(stmt=LetStatement(varName=VarName(name=y), index=Expression(expElms=[_Term(term=IntC(const=3))]), exp=Expression(expElms=[_Term(term=IntC(const=5))])))]), elseStmts=Statements(statements=[])))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=KeyC(const=True))]), ifStmts=Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=KeyC(const=False))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=IntC(const=1))])))]), elseStmts=Statements(statements=[])))]), elseStmts=Statements(statements=[])))])
//Statements(statements=[While(stmt=WhileStatement(expression=Expression(expElms=[_Term(term=KeyC(const=True))]), statements=Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=IntC(const=2))])))])))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=KeyC(const=False))]), ifStmts=Statements(statements=[While(stmt=WhileStatement(expression=Expression(expElms=[_Term(term=KeyC(const=True))]), statements=Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=IntC(const=2))])))])))]), elseStmts=Statements(statements=[])))])
//Statements(statements=[While(stmt=WhileStatement(expression=Expression(expElms=[_Term(term=KeyC(const=True))]), statements=Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=VarName(name=x))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=y), index=null, exp=Expression(expElms=[_Term(term=IntC(const=1))])))]), elseStmts=Statements(statements=[]))), While(stmt=WhileStatement(expression=Expression(expElms=[_Term(term=KeyC(const=True))]), statements=Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=IntC(const=2))])))])))])))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=KeyC(const=True))]), ifStmts=Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=VarName(name=x))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=y), index=null, exp=Expression(expElms=[_Term(term=IntC(const=1))])))]), elseStmts=Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=z), index=null, exp=Expression(expElms=[_Term(term=IntC(const=3))])))]))), While(stmt=WhileStatement(expression=Expression(expElms=[_Term(term=KeyC(const=True))]), statements=Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=IntC(const=2))])))])))]), elseStmts=Statements(statements=[])))])
//Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=_SubroutineCall(call=SubroutineCall(subroutineName=Identifier(name=b), expList=ExpressionList(expList=[]), ClassOrVarName=Identifier(name=a))))])))])
//Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=_SubroutineCall(call=SubroutineCall(subroutineName=Identifier(name=yyy), expList=ExpressionList(expList=[Expression(expElms=[_Term(term=IntC(const=1)), _Op(op=Plus), _Term(term=IntC(const=2))]), Expression(expElms=[_Term(term=KeyC(const=True)), _Op(op=And), _Term(term=KeyC(const=False))]), Expression(expElms=[_Term(term=VarName(name=x)), _Op(op=Asterisk), _Term(term=IntC(const=3))])]), ClassOrVarName=Identifier(name=xxx))))])))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=_SubroutineCall(call=SubroutineCall(subroutineName=Identifier(name=y), expList=ExpressionList(expList=[]), ClassOrVarName=Identifier(name=x))))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=_SubroutineCall(call=SubroutineCall(subroutineName=Identifier(name=_b), expList=ExpressionList(expList=[]), ClassOrVarName=Identifier(name=_a1))))])))]), elseStmts=Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=z), index=null, exp=Expression(expElms=[_Term(term=_SubroutineCall(call=SubroutineCall(subroutineName=Identifier(name=main), expList=ExpressionList(expList=[Expression(expElms=[_Term(term=VarName(name=a))]), Expression(expElms=[_Term(term=VarName(name=b))]), Expression(expElms=[_Term(term=VarName(name=c))])]), ClassOrVarName=Identifier(name=Main))))])))])))])
//Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=_SubroutineCall(call=SubroutineCall(subroutineName=Identifier(name=f), expList=ExpressionList(expList=[]), ClassOrVarName=null)))])))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=_SubroutineCall(call=SubroutineCall(subroutineName=Identifier(name=f), expList=ExpressionList(expList=[]), ClassOrVarName=null)))]), ifStmts=Statements(statements=[Do(stmt=DoStatement(subroutineCall=SubroutineCall(subroutineName=Identifier(name=g), expList=ExpressionList(expList=[Expression(expElms=[_Term(term=IntC(const=1))]), Expression(expElms=[_Term(term=IntC(const=2))]), Expression(expElms=[_Term(term=IntC(const=3))])]), ClassOrVarName=null)))]), elseStmts=Statements(statements=[])))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=_SubroutineCall(call=SubroutineCall(subroutineName=Identifier(name=f), expList=ExpressionList(expList=[]), ClassOrVarName=null)))]), ifStmts=Statements(statements=[Return(stmt=ReturnStatement(expression=null))]), elseStmts=Statements(statements=[])))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=KeyC(const=True))]), ifStmts=Statements(statements=[Return(stmt=ReturnStatement(expression=Expression(expElms=[_Term(term=_SubroutineCall(call=SubroutineCall(subroutineName=Identifier(name=g), expList=ExpressionList(expList=[Expression(expElms=[_Term(term=IntC(const=4))])]), ClassOrVarName=null)))])))]), elseStmts=Statements(statements=[Return(stmt=ReturnStatement(expression=Expression(expElms=[_Term(term=_SubroutineCall(call=SubroutineCall(subroutineName=Identifier(name=f), expList=ExpressionList(expList=[]), ClassOrVarName=null)))])))])))])
//Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=a), index=Expression(expElms=[_Term(term=IntC(const=2))]), exp=Expression(expElms=[_Term(term=IntC(const=1))])))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=ArrayAndIndex(name=x, index=Expression(expElms=[_Term(term=IntC(const=2))])))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=g), index=Expression(expElms=[_Term(term=_SubroutineCall(call=SubroutineCall(subroutineName=Identifier(name=f), expList=ExpressionList(expList=[Expression(expElms=[_Term(term=ArrayAndIndex(name=a, index=Expression(expElms=[])))])]), ClassOrVarName=null)))]), exp=Expression(expElms=[_Term(term=IntC(const=4))])))]), elseStmts=Statements(statements=[])))])
//Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=UnaryOpTerm(op=Tilde, term=KeyC(const=False)))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=y), index=null, exp=Expression(expElms=[_Term(term=VarName(name=a)), _Op(op=Minus), _Term(term=VarName(name=b)), _Op(op=Minus), _Term(term=IntC(const=2)), _Op(op=Plus), _Term(term=IntC(const=1))])))]), elseStmts=Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=z), index=null, exp=Expression(expElms=[_Term(term=UnaryOpTerm(op=Minus, term=IntC(const=2))), _Op(op=Asterisk), _Term(term=VarName(name=x)), _Op(op=Minus), _Term(term=IntC(const=1))])))])))])
//VarDec(type=Type$Int@53bd815b, vars=[i, j])
//VarDec(type=ClassName(name=Color), vars=[green, blue, red, white])
//VarDec(type=Type$Boolean@7637f22, vars=[t])
//SubroutineBody(varDecs=[VarDec(type=ClassName(name=Color), vars=[green, blue, red, white]), VarDec(type=Type$Boolean@7637f22, vars=[t])], statements=Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=UnaryOpTerm(op=Tilde, term=KeyC(const=False)))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=y), index=null, exp=Expression(expElms=[_Term(term=VarName(name=a)), _Op(op=Minus), _Term(term=VarName(name=b)), _Op(op=Minus), _Term(term=IntC(const=2)), _Op(op=Plus), _Term(term=IntC(const=1))])))]), elseStmts=Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=z), index=null, exp=Expression(expElms=[_Term(term=UnaryOpTerm(op=Minus, term=IntC(const=2))), _Op(op=Asterisk), _Term(term=VarName(name=x)), _Op(op=Minus), _Term(term=IntC(const=1))])))]))), While(stmt=WhileStatement(expression=Expression(expElms=[_Term(term=KeyC(const=True))]), statements=Statements(statements=[If(stmt=IfStatement(expression=Expression(expElms=[_Term(term=VarName(name=x))]), ifStmts=Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=y), index=null, exp=Expression(expElms=[_Term(term=IntC(const=1))])))]), elseStmts=Statements(statements=[]))), While(stmt=WhileStatement(expression=Expression(expElms=[_Term(term=KeyC(const=True))]), statements=Statements(statements=[Let(stmt=LetStatement(varName=VarName(name=x), index=null, exp=Expression(expElms=[_Term(term=IntC(const=2))])))])))])))]))
//ParameterList(list=[Parameter(type=Type$Int@41a4555e, name=abc), Parameter(type=ClassName(name=ClassName), name=cn)])
//Statements(statements=[Do(stmt=DoStatement(subroutineCall=SubroutineCall(subroutineName=Identifier(name=drawRectangle), expList=ExpressionList(expList=[Expression(expElms=[_Term(term=VarName(name=x))]), Expression(expElms=[_Term(term=_Expression(left=Left, exp=Expression(expElms=[_Term(term=VarName(name=y)), _Op(op=Plus), _Term(term=VarName(name=size))]), right=Right)), _Op(op=Minus), _Term(term=IntC(const=1))]), Expression(expElms=[_Term(term=VarName(name=x)), _Op(op=Plus), _Term(term=VarName(name=size))]), Expression(expElms=[_Term(term=VarName(name=y)), _Op(op=Plus), _Term(term=VarName(name=size))])]), ClassOrVarName=Identifier(name=Screen))))])
//Statements(statements=[Do(stmt=DoStatement(subroutineCall=SubroutineCall(subroutineName=Identifier(name=a), expList=ExpressionList(expList=[Expression(expElms=[_Term(term=IntC(const=1)), _Op(op=Plus), _Term(term=_Expression(left=Left, exp=Expression(expElms=[_Term(term=IntC(const=2)), _Op(op=Plus), _Term(term=VarName(name=x))]), right=Right)), _Op(op=Minus), _Term(term=IntC(const=1))])]), ClassOrVarName=null)))])
//Statements(statements=[Do(stmt=DoStatement(subroutineCall=SubroutineCall(subroutineName=Identifier(name=a), expList=ExpressionList(expList=[Expression(expElms=[_Term(term=IntC(const=1)), _Op(op=Plus), _Term(term=IntC(const=2)), _Op(op=Plus), _Term(term=VarName(name=x)), _Op(op=Minus), _Term(term=IntC(const=1))])]), ClassOrVarName=null)))])


