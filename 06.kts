import java.io.File

// From 04.kts:
fun <T> List<T>.split(splitEntry: T): List<List<T>> =
        this.fold(mutableListOf(mutableListOf<T>())) { acc, line ->
            if (line == splitEntry) {
                acc.add(mutableListOf())
            } else {
                acc.last().add(line)
            }
            return@fold acc
        }

// ---

inline fun indexOf(c: Char): Int = c.toInt() - 97;

inline fun parikhOf(s: String): UInt {
    var parikh = CharArray(26) { i -> '0' }
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
