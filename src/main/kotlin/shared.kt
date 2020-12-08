

fun <T> List<T>.split(splitEntry: T): List<List<T>> =
    this.fold(mutableListOf(mutableListOf<T>())) { acc, line ->
        if (line == splitEntry) {
            acc.add(mutableListOf())
        } else {
            acc.last().add(line)
        }
        return@fold acc
    }