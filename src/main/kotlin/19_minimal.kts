@file:Include("shared.kt")

import java.io.File

data class Rule(val index: Int, val alternatives: List<Either<out String, out List<Int>>>) {

    override fun toString(): String = "$index: ${alternatives.joinToString(" | ")}"

    companion object {
        operator fun invoke(ruleLine: String): Rule {
            val (index, rhs) = ruleLine.split(":").map { it.trim() }
            val alternatives = rhs
                .split("|")
                .map { it.trim() }
                .map { alternativeString ->
                    if (alternativeString.trim().startsWith("\"")) {
                        Either(alternativeString.drop(1).dropLast(1), null)
                    } else {
                        alternativeString
                            .split(Regex("\\s+"))
                            .map { it.toInt() }
                            .let { Either(null, it) }
                    }
                }

            return Rule(index.toInt(), alternatives)
        }
    }
}

data class Rules(val rules: Map<Int, Rule>) {
    fun toRegex(startRule: Int = 0): Regex {
        fun (Rule).toRegex(): Regex =
            alternatives
                .joinToString(separator = "|", prefix = "(?:", postfix = ")") {
                    it.t
                        ?: it.u?.map { r ->
                            rules[r]?.toRegex()
                        }?.requireNoNulls()
                            ?.joinToString("")
                        ?: throw IllegalStateException("Empty Either")
                }.let { Regex(it) }

        return rules[startRule]?.toRegex()
            ?: throw IllegalArgumentException("Start rule $startRule not found")
    }

    override fun toString(): String = rules.values.joinToString("\n")
}

val (ruleLines, messages) = File(args[0]).readLines().split("")
val rules = ruleLines
    .map { Rule(it) }
    .map { Pair(it.index, it) }
    .toMap()
    .let { Rules(it) }

println(messages.count { rules.toRegex(0).matches(it) })

fun (Rules).customMatches(message: String): Boolean {
    val rule42 = Regex("^${toRegex(42)}")
    val rule31 = Regex("^${toRegex(31)}")

    fun consumePrefixesMatching(pattern: Regex, text: String): Pair<Int, String> {
        var matches = 0
        var oldText = text
        do {
            val newText = oldText.replaceFirst(pattern, "")
            if (newText == oldText) {
                break
            } else {
                matches += 1
            }
            oldText = newText
        } while (oldText.isNotBlank())
        return Pair(matches, oldText)
    }

    val (seen42, after42) = consumePrefixesMatching(rule42, message)
    val (seen31, after31) = consumePrefixesMatching(rule31, after42)

    return after31.isBlank() && seen31 > 0 && seen42 > seen31
}
println(messages.count { rules.customMatches(it) })
