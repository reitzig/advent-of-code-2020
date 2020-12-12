fun <T> List<T>.split(splitEntry: T): List<List<T>> =
    this.fold(mutableListOf(mutableListOf<T>())) { acc, line ->
        if (line == splitEntry) {
            acc.add(mutableListOf())
        } else {
            acc.last().add(line)
        }
        return@fold acc
    }

fun <T, U> List<T>.combinationsWith(other: List<U>): List<Pair<T, U>> =
    flatMap { left -> other.map { right -> Pair(left, right) } }

fun <T> List<T>.combinations(): List<Pair<T, T>> =
    combinationsWith(this)

operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>): Pair<Int, Int> =
    Pair(first + other.first, second + other.second)

//fun Pair<Int, Int>.projectRay(): Sequence<Pair<Int, Int>> =
//    generateSequence(this) { it + this }

fun Pair<Int, Int>.projectRay(
    direction: Pair<Int, Int>,
    isInBounds: (Pair<Int, Int>).() -> Boolean = { true }
): Sequence<Pair<Int, Int>> =
    generateSequence(this) { (it + direction).takeIf(isInBounds) }