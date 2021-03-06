import java.io.File

data class Policy(val min: Int, val max: Int, var char: Char) {
    fun admitsByOldRules(password: String): Boolean {
        return password.count { it == char } in min..max
    }

    fun admitsByNewRules(password: String): Boolean {
        return (password[min - 1] == char) != (password[max - 1] == char)
    }
}

val linePattern = Regex("^(\\d+)-(\\d+)\\s+(\\w):\\s*(\\w+)$")

// ---

val input = File(args[0]).readLines().map { line ->
    val match = linePattern.matchEntire(line)
            ?: throw IllegalArgumentException("invalid line '${line}'")

    match.groupValues.let {
        assert(it[3].length == 1)
        return@map Pair(
                Policy(it[1].toInt(), it[2].toInt(), it[3].first()),
                it[4])
    }
}

println(input.filter { it.first.admitsByOldRules(it.second) }.size)
println(input.filter { it.first.admitsByNewRules(it.second) }.size)
