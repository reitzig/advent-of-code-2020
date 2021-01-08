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

fun <T> (MutableList<T>).removeAllAt(indices: IntRange): List<T> {
    val result = this.subList(indices.first, indices.last + 1).toList()
    indices.forEach { _ -> removeAt(indices.first) }
    return result
}

fun <T> (MutableList<T>).rotateLeft(by: Int) {
    for (i in 1..(by % size)) {
        add(removeAt(0))
    }
}

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

fun <T : Comparable<T>> List<T>.indexOfMaxOrNull(): Int? =
    mapIndexed { i, e -> Pair(i, e) }.maxByOrNull { it.second }?.first

fun <T, R : Comparable<R>> List<T>.indexOfMaxByOrNull(selector: (T) -> R): Int? =
    mapIndexed { i, e -> Pair(i, selector(e)) }.maxByOrNull { it.second }?.first

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

data class Quadruple<out A, out B, out C, out D>(
    public val first: A,
    public val second: B,
    public val third: C,
    public val fourth: D
) {
    override fun toString(): String = "($first, $second, $third, $fourth)"
}

fun <T> Quadruple<T, T, T, T>.toList(): List<T> = listOf(first, second, third, fourth)