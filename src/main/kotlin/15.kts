//KOTLIN_OPTS -J-Xmx2g

import java.io.File

fun spokenNumbers(startingNumbers: List<Int>): Sequence<Int> {
    val lastAppearance = mutableMapOf(
        *startingNumbers
            .mapIndexed { index, number -> Pair(number, index) }
            .toTypedArray()
    )

    return startingNumbers.asSequence() +
            // Seed: As there are no repeats in the starting number,
            //       first derived number is always zero
            generateSequence(Pair(startingNumbers.size, 0)) { (lastIndex, lastNumber) ->
                if (lastAppearance.containsKey(lastNumber)) {
                    val number = lastIndex - lastAppearance[lastNumber]!!
                    lastAppearance[lastNumber] = lastIndex
                    Pair(lastIndex + 1, number)
                } else {
                    lastAppearance[lastNumber] = lastIndex
                    return@generateSequence Pair(lastIndex + 1, 0)
                }
            }.map { (_, number) -> number }
}

val startingNumbers = File(args[0]).readText().trim()
    .split(",")
    .map { it.toInt() }

// Part 1:
spokenNumbers(startingNumbers)
    .drop(2020 - 1)
    .first()
    .let { println(it) }

// Part 2:
spokenNumbers(startingNumbers)
    .drop(30000000 - 1)
    .first()
    .let { println(it) }