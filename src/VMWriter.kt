enum class Segment {
    CONSTANT, ARGUMENT, LOCAL, STATIC, THIS, THAT, POINTER, TEMP
}
enum class Command {
    ADD, SUB, NEG, EQ, GT, LT, AND, OR, NOT
}

class VMWriter(val className: String) {
    fun writePush(segment: Segment, index: Int) {
        val seg = segment.toString().toLowerCase()
        println("push $seg $index")
    }
    fun writePop(segment: Segment, index: Int) {
        val seg = segment.toString().toLowerCase()
        println("pop $seg $index")
    }
    fun writeArithmetic(command: Command) {
        println(command.toString().toLowerCase())
    }
    fun writeCall(name: String, nArgs: Int) {
        println("call $name $nArgs")
    }
    fun writeFunction(name: String, nArgs: Int) {
        println("function $className.$name $nArgs")
    }
    fun writeReturn() {
        println("return")
    }
}