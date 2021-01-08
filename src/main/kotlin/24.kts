@file:Include("shared.kt")

import java.io.File

typealias Coordinate = Pair<Int, Int>

fun Coordinate.neighbours(): Sequence<Coordinate> =
    HexStep.values()
        .asSequence()
        .map { this + it.offset }

/* Note:
 - the reps are a prefix code, so we can parse without hassle
 - every row (Y coordinate) is occupied, but each row occupies only every other X coordinate
 */
enum class HexStep(val rep: String, val offset: Coordinate) {
    East("e", Coordinate(+2, 0)),
    SouthEast("se", Coordinate(+1, -1)),
    SouthWest("sw", Coordinate(-1, -1)),
    West("w", Coordinate(-2, 0)),
    NorthWest("nw", Coordinate(-1, +1)),
    NorthEast("ne", Coordinate(+1, +1));

    companion object {
        fun sequenceFrom(repString: String): Sequence<HexStep> {
            require(repString.matches(Regex("(${values().joinToString("|") { it.rep }})+")))

            var remainingCharacters = repString
            fun forward(offset: Int) {
                remainingCharacters = remainingCharacters.drop(offset)
            }

            return generateSequence {
                when (val prefix = remainingCharacters.take(2)) {
                    "" -> null
                    "e", "es", "en", "ew", "ee" -> {
                        forward(1); East
                    }
                    "se" -> {
                        forward(2); SouthEast
                    }
                    "sw" -> {
                        forward(2); SouthWest
                    }
                    "w", "ws", "wn", "ww", "we" -> {
                        forward(1); West
                    }
                    "nw" -> {
                        forward(2); NorthWest
                    }
                    "ne" -> {
                        forward(2); NorthEast
                    }
                    else -> throw IllegalArgumentException("Invalid rep string: suffix starts with $prefix")
                }
            }
        }
    }
}

fun Sequence<HexStep>.finalCoordinate(start: Coordinate = Coordinate(0, 0)): Coordinate =
    fold(start) { current, step -> current + step.offset }

enum class Color {
    Black,
    White
}

class TileExhibit(startingTiles: Map<Coordinate, Color>) {
    val tiles = startingTiles.toMutableMap()

    init {
        expand(tiles.filterValues { it == Color.Black }.keys)
    }

    val blackTiles: Int
        get() = tiles.count { it.value == Color.Black }

    private fun expand(newlyBlackTiles: Collection<Coordinate>) {
        newlyBlackTiles
            .flatMap { it.neighbours() }
            .forEach { tiles.putIfAbsent(it, Color.White) }
    }

    fun aDayPasses() {
        tiles
            // Determine flips
            .mapNotNull { (tile, color) ->
                val blackNeighbours = tile.neighbours().count { tiles[it] == Color.Black }
                return@mapNotNull when (color) {
                    Color.Black -> if (blackNeighbours !in 1..2) {
                        Pair(tile, Color.White)
                    } else {
                        null
                    }
                    Color.White -> if (blackNeighbours == 2) {
                        Pair(tile, Color.Black)
                    } else {
                        null
                    }
                }
            }
            // Perform flips
            .onEach { (tile, color) ->
                tiles[tile] = color
            }
            // Expand around new black tiles
            .filter { (_, color) -> color == Color.Black }
            .map { (tile, _) -> tile }
            .let { expand(it) }
    }

    override fun toString(): String {
        val (minX, maxX, minY, maxY) = tiles.keys
            .fold(Quadruple(0, 0, 0, 0)) { (minX, maxX, minY, maxY), tile ->
                Quadruple(
                    minOf(minX, tile.first),
                    maxOf(maxX, tile.first),
                    minOf(minY, tile.second),
                    maxOf(maxY, tile.second)
                )
            }

        return (minY..maxY).joinToString("\n") { x ->
            (minX..maxX).joinToString("") { y ->
                when (tiles[Coordinate(x, y)]) {
                    null -> " "
                    Color.Black -> "#"
                    Color.White -> "."
                }
            }
        }
    }
}

// Input:
val stepSequences = File(args[0]).readLines().map { HexStep.sequenceFrom(it) }
//    .map { it.toList() }

// Part 1:
val blackTiles = stepSequences
    .map { it.asSequence().finalCoordinate() }
    .groupBy { it }
    .filter { it.value.size % 2 == 1 } // odd number of flips means black side up!
    .keys
println(blackTiles.size)

// Part 2:
TileExhibit(blackTiles.map { Pair(it, Color.Black) }.toMap().toMutableMap())
    .apply {
//        println(this)
        for (day in 1..100) {
            aDayPasses()
//            println(this)
//            println("Day $day: $blackTiles")
        }
    }
    .also { println(it.blackTiles) }
