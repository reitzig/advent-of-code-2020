@file:Include("shared.kt")

import java.io.File

// NB: Would like this to be an inline class, but alas, not possible in kscript (yet)
data class Card(val value: Int)

data class Hand(val cards: List<Card>) {
    fun isEmpty(): Boolean = cards.isEmpty()

    val score: Int
        get() = cards.reversed().mapIndexed { i, card -> (i + 1) * card.value }.sum()
}

data class GameResult(val winningPlayer: Int, val score: Int)

enum class WinCondition {
    ByScore {
        override fun determineWinner(hand1: Hand, hand2: Hand): GameResult =
            if (hand1.isEmpty()) {
                GameResult(1, hand2.score)
            } else {
                GameResult(0, hand1.score)
            }
    },
    InfiniteLoop {
        override fun determineWinner(hand1: Hand, hand2: Hand): GameResult =
            GameResult(0, hand1.score)
    };

    abstract fun determineWinner(hand1: Hand, hand2: Hand): GameResult

    fun determineWinner(hand1: List<Card>, hand2: List<Card>): GameResult =
        determineWinner(Hand(hand1), Hand(hand2))
}

fun play(hand1: Hand, hand2: Hand, recursive: Boolean = true): GameResult {
    val seenConfigurations = mutableSetOf<String>()
    val players = listOf(
        hand1.cards.toMutableList(),
        hand2.cards.toMutableList()
    )

    while (players.none { it.isEmpty() }) {
        val configuration = players.joinToString("#") { it.joinToString(",") }
        if (seenConfigurations.contains(configuration)) {
            return WinCondition.InfiniteLoop.determineWinner(players[0], players[1])
        } else {
            seenConfigurations.add(configuration)
        }

        val cards = players.map { it.removeFirst() }
        val winner = players.zip(cards).let { played ->
            if (recursive && played.all { (hand, card) -> card.value <= hand.size }) {
                played.map { (hand, card) ->
                    Hand(hand.toList().take(card.value))
                }.let { play(it[0], it[1]).winningPlayer }
            } else {
                cards.indexOfMaxByOrNull { it.value } ?: throw IllegalStateException("No cards drawn?!")
            }
        }

        players[winner].addAll(if (winner == 0) cards else cards.reversed())
    }

    return WinCondition.ByScore.determineWinner(players[0], players[1])
}

// Input:
val (player1, player2) = File(args[0]).readLines()
    .split("")
    .map { playerInput ->
        playerInput.drop(1).map { Card(it.toInt()) }
    }
    .map { Hand(it) }

// Part 1:
play(player1, player2, recursive = false)
    .also { println(it) }

// Part 2:
play(player1, player2)
    .also { println(it) }
