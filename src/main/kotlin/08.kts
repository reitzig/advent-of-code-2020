import _08.Instruction.*
import _08.Stop.*
import java.io.File

sealed class Instruction(val parameter: Int) {
    data class Acc(val amount: Int) : Instruction(amount) {
        override fun newAccumulator(currentAcc: Int) = currentAcc + amount
    }

    data class Jump(val amount: Int) : Instruction(amount) {
        override fun nextLine(currentLine: Int): Int = currentLine + amount
    }

    data class Nop(val ignored: Int) : Instruction(ignored) {
        override fun toString(): String = "NOP"
    }

    open fun nextLine(currentLine: Int): Int = currentLine + 1
    open fun newAccumulator(currentAcc: Int): Int = currentAcc

    companion object {
        operator fun invoke(instructionLine: String): Instruction {
            val parts = instructionLine.split(" ")
            return when (parts[0]) {
                "nop" -> Nop(parts[1].toInt())
                "acc" -> Acc(parts[1].toInt())
                "jmp" -> Jump(parts[1].toInt())
                else -> throw IllegalArgumentException("unknown instruction '$instructionLine'")
            }
        }
    }
}

sealed class Stop(val result: Int) {
    data class Loop(val lastAccumulator: Int): Stop(lastAccumulator)
    data class Term(val finalAccumulator: Int): Stop(finalAccumulator)
}

data class Program(val instructions: List<Instruction>) : List<Instruction> by instructions {
    fun run(): Stop {
        val runLines = mutableSetOf<Int>()
        //println(program)

        var currentLine = 0
        var accumulator = 0
        while ( true ) {
            if ( runLines.contains(currentLine) ) {
                return Loop(accumulator)
            } else if ( currentLine >= instructions.size ) {
                return Term(accumulator)
            }

            runLines.add(currentLine)
            instructions[currentLine].run {
                //println("$this\t\t$accumulator")
                currentLine = nextLine(currentLine)
                accumulator = newAccumulator(accumulator)
            }
        }
    }

    fun withChange(line: Int, new: Instruction): Program =
        Program(toMutableList().apply { set(line, new) })

    fun flippingNopOrJump(): Sequence<Program> =
        instructions.asSequence().mapIndexedNotNull { index, instruction ->
            when ( instruction ) {
                is Nop -> withChange(index, Jump(instruction.parameter))
                is Jump -> withChange(index, Nop(instruction.parameter))
                is Acc -> null
            }
        }
}

val program = Program(File(args[0]).readLines().map { Instruction(it) })
// Part 1
println(program.run().result)
// Part 2
for (modifiedProgram in program.flippingNopOrJump()) {
    when (val stop = modifiedProgram.run()) {
        is Loop -> continue
        is Term -> {
            println(stop.finalAccumulator)
            break
        }
    }
}
