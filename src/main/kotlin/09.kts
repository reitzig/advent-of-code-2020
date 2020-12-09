import _09.Result.*
import java.io.File

sealed class Result {
    data class Wrong(val firstBadNumber: Long) : Result()
    object Okay : Result()
}

fun canSum(summands: List<Long>, number: Long): Boolean {
    for (i in summands.indices) {
        for (j in summands.indices) {
            if (i == j) continue

            if (summands[i] + summands[j] == number) {
                return true
            }
        }
    }

    return false
}

fun checkSequence(numbers: List<Long>, preambleLength: Int): Result {
    var minAllowedIndex = 0
    for (number in numbers.drop(preambleLength)) {
        val allowedSummands = numbers.slice(minAllowedIndex until minAllowedIndex + preambleLength)
        if (!canSum(allowedSummands, number)) {
            return Wrong(number)
        }
        minAllowedIndex += 1
    }

    return Okay
}

fun findSublistWithSum(numbers: List<Long>, sum: Long): List<Long> {
    for (i in numbers.indices) {
        for (j in i + 1..numbers.lastIndex) {
            val sub = numbers.subList(i, j + 1)
            when (sub.sum()) {
                in 0 until sum -> continue
                sum -> return sub
                else -> break
            }
        }
    }

    throw IllegalArgumentException("no such subsequence")
}

//val preambleLength = 5 // example
val preambleLength = 25 // input
val numbers = File(args[0]).readLines().map { it.toLong() }
// Part 1:
when (val result = checkSequence(numbers, preambleLength)) {
    is Okay -> println("okay")
    is Wrong -> {
        println(result)
        // Part 2
        findSublistWithSum(numbers, result.firstBadNumber).let {
            assert(it.isNotEmpty())
            println(it.minOrNull()!! + it.maxOrNull()!!)
        }
    }
}