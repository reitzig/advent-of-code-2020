@file:KotlinOpts("-J-ea")

import java.io.File

// Let's build a linked circle!
// Rationale: Performing the arrangements and lookups necessary to take a turn
//            is inefficient using either ArrayList or LinkedList from the std lib.
//            As the operations are otherwise trivial, flying our own seems feasible.
//            Cheat: CupsGame maintains an index value->Cup to avoid linear scans.

data class Cup(val value: Int, var nextInCircle: Cup? = null) {
    override fun toString(): String {
        return "Cup($value, ->${nextInCircle?.value})"
    }
}

class CupsGame(cupValues: Sequence<Int>) {
    private var currentCup: Cup

    /**
     * Maps value to element in linked circle!
     */
    private val cupIndex: Map<Int, Cup>
    private val maxCupValue = cupValues.maxOrNull()!!

    init {
        val cups = cupValues.map { Cup(it) }.toList()
        cups.zipWithNext { a, b -> a.nextInCircle = b }
        cups.last().nextInCircle = cups.first()
        currentCup = cups.first()
        assert(cups.all { it.nextInCircle != null }) { "Gap in the circle!" }

        cupIndex = cups.map { Pair(it.value, it) }.toMap()
    }

    val circleSize: Int
        get() = cupIndex.size

    fun takeTurn() {
//        println("cups: $this")
        val pickedUp = asSequence(currentCup).drop(1).take(3).toList()
        val pickedUpValues = pickedUp.map { it.value }
//        println("picked up: $pickedUpValues")
        val destinationCup =
            ((currentCup.value - 1 downTo 1).asSequence() + (maxCupValue downTo currentCup.value + 1).asSequence())
                .firstOrNull { !pickedUpValues.contains(it) }
                ?.let { cupIndex[it]!! }
                ?: throw IllegalStateException("All cups picked?")
//        println("destination: ${destinationCup.value}")

        // Rewire:
        //      currentCup -> [pickedUp] -> A   ...  destinationCup -> B
        // -->  currentCup -> A                 ... destinationCup -> [pickedUp] -> B
        currentCup.nextInCircle = pickedUp.last().nextInCircle
        val tmpB = destinationCup.nextInCircle!!
        destinationCup.nextInCircle = pickedUp.first()
        pickedUp.last().nextInCircle = tmpB

        // And prepare for next turn:
        currentCup = currentCup.nextInCircle!!
    }

    fun asSequence(startingCup: Cup = currentCup) =
        generateSequence(startingCup) { lastCup -> lastCup.nextInCircle.takeIf { it != startingCup } }

    fun toAnswer(): String =
        cupIndex[1]?.let { cup1 ->
            asSequence(cup1).drop(1).joinToString("") { "${it.value}" }
        } ?: throw IllegalStateException("We lost cup 1!")

    fun cupsWithStars(): List<Cup> =
        cupIndex[1]?.let { cup1 ->
            asSequence(cup1).drop(1).take(2).toList()
        } ?: throw IllegalStateException("We lost cup 1!")

    override fun toString(): String =
        asSequence()
            .map { if (it == currentCup) "(${it.value})" else it.value }
            .joinToString(" ")
}

fun crabify(cupValues: Sequence<Int>): Sequence<Int> =
    cupValues + ((cupValues.maxOrNull()!! + 1)..1_000_000).asSequence()

fun play(startingArrangement: CupsGame, rounds: Int = 100) {
    for (round in 1..rounds) {
//        println("-- move $round --")
        startingArrangement.takeTurn()
//        println("")
    }

//    """
//        --- final --
//        cups: $startingArrangement
//    """.trimIndent().let { println(it) }
}

// Input:
val cupValues = File(args[0]).readText().trim().map { "$it".toInt() }.asSequence()

// Part 1:
with(CupsGame(cupValues)) {
    play(this, 100)
    println(toAnswer())
}

// Part 2:
with(CupsGame(crabify(cupValues))) {
    assert(circleSize == 1_000_000) { "wrong number of cups: $circleSize" }
    assert(asSequence().map { it.value }.distinct().count() == 1_000_000) { "duplicate numbers!" }
    play(this, 10_000_000)
    cupsWithStars()
//        .also { println(it) }
        .map { it.value.toLong() }
        .reduce(Long::times)
        .also { println(it) }
}
