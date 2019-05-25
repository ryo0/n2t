enum class Attribute {
    Static, Field, Argument, Var
}

enum class FuncAttribute {
    Constructor, Function, Method
}

val funcAttrMap = mapOf(
    MethodDec.Constructor to FuncAttribute.Constructor,
    MethodDec.Function to FuncAttribute.Function,
    MethodDec.Method to FuncAttribute.Method
)

fun convertToAttr(varDec: _ClassVarDec): Attribute {
    if (varDec == _ClassVarDec.Field) {
        return Attribute.Field
    } else {
        return Attribute.Static
    }
}

data class SymbolValue(val type: Type, val attribute: Attribute, val index: Int)
data class FuncValue(val type: VoidOrType, val attribute: FuncAttribute)

class SymbolTable(program: Class) {
    val classTable = mutableMapOf<String, SymbolValue>()
    val funAttrTable = mutableMapOf<String, FuncValue>()
    var fieldIndex = -1
    var staticIndex = -1
    var argIndex = 0
    var varIndex = 0

    init {
        program.varDec.forEach {
            it.varNames.forEach { name ->
                val attr = convertToAttr(it.varDec)
                if (attr == Attribute.Field) {
                    fieldIndex++
                    classTable[name] = SymbolValue(it.type, attr, fieldIndex)
                } else if (attr == Attribute.Static) {
                    staticIndex++
                    classTable[name] = SymbolValue(it.type, attr, staticIndex)
                }
            }
        }
        program.subroutineDec.forEach {
            val dec = funcAttrMap[it.dec] ?: throw Error("無効なfunDec")
            funAttrTable["${program.name}.${it.name}"] = FuncValue(it.type, dec)
            subroutineTableCreator(it)
        }
    }

    fun subroutineTableCreator(subroutine: SubroutineDec): Map<String, SymbolValue> {
        argIndex = 0
        varIndex = 0
        val table = mutableMapOf<String, SymbolValue>()
        subroutine.paramList.list.forEach {
            table[it.name] = SymbolValue(it.type, Attribute.Argument, argIndex)
            argIndex++
        }

        subroutine.body.varDecs.forEach { varDec ->
            varDec.vars.forEach { varName ->
                table[varName] = SymbolValue(varDec.type, Attribute.Var, varIndex)
                varIndex++
            }
        }
        return table
    }
}
