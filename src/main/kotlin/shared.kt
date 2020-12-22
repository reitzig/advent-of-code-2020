import kotlin.math.absoluteValue

data class Either<T, U>(val t: T? = null, val u: U? = null) {
    init {
        assert((t == null) != (u == null)) { "Exactly one parameter must be null" }
    }

    override fun toString(): String = "${t ?: ""}${u ?: ""}"
}

fun <T> List<T>.split(splitEntry: T): List<List<T>> =
    this.fold(mutableListOf(mutableListOf<T>())) { acc, line ->
        if (line == splitEntry) {
            acc.add(mutableListOf())
        } else {
            acc.last().add(line)
        }
        return@fold acc
    }

fun <T> List<T>.replace(index: Int, newElement: T): List<T> =
    mapIndexed { i, elem ->
        if (i == index) {
            newElement
        } else {
            elem
        }
    }

fun <T> List<T>.replace(index: Int, replacer: (T) -> T): List<T> =
    mapIndexed { i, elem ->
        if (i == index) {
            replacer(elem)
        } else {
            elem
        }
    }

fun <T> List<List<T>>.replace(index: Pair<Int, Int>, newElement: T): List<List<T>> =
    replace(index.first) { it.replace(index.second, newElement) }

fun <T, U> List<T>.combinationsWith(other: List<U>): List<Pair<T, U>> =
    flatMap { left -> other.map { right -> Pair(left, right) } }

fun <T> List<T>.combinations(): List<Pair<T, T>> =
    combinationsWith(this)

fun <T> List<T>.combinations(times: Int): List<List<T>> =
    when (times) {
        0 -> listOf()
        1 -> map { listOf(it) }
        else -> combinations(times - 1).let { tails ->
            tails.flatMap { tail -> this.map { head -> tail + head } }
        }
    }

fun <S, T, U> Pair<S, Pair<T, U>>.flatten(): Triple<S, T, U> =
    Triple(this.first, this.second.first, this.second.second)

//fun <S, T, U> Pair<Pair<S, T>, U>.flatten(): Triple<S, T, U> =
//    Triple(this.first.first, this.first.second, this.second)

operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>): Pair<Int, Int> =
    Pair(first + other.first, second + other.second)

operator fun Pair<Int, Int>.times(factor: Int): Pair<Int, Int> =
    Pair(first * factor, second * factor)

fun Pair<Int, Int>.manhattanFrom(origin: Pair<Int, Int> = Pair(0, 0)): Int =
    (origin.first - this.first).absoluteValue +
            (origin.second - this.second).absoluteValue

//fun Pair<Int, Int>.projectRay(): Sequence<Pair<Int, Int>> =
//    generateSequence(this) { it + this }

fun Pair<Int, Int>.projectRay(
    direction: Pair<Int, Int>,
    isInBounds: (Pair<Int, Int>).() -> Boolean = { true }
): Sequence<Pair<Int, Int>> =
    generateSequence(this) { (it + direction).takeIf(isInBounds) }