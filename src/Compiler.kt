// THIS, THATは値が場合に応じて変わるのでその都度Pointerでセットしてる
// THISはthis, THATは配列の現在地の意味

class Compiler(private val _class: Class) {
    val className = _class.name
    private val table = SymbolTable(_class)
    private var subroutineTable: Map<String, SymbolValue>? = null
    private val vmWriter = VMWriter(className)
    private var inMethod = false
    private var ifLabelCounter = 0
    private var whileLabelCounter = 0

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
                is Stmt.If -> {
                    compileIf(it.stmt)
                }
                is Stmt.While -> {
                    compileWhile(it.stmt)
                }
            }
        }
    }

    private fun compileIf(stmt: IfStatement) {
        val exp = stmt.expression
        val ifStmts = stmt.ifStmts
        val elseStmts = stmt.elseStmts
        compileExpression(exp)
        val ifLabel = "IfLabel-$className-$ifLabelCounter"
        val elseLabel = "ElseLabel-$className-$ifLabelCounter"
        val endLabel = "IfEndLabel-$className-$ifLabelCounter"
        ifLabelCounter++
        vmWriter.writeIf(ifLabel)
        vmWriter.writeGoto(elseLabel)
        vmWriter.writeLabel(ifLabel)
        compileStatements(ifStmts)
        vmWriter.writeGoto(endLabel)
        vmWriter.writeLabel(elseLabel)
        compileStatements(elseStmts)
        vmWriter.writeLabel(endLabel)

    }

    private fun compileWhile(stmt: WhileStatement) {
        val exp = stmt.expression
        val label1 = "WhileLabel-Top-$className-$whileLabelCounter"
        val label2 = "WhileLabel-Bottom-$className-$whileLabelCounter"
        whileLabelCounter++
        val whileStmts = stmt.statements
        vmWriter.writeLabel(label1)
        compileExpression(exp)
        vmWriter.writeArithmetic(Command.NOT)
        vmWriter.writeIf(label2)
        compileStatements(whileStmts)
        vmWriter.writeGoto(label1)
        vmWriter.writeLabel(label2)
    }

    private fun compileReturn(stmt: ReturnStatement) {
        if (stmt.expression == null) {
            vmWriter.writePush(Segment.CONSTANT, 0)
        } else {
            compileExpression(stmt.expression)
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
        compileSubroutineCall(doStatement.subroutineCall)
        // do文では必ず返り値が捨てられる
        vmWriter.writePop(Segment.TEMP, 0)
    }

    private fun compileSubroutineCall(subroutineCall: SubroutineCall) {
        val classOrVarName = subroutineCall.classOrVarName
        val subroutineName = subroutineCall.subroutineName.name
        val expList = subroutineCall.expList.expList
        val className = classOrVarName?.name ?: className
        val funcValue = table.funAttrTable["$className.$subroutineName"]

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
                // ファンクションかコンストラクタ。やることは同じ
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
        if(funcValue != null && funcValue.type == VoidOrType.Void) {
            // ここ要らないかも。ダメだったら復活させる事を考える
//            vmWriter.writePop(Segment.TEMP, 0)
        }
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
        when (term) {
            is Term.IntC -> {
                vmWriter.writePush(Segment.CONSTANT, term.const)
            }
            is Term.KeyC -> {
                when (term.const) {
                    Keyword.This -> {
                        vmWriter.writePush(Segment.POINTER, 0)
                    }
                    Keyword.True -> {
                        vmWriter.writePush(Segment.CONSTANT, 0)
                        vmWriter.writeArithmetic(Command.NOT)
                    }
                    Keyword.False, Keyword.Null -> {
                        vmWriter.writePush(Segment.CONSTANT, 0)
                    }
                }

            }
            is Term.VarName -> {
                val symbolInfo = getSymbolInfo(term.name) ?: throw Error("シンボルテーブルがおかしい ${term.name}")
                when (symbolInfo.attribute) {
                    Attribute.Field -> {
                        vmWriter.writePush(Segment.THIS, symbolInfo.index)
                    }
                    Attribute.Argument -> {
                        vmWriter.writePush(Segment.ARGUMENT, argIndex(symbolInfo.index, inMethod))
                    }
                    Attribute.Var -> {
                        vmWriter.writePush(Segment.LOCAL, symbolInfo.index)
                    }
                }
            }
            is Term._Expression -> {
                compileExpression(term.exp)
            }
            is Term.UnaryOpTerm -> {
                if (term.op == UnaryOp.Minus) {
                    compileTerm(term.term)
                    vmWriter.writeArithmetic(Command.NEG)
                } else if (term.op == UnaryOp.Tilde) {
                    compileTerm(term.term)
                    vmWriter.writeArithmetic(Command.NOT)
                }
            }
            is Term._SubroutineCall -> {
                compileSubroutineCall(term.call)
            }
        }
    }

    private fun compileOperand(op: Op) {
        when (op) {
            Op.Plus -> {
                vmWriter.writeArithmetic(Command.ADD)
            }
            Op.Minus -> {
                vmWriter.writeArithmetic(Command.SUB)
            }
            Op.Asterisk -> {
                vmWriter.writeCall("Math.multiply", 2)
            }
            Op.Slash -> {
                vmWriter.writeCall("Math.divide", 2)
            }
            Op.Equal -> {
                vmWriter.writeArithmetic(Command.EQ)
            }
            Op.GreaterThan -> {
                vmWriter.writeArithmetic(Command.GT)
            }
            Op.LessThan -> {
                vmWriter.writeArithmetic(Command.LT)
            }
            Op.And -> {
                vmWriter.writeArithmetic(Command.AND)
            }
            Op.Pipe -> {
                vmWriter.writeArithmetic(Command.OR)
            }
        }
    }
}