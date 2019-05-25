// THIS, THATは値が場合に応じて変わるのでその都度Pointerでセットしてる
// THISはthis, THATは配列の現在地の意味
// TODO メソッド内部にいる間は常にArgが+1される

class Compiler(private val _class: Class) {
    val className = _class.name
    private val table = SymbolTable(_class)
    private var subroutineTable: Map<String, SymbolValue>? = null
    private val vmWriter = VMWriter(className)
    private var inMethod = false

    fun compileClass() {
        _class.subroutineDec.forEach { compileSubroutine(it) }
    }

    private fun argIndex(index: Int, inMethod: Boolean): Int {
        return if (inMethod) {
            index + 1
        } else {
            index
        }
    }

    private fun getSymbolInfo(name: String): SymbolValue? {
        val subTable = subroutineTable
        if (subTable != null) {
            return subTable[name] ?: table.classTable[name]
        } else {
            return table.classTable[name]
        }
    }

    private fun localVarNum(): Int {
        return table.varIndex
    }

    private fun compileSubroutine(subroutineDec: SubroutineDec) {
        subroutineTable = table.subroutineTableCreator(subroutineDec)
        vmWriter.writeFunction(subroutineDec.name, localVarNum())
        if (subroutineDec.dec == MethodDec.Constructor) {
            if (table.fieldIndex != -1) {
                vmWriter.writePush(Segment.CONSTANT, table.fieldIndex + 1)
                vmWriter.writeCall("Memory.alloc", 1)
                vmWriter.writePop(Segment.POINTER, 0)
            }
        } else if (subroutineDec.dec == MethodDec.Method) {
            vmWriter.writePush(Segment.ARGUMENT, 0)
            vmWriter.writePop(Segment.POINTER, 0)
            inMethod = true
        }
        compileStatements(subroutineDec.body.statements)
        val type = subroutineDec.type
        inMethod = false
    }

    private fun compileStatements(statements: Statements) {
        statements.statements.forEach {
            when (it) {
                is Stmt.Do -> {
                    compileDoStatement(it.stmt)
                }
                is Stmt.Let -> {
                    compileLetStatement(it.stmt)
                }
                is Stmt.Return -> {
                    compileReturn(it.stmt)
                }
            }
        }
    }

    private fun compileReturn(stmt: ReturnStatement) {
        if (stmt.expression == null) {
            vmWriter.writePush(Segment.CONSTANT, 0)
        } else {
            val _term = stmt.expression.expElms[0]
            if (_term is ExpElm._Term) {
                val term = _term.term
                if (term is Term.KeyC) {
                    if (term.const == Keyword.This) {
                        vmWriter.writePush(Segment.POINTER, 0)
                    }
                }
            }
        }
        vmWriter.writeReturn()
    }

    private fun compileLetStatement(letStatement: LetStatement) {
        val symbolInfo = getSymbolInfo(letStatement.varName.name) ?: throw Error("symbolTableにない値をlet文で扱っている")
        val index = symbolInfo.index
        val exp = letStatement.exp
        val arrayIndex = letStatement.index
        compileExpression(exp)
        if (arrayIndex == null) {
            if (symbolInfo.attribute == Attribute.Field) {
                vmWriter.writePop(Segment.THIS, symbolInfo.index)
            } else if (symbolInfo.attribute == Attribute.Argument) {
                vmWriter.writePop(Segment.ARGUMENT, argIndex(index, inMethod))
            } else if (symbolInfo.attribute == Attribute.Var) {
                vmWriter.writePop(Segment.LOCAL, index)
            }
        } else {
        }
    }

    private fun compileDoStatement(doStatement: DoStatement) {
        // TODO メソッドなら呼ぶ前にオブジェクトをpushする
        // TODO そして引数を一つ増やす
        // TODO メソッドかファンクションかを判定するために、シンボルテーブルを拡張するべき
        // TODO 「クラス名であること」も必要になる気が。

        val classOrVarName = doStatement.subroutineCall.classOrVarName
        val subroutineName = doStatement.subroutineCall.subroutineName.name
        val expList = doStatement.subroutineCall.expList.expList
        val funcAttr = table.funAttrTable[subroutineName]

        expList.forEach { compileExpression(it) }

        // 1. a.fun()
        // aが何らかのクラスに属する  → メソッドになる
        // aがクラスに属さない → funに応じてファンクションもしくはコンストラクタになる
        // → コンストラクタがdoで呼ばれることはないらしいので、必ずファンクションになる
        // 2. fun()
        // 常にメソッド。オブジェクトは現在のクラスとなる

        if (classOrVarName != null) {
            val name = classOrVarName.name
            val symbolValue = getSymbolInfo(name)
            if (symbolValue != null && symbolValue.type is Type.ClassName) {
                // メソッド
                val className = symbolValue.type.name
                val paramNum = expList.count() + 1
                vmWriter.writePush(Segment.POINTER, 0)
                vmWriter.writeCall("$className.$subroutineName", paramNum)
            } else {
                // ファンクション
                val className = classOrVarName.name
                val paramNum = expList.count()
                vmWriter.writeCall("$className.$subroutineName", paramNum)
            }
        } else {
            // メソッド
            val paramNum = expList.count() + 1
            vmWriter.writePush(Segment.POINTER, 0)
            vmWriter.writeCall("$className.$subroutineName", paramNum)
        }
        vmWriter.writePop(Segment.TEMP, 0)
    }

    private fun compileExpression(exp: Expression) {
        val first = exp.expElms.first()
        if (exp.expElms.count() > 1) {
            val op = exp.expElms[1]
            val rest = exp.expElms.slice(2 until exp.expElms.count())
            if (first is ExpElm._Term && op is ExpElm._Op) {
                compileTerm(first.term)
                compileExpression(Expression(rest))
                compileOperand(op.op)
            }
        } else if (first is ExpElm._Term) {
            compileTerm(first.term)
        }
    }

    private fun compileTerm(term: Term) {
        if (term is Term.IntC) {
            vmWriter.writePush(Segment.CONSTANT, term.const)
        } else if (term is Term.KeyC ) {
            if(term.const == Keyword.This) {
                vmWriter.writePush(Segment.POINTER, 0)
            }
        } else if (term is Term.VarName) {
            val symbolInfo = getSymbolInfo(term.name) ?: throw Error("シンボルテーブルがおかしい ${term.name}")
            if (symbolInfo.attribute == Attribute.Field) {
                vmWriter.writePush(Segment.THIS, symbolInfo.index)
            } else if (symbolInfo.attribute == Attribute.Argument) {
                vmWriter.writePush(Segment.ARGUMENT, argIndex(symbolInfo.index, inMethod))
            } else if (symbolInfo.attribute == Attribute.Var) {
                vmWriter.writePush(Segment.LOCAL, symbolInfo.index)
            }
        } else if (term is Term._Expression) {
            compileExpression(term.exp)
        }
    }

    private fun compileOperand(op: Op) {
        if (op == Op.Plus) {
            vmWriter.writeArithmetic(Command.ADD)
        } else if (op == Op.Minus) {
            vmWriter.writeArithmetic(Command.SUB)
        } else if (op == Op.Asterisk) {
            vmWriter.writeCall("Math.multiply", 2)
        } else if (op == Op.Slash) {
            vmWriter.writeCall("Math.divide", 2)
        }
    }
}