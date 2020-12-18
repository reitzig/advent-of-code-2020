@file:Include("shared.kt")

import java.io.File

enum class GridElement(val rep: Char) {
    Active('#'),
    Inactive('.');

    companion object {
        fun valueOfRep(rep: Char): GridElement =
            values().firstOrNull { it.rep == rep }
                ?: throw IllegalArgumentException("No element with rep $rep")
    }
}

interface Position<Self : Position<Self>> {
    val neighbours: Sequence<Self>
}

// Would have liked this refactoring to not re-compute neighbours all the time,
// but alas: https://youtrack.jetbrains.com/issue/KT-43995
data class Position3(val x: Int, val y: Int, val z: Int) : Position<Position3> {
    override val neighbours: Sequence<Position3> by lazy {
        neighbourOffsets
            .map { (dx, dy, dz) ->
                Position3(x + dx, y + dy, z + dz)
            }
    }

    companion object {
        private val neighbourOffsets = listOf(-1, 0, 1)
            .combinations(3)
            .asSequence()
            .filterNot { l -> l.all { it == 0 } }
    }
}

data class Position4(val x: Int, val y: Int, val z: Int, val zz: Int) : Position<Position4> {
    override val neighbours: Sequence<Position4> by lazy {
        neighbourOffsets
            .map { (dx, dy, dz, dzz) ->
                Position4(x + dx, y + dy, z + dz, zz + dzz)
            }
    }

    companion object {
        private val neighbourOffsets = listOf(-1, 0, 1)
            .combinations(4)
            .asSequence()
            .filterNot { l -> l.all { it == 0 } }
    }
}

data class PocketDimension<P : Position<P>>(var grid: MutableMap<P, GridElement>) {
    init {
        grid.keys.toList().forEach { expandAround(it) }
    }

    val activeElements: List<P>
        get() = grid.filterValues { it == GridElement.Active }
            .map { it.key }

    fun activeNeighbours(position: P): Int =
        position.neighbours.count { grid[it] == GridElement.Active }

    private fun expandAround(position: P) {
        if (grid[position] == GridElement.Active) {
            position.neighbours.forEach {
                grid.putIfAbsent(it, GridElement.Inactive)
            }
        }
    }

    fun blink(willGoInactive: (P) -> Boolean, willGoActive: (P) -> Boolean) {
        val toExpand = mutableListOf<P>()
        grid = grid.mapValues { (pos, elem) ->
            if (elem == GridElement.Active && willGoInactive(pos)) {
                GridElement.Inactive
            } else if (elem == GridElement.Inactive && willGoActive(pos)) {
                toExpand.add(pos)
                GridElement.Active
            } else {
                elem
            }
        }.toMutableMap()
        toExpand.forEach { expandAround(it) }
    }

    companion object {
        operator fun <P : Position<P>> invoke(
            initialConfiguration: List<String>,
            position: (Int, Int) -> P
        ): PocketDimension<P> =
            initialConfiguration
                .flatMapIndexed { x, row ->
                    row.mapIndexed { y, elem ->
                        Pair(position(x, y), GridElement.valueOfRep(elem))
                    }
                }
                .toMap().toMutableMap()
                .let { PocketDimension(it) }
    }
}

val input = File(args[0]).readLines()

// Part 1:
val pocketDimension3 = input.let {
    PocketDimension(it) { x, y -> Position3(x, y, 0) }
}
for (i in 1..6) {
    pocketDimension3.apply {
        blink({ activeNeighbours(it) !in 2..3 }, { activeNeighbours(it) == 3 })
    }
}
println(pocketDimension3.activeElements.size)

// Part 3:
val pocketDimension4 = input.let {
    PocketDimension(it) { x, y -> Position4(x, y, 0, 0) }
}
for (i in 1..6) {
    pocketDimension4.apply {
        blink({ activeNeighbours(it) !in 2..3 }, { activeNeighbours(it) == 3 })
    }
}
println(pocketDimension4.activeElements.size)