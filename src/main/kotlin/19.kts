@file:KotlinOpts("-J-ea")
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
            if (ruleOverrides.containsKey(index)) {
                ruleOverrides[index]?.let { it(this@Rules) }
                    ?: throw IllegalStateException("concurrent modification")
            } else {
                alternatives
                    .joinToString(separator = "|", prefix = "(?:", postfix = ")") {
                        it.t
                            ?: it.u?.map { r ->
                                rules[r]?.toRegex()
                            }?.requireNoNulls()
                                ?.joinToString("")
                            ?: throw IllegalStateException("Empty Either")
                    }.let { Regex(it) }
            }

        return rules[startRule]?.toRegex()
            ?: throw IllegalArgumentException("Start rule $startRule not found")
    }

    private val ruleOverrides = mutableMapOf<Int, (Rules).() -> Regex>()

    fun setCustomOverride(ruleIndex: Int, customRegex: (Rules).() -> Regex) {
        ruleOverrides[ruleIndex] = customRegex
    }

    // Only needed for analysis:
    fun dependsOn(startIndex: Int = 0): List<Int> {
        fun dependsOnRec(index: Int, alreadySeen: Set<Int>): Set<Int> =
            (rules[index] ?: throw IllegalStateException("Rules $index not found"))
                .alternatives
                .mapNotNull { it.u }
                .reduceOrNull(List<Int>::plus)
                .orEmpty()
                .let { dependencies ->
                    dependencies union
                            dependencies
                                .filterNot { alreadySeen.contains(it) }
                                .flatMap { dependsOnRec(it, alreadySeen + index) }
                }

        return dependsOnRec(startIndex, mutableSetOf()).toSortedSet().toList()
    }

    override fun toString(): String = rules.values.joinToString("\n")
}

val (ruleLines, messages) = File(args[0]).readLines().split("")
val rules = ruleLines
    .map { Rule(it) }
    .map { Pair(it.index, it) }
    .toMap()
    .let { Rules(it) }

// Part 1:
println(messages.count { rules.toRegex(0).matches(it) })

// Part 2: Setup & Analysis
val newRules = listOf(
    Rule("8: 42 | 42 8"),
    Rule("11: 42 31 | 42 11 31")
)
    .map { Pair(it.index, it) }
    .toMap()
    .let {
        val r = rules.rules.toMutableMap()
        r.putAll(it)
        Rules(r)
    }
assert(2 == newRules.rules.filterValues { newRules.dependsOn(it.index).contains(it.index) }.size)
// --> 8 -> 8 and 11 --> 11 are the only cycles, so we can override them in isolation!
assert(listOf(0, 11) == newRules.rules.filterValues { newRules.dependsOn(it.index).contains(11) }.map { it.value.index }
    .sorted())
assert(listOf(8, 11) == newRules.rules[0]!!.alternatives.flatMap { it.u!! }.distinct().sorted())
// --> rule 11 appears exactly once
// --> can use regexp from https://stackoverflow.com/a/3644267/539599 with its fixed matching group index!
//     (it will be the only one in the full expression, phew)
newRules.setCustomOverride(8) {
    Regex("(?:${toRegex(42)}+)")
}
newRules.setCustomOverride(11) {
    val rule42 = toRegex(42)
    val rule31 = toRegex(31)
    Regex("(?:(?:${rule42}(?=${rule42}*(\\1?+${rule31})))+\\1)")
}

// Part 2:
println(messages.count { newRules.toRegex(0).matches(it) })
// Unfortunately, this is not right. So, abandon the regex approach and hard-code the start rule 0 -> 8 11
// Note that it combines to 0 -> 42+ 31+ with #42 >= #31 + 1
fun (Rules).customMatches(message: String): Boolean {
    val rule42 = Regex("^${toRegex(42)}")
    val rule31 = Regex("^${toRegex(31)}")

    // NB: This works by sheer luck only. Depending on rules and input,
    //     whichever matching this eager loop finds may or may not turn out
    //     to be the right choice. (Roughly speaking, rule42 and rule31 both
    //     have to represent prefix(-free) codes.
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
// Part 2, attempt 2:
println(messages.count { newRules.customMatches(it) })
