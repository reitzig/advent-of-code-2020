@file:KotlinOpts("-J-ea")
@file:Include("shared.kt")

import java.io.File
import java.lang.IllegalStateException
import kotlin.math.roundToInt
import kotlin.math.sqrt

enum class Pixel(val rep: Char) {
    Up('#'),
    Down('.'),
    Monster('O');

    override fun toString(): String = "$rep"

    companion object {
        fun valueOf(rep: Char): Pixel =
            values().firstOrNull { it.rep == rep }
                ?: throw IllegalArgumentException("No element with rep $rep")
    }
}

fun <T> (List<List<T>>).column(index: Int): List<T> {
    assert(all { it.size > index }) { "Not a rectangle up to $index!" }
    return map { it[index] }
}

fun <T> (List<List<T>>).firstColumn(): List<T> = column(0)

fun <T> (List<List<T>>).lastColumn(): List<T> {
    assert(all { it.size == first().size }) { "Not a rectangle!" }
    return map { it.last() }
}

// This should be an inline class, but alas, not possible in Kotlin _Script_. :/
typealias PixelMap = List<List<Pixel>>

fun (PixelMap).orientations(): List<PixelMap> = rotations() + flipped().rotations()

fun (PixelMap).rotations(): List<PixelMap> {
    assert(all { it.size == size }) { "Not a square!" }
    return listOf(this) + listOf(
        { i: Int, j: Int -> Pair(j, size - i - 1) }, // turn left
        { i: Int, j: Int -> Pair(size - j - 1, i) }, // turn right
        { i: Int, j: Int -> Pair(size - i - 1, size - j - 1) }, // turn twice
    ).map { transformer ->
        (0 until size).map { i ->
            (0 until size).map { j ->
                val (thisI, thisJ) = transformer(i, j)
                this[thisI][thisJ]
            }
        }
    }
}

fun (PixelMap).flipped(): PixelMap = map { it.reversed() }

// Extensions/functions to work around https://youtrack.jetbrains.com/issue/KT-43995
fun (ImageTile).canNeighbour(other: ImageTile): Boolean =
    pixels.orientations().combinationsWith(other.pixels.orientations())
        .any { (orientedThis, orientedOther) ->
            orientedThis.first() == orientedOther.last() // this below other
                    || orientedThis.last() == orientedOther.first() // this above other
                    || orientedThis.lastColumn() == orientedOther.firstColumn() // this left of other
                    || orientedThis.firstColumn() == orientedOther.lastColumn() // this right of other
        }

fun (ImageTile).orientations(): List<OrientedImageTile> =
    pixels.orientations().map { OrientedImageTile(id, it) }

data class ImageTile(val id: Int, val pixels: PixelMap) {
// This would be useful, but alas, KT-43995 again. Didn't find a workaround :/
//    val pixelOrientations: List<PixelMap> by lazy { pixels.orientations() }

    override fun toString(): String =
        pixels.joinToString("\n", prefix = "Tile $id:\n") {
            it.joinToString("")
        }

    companion object {
        private val headerPattern = Regex("Tile (\\d+):")
        operator fun invoke(tileLines: List<String>): ImageTile {
            val id = headerPattern.matchEntire(tileLines.first())
                ?.groupValues?.get(1)?.toInt()
                ?: throw IllegalArgumentException("Invalid header line: ${tileLines.first()}")

            val pixels = tileLines.drop(1)
                .map { line ->
                    line.map { Pixel.valueOf(it) }
                }

            return ImageTile(id, pixels)
        }
    }
}

enum class NeighbourDirection {
    Above,
    RightOf,
    LeftOf,
    Below;
}

// Extensions to work around https://youtrack.jetbrains.com/issue/KT-43995
fun (OrientedImageTile).canBe(where: NeighbourDirection, other: OrientedImageTile): Boolean =
    when (where) {
        NeighbourDirection.Above -> this.pixels.last() == other.pixels.first()
        NeighbourDirection.RightOf -> this.pixels.firstColumn() == other.pixels.lastColumn()
        NeighbourDirection.Below -> this.pixels.first() == other.pixels.last()
        NeighbourDirection.LeftOf -> this.pixels.lastColumn() == other.pixels.firstColumn()
    }

fun (OrientedImageTile).canNeighbour(other: OrientedImageTile): Boolean =
    this.canBe(NeighbourDirection.Above, other)
            || this.canBe(NeighbourDirection.RightOf, other)
            || this.canBe(NeighbourDirection.Below, other)
            || this.canBe(NeighbourDirection.LeftOf, other)

data class OrientedImageTile(val originalId: Int, val pixels: PixelMap) {
    override fun toString(): String =
        pixels.joinToString("\n", prefix = "Oriented Tile $originalId:\n") {
            it.joinToString("")
        }
}

// Input:
val tiles = File(args[0]).readLines()
    .split("")
    .map { ImageTile(it) }
val fullImageTileSize = sqrt(tiles.size.toDouble()).roundToInt()
assert(fullImageTileSize * fullImageTileSize == tiles.size) { "Compute final image dimensions wrong" }

// Analysis:
assert(2 < tiles.map { tile -> tiles.count { other -> tile.canNeighbour(other) } }
    .sorted().minOrNull()!!) { "There were clear corner pieces after all!" }
