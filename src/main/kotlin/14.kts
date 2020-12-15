import java.io.File

sealed class Instruction {
    data class SetBitmask private constructor(val mask: String) : Instruction() {
        fun maskValue(instruction: AssignValue): AssignValue =
            instruction.value.toString(2)
                .padStart(mask.length, '0')
                .zip(mask)
                .map { (valueBit, maskBit) ->
                    when (maskBit) {
                        'X' -> valueBit
                        else -> maskBit
                    }
                }
                .joinToString("")
                .toLong(2)
                .let { AssignValue(instruction.address, it) }

        fun maskAddress(instruction: AssignValue): List<AssignValue> =
            instruction.address.toString(2)
                .padStart(mask.length, '0')
                .zip(mask)
                .map { (valueBit, maskBit) ->
                    when (maskBit) {
                        '0' -> listOf(valueBit)
                        '1' -> listOf('1')
                        'X' -> listOf('0', '1')
                        else -> throw IllegalStateException("invalid mask bit $maskBit")
                    }
                }
                .fold(listOf("")) { prefixes, bits ->
                    prefixes.flatMap { prefix ->
                        bits.map { bit ->
                            prefix + bit
                        }
                    }
                }
                .map { it.toLong(2) }
                .map { AssignValue(it, instruction.value) }

        companion object {
            private val pattern = Regex("^mask\\s*=\\s*([X01]+)$")
            fun maskValue(programLine: String): SetBitmask? =
                pattern.matchEntire(programLine)
                    ?.let {
                        SetBitmask(it.groupValues[1])
                    }

            val NOP = SetBitmask("X".repeat(36))
        }
    }

    data class AssignValue(val address: Long, val value: Long) : Instruction() {
        companion object {
            private val pattern = Regex("^mem\\[(\\d+)\\]\\s*=\\s*(\\d+)$")

            operator fun invoke(programLine: String): AssignValue? =
                pattern.matchEntire(programLine)
                    ?.let {
                        AssignValue(it.groupValues[1].toLong(), it.groupValues[2].toLong())
                    }
        }
    }

    companion object {
        fun maskValue(programLine: String): Instruction =
            AssignValue(programLine)
                ?: SetBitmask.maskValue(programLine)
                ?: throw IllegalArgumentException("Invalid instruction: '$programLine'")
    }
}

data class Memory(
    private var modifier: Instruction.SetBitmask = Instruction.SetBitmask.NOP,
    private val registers: MutableMap<Long, Long> = mutableMapOf()
) : Map<Long, Long> by registers {
    fun executeV1(instruction: Instruction) =
        when (instruction) {
            is Instruction.SetBitmask ->
                modifier = instruction
            is Instruction.AssignValue ->
                modifier.maskValue(instruction).run {
                    registers[address] = value
                }
        }

    fun executeV2(instruction: Instruction) =
        when (instruction) {
            is Instruction.SetBitmask ->
                modifier = instruction
            is Instruction.AssignValue ->
                modifier.maskAddress(instruction).forEach {
                    registers[it.address] = it.value
                }
        }

    override fun toString() = registers.toString()
}

val instructions = File(args[0]).readLines().map { Instruction.maskValue(it) }

// Part 1:
Memory().run {
    instructions.forEach { executeV1(it) }
    println(values.sum())
}

// Part 1:
Memory().run {
    instructions.forEach { executeV2(it) }
    println(values.sum())
}
