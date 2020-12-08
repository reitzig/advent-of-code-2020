//INCLUDE shared.kt

import java.io.File

fun indexOf(c: Char): Int = c.toInt() - 97;

fun parikhOf(s: String): UInt {
    val parikh = CharArray(26) { i -> '0' }
    s.forEach { parikh[indexOf(it)] = '1' }
    return parikh.concatToString().toUInt(radix = 2)
}

val input = File(args[0]).readLines()

// Part 1:
println(
    input.split("").asSequence()
        .map { it.map(::parikhOf) }
        .map { it.reduce(UInt::or) }
        .map { it.toString(radix = 2) }
        .map { it.count { it == '1' } }
        .sum()
)

// Part 2:
println(
    input.split("").asSequence()
        .map { it.map(::parikhOf) }
        .map { it.reduce(UInt::and) }
        .map { it.toString(radix = 2) }
        .map { it.count { it == '1' } }
        .sum()
)