// Unfortunately, there are no clear corner pieces, nor a clear center piece -- would have been too easy!

// Blow up the input by orienting all tiles in all possible ways:
val orientedTiles = tiles.flatMap { it.orientations() }
assert(orientedTiles.size == 8 * tiles.size) { "Computation of orientations went wrong!" }

// Analysis:
assert(5 > orientedTiles.map { tile ->
    orientedTiles.count { other ->
        tile.originalId != other.originalId && tile.canNeighbour(other)
    }
}.maxOrNull()!!) { "There were pieces with large neighbour count after all!" }
// Yes, all oriented tiles have few possible neighbours overall! So we can hope the search space
// is actually a lot smaller than it seems

fun assembleImage(pieces: List<OrientedImageTile>): List<List<OrientedImageTile>> {
    fun fillInImage(
        remainingPieces: List<OrientedImageTile>,
        partialImage: List<List<OrientedImageTile?>>,
        nextIndex: Pair<Int, Int> = Pair(0, 0)
    ): List<List<OrientedImageTile>>? {
        val (i, j) = nextIndex
        if (i > partialImage.lastIndex) {
            // Done, all filled!
            return partialImage.map { it.requireNoNulls() }
        } else if (j > partialImage[i].lastIndex) {
            // go to next row
            return fillInImage(remainingPieces, partialImage, Pair(i + 1, 0))
        }

        // We fill row by row, so we have to check (up to) to neighbours:
        val candidates = remainingPieces.filter { candidate ->
            partialImage.getOrNull(i)?.getOrNull(j - 1) // left neighbour
                ?.let { candidate.canBe(NeighbourDirection.RightOf, it) }
                    ?: true
                    && partialImage.getOrNull(i - 1)?.getOrNull(j) // above neighbour
                ?.let { candidate.canBe(NeighbourDirection.Below, it) }
                    ?: true
        }
        if (candidates.isEmpty()) {
            return null // backtrack
        }

        for (candidate in candidates) {
            return fillInImage(
                remainingPieces.filter { it.originalId != candidate.originalId },
                partialImage.replace(Pair(i, j), candidate),
                Pair(i, j + 1) // OOB handled above
            ) ?: continue
        }

        return null // backtrack
    }


    return fillInImage(
        pieces,
        List(fullImageTileSize) { List(fullImageTileSize) { null } }
    ) ?: throw IllegalStateException("No solution found")
}

fun (PixelMap).blotGroupsOfAllMatches(pattern: Regex, replacement: Char = Pixel.Monster.rep): PixelMap {
    val flattenedOrientations = orientations().map { orientation ->
        orientation.joinToString("\n") { it.joinToString("") }
    }

    var current = ""
    for (flattenedOrientation in flattenedOrientations) {
        current = flattenedOrientation
        do {
            val old = current
            current = old.replace(pattern) { match ->
                var wholeMatch = old.substring(match.range)
                match.groups.requireNoNulls().drop(1).forEach {
                    assert(it.range.count() == 1)
                    val indexInMatch = it.range.first - match.range.first
                    wholeMatch = wholeMatch.replaceRange(indexInMatch..indexInMatch, "$replacement")
                }
                wholeMatch
            }
        } while (current != old)

        if (current != flattenedOrientation) {
            break
        }
    }

    return current.split("\n").map { row -> row.map { Pixel.valueOf(it) } }
}

// Part 1:
val finalImageTiles = assembleImage(orientedTiles)
println(finalImageTiles.joinToString("\n") { row ->
    row.joinToString(" ") { it.originalId.toString() }
})
println(
    finalImageTiles.first().first().originalId.toLong()
            * finalImageTiles.first().last().originalId
            * finalImageTiles.last().first().originalId
            * finalImageTiles.last().last().originalId
)

// Part 2:
val fullImage = finalImageTiles
    .flatMap { rowOfTiles ->
        rowOfTiles.fold(List(rowOfTiles.first().pixels.size - 2) { listOf<Pixel>() }) { assembledTileRow, nextTile ->
            assembledTileRow
                .mapIndexed { i, assembledRow ->
                    assembledRow +
                            nextTile.pixels
                                .drop(1).dropLast(1) // Cut off upper and lower border
                                .get(i)
                                .drop(1).dropLast(1) // Cut off left and right border
                }
        }
    }
val fullImageSize = fullImage.size
assert(fullImage.all { it.size == fullImageSize }) { "Final image not square!" }

val seaMonster = """
                      # 
    #    ##    ##    ###
     #  #  #  #  #  #   
""".trimIndent()
val seaMonsterLength = (seaMonster.length + 1) / 3
val seaMonsterPattern = seaMonster
    .map { pixel ->
        when (pixel) {
            ' ' -> "[.#]"
            '#' -> "(#)"
            '\n' -> ".{${fullImageSize - seaMonsterLength + 2}}"
            else -> throw IllegalArgumentException("Unexpected monster pixel")
        }
    }
    .joinToString("")
    .let { Regex(it, RegexOption.DOT_MATCHES_ALL) }
val mapWithMonsters = fullImage.blotGroupsOfAllMatches(seaMonsterPattern)
println(ImageTile(-2, mapWithMonsters))
println(mapWithMonsters.map { row -> row.count { it == Pixel.Up } }.sum())

// 1300 - 1600
// 2350 - 0010
// 1010 - 1045