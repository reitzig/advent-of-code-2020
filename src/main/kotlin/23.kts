@file:KotlinOpts("-J-ea")
@file:Include("shared.kt")

import java.io.File

typealias Cup = Int

data class Cups(val circle: MutableList<Cup>) {
    constructor(circleString: String) : this(circleString.map { "$it".toInt() as Cup }.toMutableList())

    private val maxCupValue = circle.maxOrNull()!!
    private val circleSize = circle.size // avoid concurrent modification

    fun crabify(): MutableList<Cup> =
        circle.toMutableList()
            .also { it.addAll(((circle.maxOrNull()!! + 1)..1_000_000)) }

    fun takeTurn() {
//        println("cups: $this")
        val current = circle.first()
        val pickedUp = circle.removeAllAt(1..3)
//        println("picked up: $pickedUp")
        val destinationIndex =
            ((current - 1 downTo 1).asSequence() + (maxCupValue downTo current + 1).asSequence())
                .firstOrNull { !pickedUp.contains(it) }
                ?.let { circle.indexOf(it) }
                ?: throw IllegalStateException("All cups picked?")
        circle.addAll(destinationIndex + 1, pickedUp)
        circle.rotateLeft(1)
    }

    fun toAnswer(): String =
        circle.indexOf(1).let { startIndex ->
            (circle.subList(startIndex, circle.size) + circle.subList(0, startIndex))
                .drop(1)
                .joinToString("")
        }

    fun cupsWithStars(): List<Cup> =
        circle.indexOf(1).let { listOf((it + 1) % circleSize, (it + 2) % circleSize) }

    override fun toString(): String =
        circle.mapIndexed { i, c -> if (i == 0) "($c)" else c }.joinToString(" ")
}

fun play(startingArrangement: Cups, rounds: Int = 100) {
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
val cups = Cups(File(args[0]).readText().trim())

// Part 1:
with(cups.copy()) {
    play(this, 100)
    println(toAnswer())
}

// Part 2:
with(Cups(cups.crabify())) {
    assert(circle.size == 1_000_000) { "wrong number of cups: ${circle.size}" }
    assert(circle.distinct().size == 1_000_000) { "duplicate numbers!" }
    play(this, 10_000_000)
    println(cupsWithStars().also { println(it) }.map(Int::toLong).reduce(Long::times))
}

// 1000 -