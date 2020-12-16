import java.io.File

data class Rule(val name: String, val ranges: List<IntRange>) {
    fun admits(value: Int): Boolean =
        ranges.any { it.contains(value) }

    companion object {
        operator fun invoke(ruleLine: String): Rule {
            val (name, rangesString) = ruleLine.split(":")
            val ranges = rangesString
                .split("or")
                .map { rangeString ->
                    rangeString.split("-")
                        .map { it.trim().toInt() }
                        .let { IntRange(it[0], it[1]) }
                }

            return Rule(name, ranges)
        }
    }
}

data class Ticket(val fields: List<Int>) {
    companion object {
        operator fun invoke(ticketLine: String): Ticket =
            ticketLine
                .split(",")
                .map { it.toInt() }
                .let { Ticket(it) }
    }
}

fun possibleRulesForField(fieldIndex: Int, tickets: List<Ticket>, rules: List<Rule>): List<Rule> =
    rules.filter { rule ->
        tickets.all { ticket ->
            rule.admits(ticket.fields[fieldIndex])
        }
    }

fun disambiguateRuleAssignments(possibleRules: List<Pair<Int, List<Rule>>>): Map<Int, Rule> {
    fun disambiguate(possibleAssignments: List<Pair<Int, List<Rule>>>): List<Pair<Int, List<Rule>>> =
        when (possibleAssignments.size) {
            0 -> listOf()
            else -> {
                val agreedAssignments = possibleAssignments
                    .filter { (_, rules) -> rules.size == 1 }
                val agreedRules = agreedAssignments
                    .flatMap { it.second }
                    .distinct()

                agreedAssignments +
                        possibleAssignments.subtract(agreedAssignments)
                            .map { (fieldIndex, rules) ->
                                Pair(fieldIndex, rules.subtract(agreedRules).toList())
                            }
                            .let { disambiguate(it) }
            }
        }

    return disambiguate(possibleRules)
        .map { (fieldIndex, rules) ->
            Pair(fieldIndex, rules.first())
        }
        .toMap()
}

val input = File(args[0]).readLines()
val rules = input
    .takeWhile { it.isNotBlank() }
    .map { Rule(it) }
val myTicket = input
    .dropWhile { it.trim() != "your ticket:" }
    .drop(1)
    .first()
    .let { Ticket(it) }
val nearbyTickets = input
    .dropWhile { it.trim() != "nearby tickets:" }
    .drop(1)
    .map { Ticket(it) }

// Part 1:
nearbyTickets
    .flatMap { ticket ->
        ticket.fields.filterNot { value ->
            rules.any { it.admits(value) }
        }
    }
    .sum()
    .let { println(it) }

// Part 2:
val validTickets = nearbyTickets
    .filter { ticket ->
        ticket.fields.all { value ->
            rules.any { it.admits(value) }
        }
    }
(0..validTickets.first().fields.lastIndex)
    .map { Pair(it, possibleRulesForField(it, validTickets, rules)) }
    .let { disambiguateRuleAssignments(it) }
    .filter { (_, rule) -> rule.name.startsWith("departure") }
    .map { (fieldIndex, _) -> myTicket.fields[fieldIndex] }
    .fold(1L, Long::times)
    .let { println(it) }