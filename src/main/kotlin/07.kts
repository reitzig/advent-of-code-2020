import java.io.File

typealias Color = String

data class BagRule private constructor(val color: Color, val canContain: Map<Color, Int>) {

    companion object {
        val bagAmountPattern = Regex("(\\d+) ([a-z ]+) bags?")
        val rulePattern = Regex("^([a-z ]+) bags contain ([0-9a-z, ]+)\\.$")

        operator fun invoke(englishRule: String): BagRule {
            val match = rulePattern.matchEntire(englishRule)
                    ?: throw IllegalArgumentException("Invalid rule: '$englishRule'")

            val color = match.groupValues[1]
            val contains = if (match.groupValues[2] == "no other bags") {
                mapOf()
            } else {
                match.groupValues[2]
                        .split(", ")
                        .map {
                            bagAmountPattern.matchEntire(it)
                                    ?: throw IllegalArgumentException("Invalid bag amount: '$it'")
                        }
                        .map { it.groupValues.drop(1) }
                        .map { Pair(it.last(), it.first().toInt()) }
                        .toMap()
            }

            return BagRule(color, contains)
        }
    }

    override fun toString(): String {
        val included = if (canContain.isEmpty()) {
            "no other bags"
        } else {
            canContain.map {
                if (it.value > 1) {
                    "${it.value} ${it.key} bags"
                } else {
                    "${it.value} ${it.key} bag"
                }
            }.joinToString(", ")
        }
        return "$color bags contain $included."
    }
}

data class BagRules(val rules: Set<BagRule>) {
    fun holdersOf(color: Color): List<Color> {
        fun directHoldersOf(color: Color): Set<BagRule> =
                rules.filter { it.canContain.containsKey(color) }.toSet()

        // Fixpoint "iteration"
        fun allHoldersOf(someHolders: Set<BagRule>): Set<BagRule> {
            val directHolders = someHolders.flatMap { directHoldersOf(it.color) }.toSet()
            val moreHolders = someHolders.union(directHolders)

            return if (someHolders == moreHolders) {
                someHolders
            } else {
                allHoldersOf(moreHolders)
            }
        }

        return allHoldersOf(directHoldersOf(color)).map { it.color }
    }

    fun numberOfBagsIn(color: Color): Int? = numberOfBagsInAll()[color]

    fun numberOfBagsInAll(): Map<Color, Int?> {
        val numbers = rules
                .filter { it.canContain.isEmpty() }
                .map { Pair(it.color, 0) }
                .toMap<Color, Int?>().toMutableMap()

        fun computedNumbers(): Int = numbers.values.count { it != null }

        // Fixpoint iteration
        do {
            val alreadyComputedNumbers = computedNumbers()

            rules.filter { numbers[it.color] == null }
                    .map {
                        Pair(it.color, it.canContain.map { colorAndCount ->
                            numbers[colorAndCount.key] // the already computed number of bags _in_ a bag of that color
                                    ?.plus(1) // the bag of that color itself
                                    ?.times(colorAndCount.value) // the number bags of that color this one contains
                        })
                    }
                    .filter { pair ->
                        pair.second.all { count -> count != null }
                    }
                    .forEach { pair ->
                        numbers[pair.first] = pair.second.requireNoNulls().sum()
                    }
        } while (alreadyComputedNumbers < computedNumbers())

        return numbers
    }
}

val bagRules = BagRules(File(args[0]).readLines().map { BagRule(it) }.toSet())
// For checking the parsing:
// println(bagRules.rules.map { it.toString() }.joinToString("\n"))
// Part 1:
println(bagRules.holdersOf("shiny gold").size)
// Part 2:
println(bagRules.numberOfBagsIn("shiny gold"))
