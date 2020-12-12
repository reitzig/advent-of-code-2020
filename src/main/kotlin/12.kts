operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>): Pair<Int, Int> =
    Pair(first + other.first, second + other.second)

sealed class Foo(val vector: Pair<Int, Int>) {
    data class Bar(val v: Pair<Int, Int>) : Foo(v)

    operator fun plus(other: Foo): Foo =
        Bar(this.vector + other.vector)

    companion object {
        operator fun invoke(x: Int, y: Int): Foo =
            Bar(Pair(x, y))
    }
}

println("foo")