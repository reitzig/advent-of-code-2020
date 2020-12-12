//INCLUDE shared.kt

import _11.Position
import java.io.File

// TODO: can't import _11.GridElement.EmptySeat -- why?

enum class GridElement(val rep: Char) {
    EmptySeat('L'),
    OccupiedSeat('#'),
    Floor('.');

    companion object {
        fun valueOfRep(rep: Char): GridElement =
            values().firstOrNull { it.rep == rep }
                ?: throw IllegalArgumentException("No element with rep $rep")
    }
}

typealias Position = Pair<Int, Int>

data class Seating(val grid: List<List<GridElement>>) {
    val occupiedSeats: Int =
        grid.map { row -> row.count { it == GridElement.OccupiedSeat } }.sum()

    fun occupiedNeighbours(position: Position): Int =
        listOf(-1, 0, 1)
            .combinations()
            .filterNot { it == Pair(0, 0) }
            .map { Position(position.first + it.first, position.second + it.second) }
            .filter {
                it.first in 0..grid.lastIndex
                        && it.second in 0..grid[position.first].lastIndex
            }
            .count { grid[it.first][it.second] == GridElement.OccupiedSeat }

    fun occupiedVisibles(position: Position): Int =
        listOf(-1, 0, 1)
            .combinations()
            .filterNot { it == Pair(0, 0) }
            .mapNotNull { direction ->
                // Get first seat (aka non-floor) in this direction
                position
                    .projectRay(direction) {
                        first in 0..grid.lastIndex && second in 0..grid[position.first].lastIndex
                    }
                    .filter { it != position }
                    .firstOrNull { grid[it.first][it.second] != GridElement.Floor }
            }
            .count { grid[it.first][it.second] == GridElement.OccupiedSeat }

    fun blink(willSitDown: (Position) -> Boolean, willGetUp: (Position) -> Boolean) =
        grid.mapIndexed { rowIndex, row ->
            row.mapIndexed { colIndex, it ->
                if (it == GridElement.EmptySeat && willSitDown(Position(rowIndex, colIndex))) {
                    GridElement.OccupiedSeat
                } else if (it == GridElement.OccupiedSeat && willGetUp(Position(rowIndex, colIndex))) {
                    GridElement.EmptySeat
                } else {
                    it
                }
            }
        }.let { Seating(it) }

    override fun toString() =
        grid.joinToString("\n") { row -> row.joinToString("") { "${it.rep}" } }
}

fun stableSeating(initial: Seating, step: (Seating).() -> Seating): Seating {
    var currentSeating = initial
    do {
        val lastSeating = currentSeating
        currentSeating = step(lastSeating)
    } while (currentSeating != lastSeating)
    return currentSeating
}

val seating = File(args[0])
    .readLines()
    .map { it.map { rep -> GridElement.valueOfRep(rep) } }
    .let { Seating(it) }

// Part 1:
println(
    stableSeating(seating) {
        blink({ occupiedNeighbours(it) == 0 }, { occupiedNeighbours(it) >= 4 })
    }.occupiedSeats
)
// Part 2:
println(
    stableSeating(seating) {
        blink({ occupiedVisibles(it) == 0 }, { occupiedVisibles(it) >= 5 })
    }.occupiedSeats
)
