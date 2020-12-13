import java.io.File

data class BusLine(val frequency: Long, val contestOffset: Long) {
    val id: Long = frequency

    fun nextDepartureAfter(timestamp: Long): Long =
        ((timestamp / frequency) * frequency).let {
            if (it < timestamp) {
                it + frequency
            } else {
                it
            }
        }

    fun normalized(): BusLine =
        copy(contestOffset = contestOffset % frequency)
}

val input = File(args[0]).readLines()
val earliestDeparture = input[0].toLong()
val busLines = input[1]
    .split(",")
    .mapIndexed { index, it ->
        BusLine(it.toLongOrNull() ?: -1, index.toLong())
    }
    .filterNot { it.frequency == -1L }

// Part 1:
println(
    busLines
        .map { Pair(it, it.nextDepartureAfter(earliestDeparture)) }
        .minByOrNull { it.second }
        ?.let { it.first.id * (it.second - earliestDeparture) }
)

// Part 2:
// Attempt 1: too slow
// val candidates = generateSequence(0L) { it + busLines[0].frequency }
// Attempt 2:
//val candidates = busLines
//    .maxByOrNull { it.frequency }
//    ?.let { leastFrequentLine ->
//        generateSequence(leastFrequentLine.frequency - leastFrequentLine.contestOffset) {
//            it + leastFrequentLine.frequency
//        }
//    }!!
//println(
//    candidates
//        .first { timestamp ->
//            if (timestamp < 0) throw IllegalStateException("overflow")
//            busLines.all { busLine ->
//                (timestamp + busLine.contestOffset) % busLine.frequency == 0L
//            }
//        }
//)
//
// Nope, not gonna work.
//
// We note that
//  - we're looking for time t s.t. t == frequency - offset (modulo frequency)
//    for all bus lines, and
//  - the frequencies are co-prime (since they are prime)
// Thus, we can apply the Chinese remainder theorem and use sieving as
// described here:
// https://en.wikipedia.org/wiki/Chinese_remainder_theorem#Search_by_sieving

val maxFrequencyLine = busLines.maxByOrNull { it.frequency }!!
busLines
    .map { it.normalized() }
    .sortedByDescending { it.frequency }
    .drop(1) // smallest solution for only the first bus line becomes seed of fold
    .fold(
        Pair(
            maxFrequencyLine.frequency - maxFrequencyLine.contestOffset,
            maxFrequencyLine.frequency
        )
    ) { baseAndProduct, line -> // (xi, n1*...*ni
        generateSequence(baseAndProduct.first) { it + baseAndProduct.second } // xi + k * n1 * ... * ni
            .first { candidate -> (candidate + line.contestOffset) % line.frequency == 0L }
            .let { Pair(it, baseAndProduct.second * line.frequency) }
            .let { println(it); it }
        // --> first component is smallest solution for first i bus lines
    }
    .let { println(it.first) }
