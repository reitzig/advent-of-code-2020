fun matchingPair(entries: List<Int>, target: Int): Pair<Int, Int>? {
    val sortedEntries = entries.sorted();
    var left = 0
    var right = entries.lastIndex

    while (left < right ) {
        val sum = sortedEntries[left] + sortedEntries[right]
        if (sum < target) {
            left += 1
        } else if ( sum > target ) {
            right -= 1
        } else {
            assert(sum == target)
            return Pair(sortedEntries[left], sortedEntries[right])
        }
    }

    return null
}

fun matchingTriplet(entries: List<Int>, target: Int): Triple<Int, Int, Int>? {
    return entries.mapIndexed { index, entry ->
        // ^_^
        val matchingPair = matchingPair(
                entries.filterIndexed { i, _ -> i != index },
                target - entry)

        if ( matchingPair != null ) {
            return@mapIndexed Triple(entry, matchingPair.first, matchingPair.second)
        } else {
            return@mapIndexed null
        }
    }.filterNotNull().firstOrNull()
}

// ---

val entries = args.map(String::toInt)

val pair = matchingPair(entries, 2020)
if ( pair != null ) {
    assert(pair.first + pair.second == 2020)
    println(pair.first * pair.second)
} else {
    println("No matching pair!")
}

val triplet = matchingTriplet(entries, 2020)
if ( triplet != null ) {
    assert(triplet.first + triplet.second + triplet.third == 2020)
    println(triplet.first * triplet.second * triplet.third)
} else {
    println("No matching triplet!")
}

// --> kscript 01.kts $(cat 01_input)