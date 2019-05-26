import java.io.File

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

    println(SymbolTable(parseClass(tokenize(testCode32))).classTable)

    val code = """// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/11/Pong/Bat.jack

/**
 * A graphical Pong bat.
 * Displayed as a filled horizontal rectangle that has
 * a screen location, a width and a height.
 * Has methods for drawing, erasing, moving left and right,
 * and changing its width (to make the hitting action more challenging).
 * This class should have been called "paddle", following the
 * standard Pong terminology. But, unaware of this terminology,
 * we called it "bat", and then decided to stick to it.
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

    /** Deallocates the object's memory. */
    method void dispose() {
        do Memory.deAlloc(this);
        return;
    }

    /** Shows the bat. */
    method void show() {
        do Screen.setColor(true);
        do draw();
        return;
    }

    /** Hides the bat. */
    method void hide() {
        do Screen.setColor(false);
        do draw();
        return;
    }

    /** Draws the bat. */
    method void draw() {
        do Screen.drawRectangle(x, y, x + width, y + height);
        return;
    }

    /** Sets the bat's direction (0=stop, 1=left, 2=right). */
    method void setDirection(int Adirection) {
        let direction = Adirection;
        return;
    }

    /** Returns the bat's left edge. */
    method int getLeft() {
        return x;
    }

    /** Returns the bat's right edge. */
    method int getRight() {
        return x + width;
    }

    /** Sets the bat's width. */
    method void setWidth(int Awidth) {
        do hide();
        let width = Awidth;
        do show();
        return;
    }

    /** Moves the bat one step in the bat's direction. */
    method void move() {
	    if (direction = 1) {
            let x = x - 4;
            if (x < 0) { let x = 0; }
            do Screen.setColor(false);
            do Screen.drawRectangle((x + width) + 1, y, (x + width) + 4, y + height);
            do Screen.setColor(true);
            do Screen.drawRectangle(x, y, x + 3, y + height);
        }
        else {
            let x = x + 4;
            if ((x + width) > 511) { let x = 511 - width; }
            do Screen.setColor(false);
            do Screen.drawRectangle(x - 4, y, x - 1, y + height);
            do Screen.setColor(true);
            do Screen.drawRectangle((x + width) - 3, y, x + width, y + height);
        }
        return;
    }
}
"""
//    Compiler(parseClass(tokenize(code))).compileClass()

//    Compiler(parseClass(tokenize(File("chap11TestData/ConvertToBin/Main.jack").readText()))).compileClass()
    val squareDir = "chap11TestData/Square/"
    val name1 = "Main.vm"
    val name2 = "Square.vm"
    val name3 = "SquareGame.vm"
    val parsed1 = parse(File("chap11TestData/Square/Main.jack").readText())
    val parsed2 = parse(File("chap11TestData/Square/Square.jack").readText())
    val parsed3 = parse(File("chap11TestData/Square/SquareGame.jack").readText())

    compile(squareDir+name1, parsed1, listOf(parsed2, parsed3))
    compile(squareDir+name2, parsed2, listOf(parsed1, parsed3))
    compile(squareDir+name3, parsed3, listOf(parsed1, parsed2))

    val parsed4 = parse(File("chap11TestData/Average/Main.jack").readText())
    compile("chap11TestData/Average/Main.vm", parsed4, listOf())
}

fun compile(path:String, program: Class, otherPrograms: List<Class>) {
    val table = SymbolTable(program)
    otherPrograms.forEach { table.createFuncAttrTable(it) }
    Compiler(program, table).compileClass(path)
}

fun parse(program: String): Class {
    return parseClass(tokenize(program))
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
