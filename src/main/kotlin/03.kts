import java.io.File

val map = File(args[0]).readLines()
val mapWidth = map[0].length
assert(args[0][0] == '.')
assert(args.all { it.length == mapWidth })

// I prefered this for part 1, but didn't see a neat way to generalize it
//var trees = map
//        .mapIndexed { rowIndex, row -> row[(rowIndex * 3) % mapWidth]}
//        .count { it == '#' }

fun metTrees(map: List<String>, shiftRight: Int, shiftDown: Int): Long {
    var trees = 0L
    for ( rowIndex in 0..map.lastIndex step shiftDown ) {
        val nextSquare = map[rowIndex][(rowIndex / shiftDown * shiftRight) % mapWidth]
        if ( nextSquare == '#' ) {
            trees += 1
        }
    }
    return trees
}

// Part 1
println(metTrees(map, 3, 1))

// Part 2
println(
    listOf(Pair(1,1), Pair(3,1), Pair(5,1), Pair(7,1), Pair(1,2))
            .map { metTrees(map, it.first, it.second) }
            .reduce(Long::times)
)
