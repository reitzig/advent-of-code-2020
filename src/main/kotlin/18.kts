@file:DependsOn("com.github.h0tk3y.betterParse:better-parse:0.4.0-1.4-M2")

import com.github.h0tk3y.betterParse.combinators.leftAssociative
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken

sealed class Expression {
    data class Times(val leftFactor: Expression, val rightFactor: Expression) : Expression() {
        override fun evaluate(): Long = leftFactor.evaluate() * rightFactor.evaluate()
    }

    data class Plus(val leftFactor: Expression, val rightFactor: Expression) : Expression() {
        override fun evaluate(): Long = leftFactor.evaluate() + rightFactor.evaluate()
    }

    data class Literal(val number: Long) : Expression() {
        override fun evaluate(): Long = number
    }

    abstract fun evaluate(): Long

    companion object {
        operator fun invoke(expression: String): Expression =
            expressionGrammar.parseToEnd(expression)
    }
}

// Adapted, but mostly taken from https://github.com/h0tk3y/better-parse
val expressionGrammar = object : Grammar<Expression>() {
    val ws by regexToken("\\s+", ignore = true)
    val number by regexToken("\\d+")
    val plus by literalToken("+")
    val times by literalToken("*")
    val lpar by literalToken("(")
    val rpar by literalToken(")")

    val term by
    (number use { Expression.Literal(text.toInt()) }) or
            (-lpar * parser(this::rootParser) * -rpar)

    val timesChain by leftAssociative(term, times) { l, _, r -> Expression.Times(l, r) }
    override val rootParser by leftAssociative(timesChain, plus) { l, _, r -> Expression.Plus(l, r) }
}