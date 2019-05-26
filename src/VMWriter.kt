import java.io.File

enum class Segment {
    CONSTANT, ARGUMENT, LOCAL, STATIC, THIS, THAT, POINTER, TEMP
}
enum class Command {
    ADD, SUB, NEG, EQ, GT, LT, AND, OR, NOT
}

class VMWriter(val className: String) {
    private var result = ""

    fun writeFile(path: String) {
        File(path).writeText(result)
    }

    fun writePush(segment: Segment, index: Int) {
        val seg = segment.toString().toLowerCase()
        result += "push $seg $index\n"
    }
    fun writePop(segment: Segment, index: Int) {
        val seg = segment.toString().toLowerCase()
        result += "pop $seg $index\n"
    }
    fun writeArithmetic(command: Command) {
        result += command.toString().toLowerCase() + "\n"
    }
    fun writeLabel(label: String) {
        result += "label $label\n"
    }
    fun writeGoto(label: String) {
        result += "goto $label\n"
    }
    fun writeIf(label: String) {
        result += "if-goto $label\n"
    }
    fun writeCall(name: String, nArgs: Int) {
        result += "call $name $nArgs\n"
    }
    fun writeFunction(name: String, nArgs: Int) {
        result += "function $className.$name $nArgs\n"
    }
    fun writeReturn() {
        result += "return\n"
    }
}