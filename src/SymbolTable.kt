enum class Attribute {
    Static, Field, Argument, Var
}

fun convertToAttr(varDec: _ClassVarDec): Attribute {
    if (varDec == _ClassVarDec.Field) {
        return Attribute.Field
    } else {
        return Attribute.Static
    }
}

data class SymbolValue(val type: Type, val attribute: Attribute, val index: Int)

class SymbolTable(program: Class) {
    val classTable = mutableMapOf<String, SymbolValue>()

    init {
        var fieldIndex = 0
        var staticIndex = 0
        program.varDec.forEach {
            it.varNames.forEach { name ->
                val attr = convertToAttr(it.varDec)
                if (attr == Attribute.Field) {
                    classTable[name] = SymbolValue(it.type, attr, fieldIndex)
                    fieldIndex++
                } else if (attr == Attribute.Static) {
                    classTable[name] = SymbolValue(it.type, attr, staticIndex)
                    staticIndex++
                }
            }
        }
        program.subroutineDec.forEach {
            println(subroutineTableCreator(it))
        }
    }

    fun subroutineTableCreator(subroutine: SubroutineDec): Map<String, SymbolValue> {
        val table = mutableMapOf<String, SymbolValue>()
        var argIndex = 0
        subroutine.paramList.list.forEach {
            table[it.name] = SymbolValue(it.type, Attribute.Argument, argIndex)
            argIndex++
        }

        var varIndex = 0
        subroutine.body.varDecs.forEach { varDec ->
            varDec.vars.forEach {varName ->
                table[varName] = SymbolValue(varDec.type, Attribute.Var, varIndex)
                varIndex++
            }
        }
        return table
    }
}
