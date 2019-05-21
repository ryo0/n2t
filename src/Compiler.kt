import kotlin.math.exp

class Compiler(private val _class: Class) {
    val className = _class.name
    private val table = SymbolTable(_class)
    private var subroutineTable: Map<String, SymbolValue>? = null
    private val vmWriter = VMWriter(className)

    fun compileClass() {
        _class.subroutineDec.forEach { compileSubroutine(it) }
    }

    private fun compileSubroutine(subroutineDec: SubroutineDec) {
        subroutineTable = table.subroutineTableCreator(subroutineDec)
        vmWriter.writeFunction(subroutineDec.name, subroutineDec.paramList.list.count())
        compileStatements(subroutineDec.body.statements)
        val type = subroutineDec.type
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
        if(stmt.expression == null) {
            vmWriter.writePush(Segment.CONST, 0)
            vmWriter.writeReturn()
        }
    }

    private fun compileLetStatement(letStatement: LetStatement) {
        val table = subroutineTable ?: throw Error("let: subroutineTableがnull")
        val symbolInfo = table[letStatement.varName.name] ?: throw Error("subroutineテーブルに無い")
        val index = symbolInfo.index
        val exp = letStatement.exp
        compileExpression(exp)
        if (symbolInfo.attribute == Attribute.Argument) {
            vmWriter.writePop(Segment.ARG, index)
        } else if (symbolInfo.attribute == Attribute.Var) {
            vmWriter.writePop(Segment.LOCAL, index)
        }
    }

    private fun compileDoStatement(doStatement: DoStatement) {
        val classOrVarName = doStatement.subroutineCall.classOrVarName
        val subroutineName = doStatement.subroutineCall.subroutineName
        val expList = doStatement.subroutineCall.expList.expList
        if (classOrVarName != null) {
            expList.forEach { compileExpression(it) }
            vmWriter.writeCall("$className.${subroutineName.name}", expList.count())
        } else {
            expList.forEach { compileExpression(it) }
            vmWriter.writeCall(subroutineName.name, expList.count())
        }
        vmWriter.writePop(Segment.TEMP,0)
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
            vmWriter.writePush(Segment.CONST, term.const)
        } else if (term is Term.VarName) {
            val table = subroutineTable ?: throw Error("let: subroutineTableがnull")
            val symbolInfo = table[term.name] ?: throw Error("subroutineテーブルに無い")
            if (symbolInfo.attribute == Attribute.Argument) {
                vmWriter.writePush(Segment.ARG, symbolInfo.index)
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