import java.io.File

data class Joltage(val value: Int) : Comparable<Joltage> {
    fun canTake(other: Joltage): Boolean =
        value <= other.value + 3

    override fun compareTo(other: Joltage): Int =
        value.compareTo(other.value)

    operator fun plus(difference: Int): Joltage =
        Joltage(value + difference)

    operator fun minus(other: Joltage): Int =
        value - other.value
}

fun countWorkingArrangements(sortedJoltages: List<Joltage>): Long {
    val countToEnd = Array<Long?>(sortedJoltages.size) { null }
    countToEnd[sortedJoltages.lastIndex] = 1

    for (i in (sortedJoltages.lastIndex - 1) downTo 0) {
        var count = 0L

        // Add together the counts for all possible next adapters
        var next = i + 1
        while (next <= sortedJoltages.lastIndex &&
            sortedJoltages[next].canTake(sortedJoltages[i])
        ) {
            count += countToEnd[next]!!
            next += 1
        }

        countToEnd[i] = count
    }

    return countToEnd[0]!!
}


val joltages = File(args[0])
    .readLines()
    .map { Joltage(it.toInt()) }
    .let { adapterJoltages ->
        adapterJoltages +
                listOf(Joltage(0),
                    adapterJoltages.maxOrNull()?.let { it + 3 })
                    .requireNoNulls()
    }
    .sorted()

// Part 1:
println(
    joltages
        .zipWithNext()
        .map { it.second - it.first }
        .groupBy({ it }, { 1 })
        .mapValues { it.value.sum() }
        .values
        .reduce(Int::times)
)
// Part 2:
println(countWorkingArrangements(joltages))
