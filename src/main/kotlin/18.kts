@file:KotlinOpts("-J-ea")
@file:Include("shared.kt")

import java.io.File

sealed class Expression {
    data class Times(val leftFactor: Expression, val rightFactor: Expression) : Expression() {
        override fun evaluate(): Long = leftFactor.evaluate() * rightFactor.evaluate()
        override fun toString(): String = "($leftFactor * $rightFactor)"
    }

    data class Plus(val leftSummand: Expression, val rightSummand: Expression) : Expression() {
        override fun evaluate(): Long = leftSummand.evaluate() + rightSummand.evaluate()
        override fun toString(): String = "($leftSummand + ${rightSummand})"
    }

    data class Literal(val number: Long) : Expression() {
        override fun evaluate(): Long = number
        override fun toString(): String = number.toString()
    }

    abstract fun evaluate(): Long

    companion object {
        operator fun invoke(expression: String, precedence: Parser.Precedence): Expression =
            Parser.parse(Parser.lex(expression), precedence)
    }
}

typealias SententialForm = List<Either<Parser.Token, Expression>>

object Parser {
    sealed class Token(val text: String) {
        data class Literal(val value: Long) : Token(value.toString()) {
            companion object {
                operator fun invoke(value: String): Literal? =
                    if (value.isNotBlank()) {
                        Literal(value.toLong())
                    } else {
                        null
                    }
            }

            override fun toString(): String = super.toString()
        }

        object ParenL : Token("(")
        object ParenR : Token(")")
        object Plus : Token("+")
        object Times : Token("*")

        override fun toString(): String = text
    }

    enum class Precedence(val chainReducer: (SententialForm) -> Expression) {
        LeftAssociative(::reduceChainWithoutParensWithoutPrecedence),
        TimesBindsStronger(::reduceChainWithoutParensTimesBindsStronger),
        PlusBindsStronger(::reduceChainWithoutParensPlusBindsStronger)
    }

    fun lex(expression: String): List<Token> =
        expression
            .fold(Pair(listOf<Token?>(), "")) { (tokens, partialNumber), char ->
                when (char) {
                    ' ' -> Pair(tokens + Token.Literal(partialNumber), "") // end token / ignore
                    '(' -> Pair(tokens + Token.Literal(partialNumber) + Token.ParenL, "")
                    ')' -> Pair(tokens + Token.Literal(partialNumber) + Token.ParenR, "") // end token / add token
                    '+' -> Pair(tokens + Token.Literal(partialNumber) + Token.Plus, "")
                    '*' -> Pair(tokens + Token.Literal(partialNumber) + Token.Times, "")
                    in CharRange('0', '9') -> Pair(tokens, partialNumber + char) // extend token
                    else -> throw IllegalArgumentException("Unexpected character $char")
                }
            }
            .run { first + Token.Literal(second) }
            .filterNotNull()

    // Workaround https://youtrack.jetbrains.com/issue/KT-43995 (otherwise, extension from shared.kt would work!)
    private fun <T> split(l: List<T>, by: T): List<List<T>> =
        l.fold(mutableListOf(mutableListOf<T>())) { acc, elem ->
            if (elem == by) {
                acc.add(mutableListOf())
            } else {
                acc.last().add(elem)
            }
            return@fold acc
        }

    private fun reduceChainWithoutParensWithoutPrecedence(flatExpression: List<Either<Token, Expression>>): Expression {
        // This becomes fugly with a single fold, so a loop it is:
        var expression = null as Expression?
        var pendingOperator = null as Token?

        fun combine(operand: Expression) {
            expression = expression?.let { left ->
                when (pendingOperator) {
                    is Token.Plus -> Expression.Plus(left, operand)
                    is Token.Times -> Expression.Times(left, operand)
                    else -> throw IllegalArgumentException("Unexpected pending operator: $pendingOperator")
                }
            } ?: operand
            pendingOperator = null
        }

        for (thing in flatExpression) {
//            println("exp: \t$expression\nop:\t$pendingOperator \nthing:\t$thing\n\n")
            if (thing.t is Token.Times || thing.t is Token.Plus) {
                pendingOperator = thing.t
            } else if (thing.t is Token.Literal) {
                combine(Expression.Literal(thing.t.value))
            } else if (thing.u != null) {
                combine(thing.u)
            } else {
                throw IllegalStateException("Can't handle: $thing")
            }
        }

        return expression!!
    }

    // Wrote this for part 1 before realizing it wasn't what was needed. So there.
    private fun reduceChainWithoutParensTimesBindsStronger(flatExpression: List<Either<Token, Expression>>): Expression =
        // Multiplication binds more strongly, so add later:
        split(flatExpression, Either(t = Token.Plus)).map { sum ->
            sum.asSequence()
                .apply { assert(none { it.t != null && it.t in listOf(Token.Plus, Token.ParenL, Token.ParenR) }) }
                .filter { it.t != Token.Times }
                .apply { assert(all { it.t == null || it.t is Token.Literal }) }
                .map { (token, expression) ->
                    if (token != null && token is Token.Literal) {
                        Expression.Literal(token.value)
                    } else {
                        expression ?: throw IllegalStateException("Unexpected token $token")
                    }
                }
                // Go left-associative:
                .reduce { left, right -> Expression.Times(left, right) }
        }
            // Go left-associative:
            .reduce { left, right -> Expression.Plus(left, right) }

    private fun reduceChainWithoutParensPlusBindsStronger(flatExpression: List<Either<Token, Expression>>): Expression =
        // Summation binds more strongly, so multiply later:
        split(flatExpression, Either(t = Token.Times)).map { sum ->
            sum.asSequence()
                .apply { assert(none { it.t != null && it.t in listOf(Token.Times, Token.ParenL, Token.ParenR) }) }
                .filter { it.t != Token.Plus }
                .apply { assert(all { it.t == null || it.t is Token.Literal }) }
                .map { (token, expression) ->
                    if (token != null && token is Token.Literal) {
                        Expression.Literal(token.value)
                    } else {
                        expression ?: throw IllegalStateException("Unexpected token $token")
                    }
                }
                // Go left-associative:
                .reduce { left, right -> Expression.Plus(left, right) }
        }
            // Go left-associative:
            .reduce { left, right -> Expression.Times(left, right) }

    // Recursive descent with look-ahead 1
    fun parse(tokens: List<Token>, precedence: Precedence): Expression {
        val expressionStack = mutableListOf(mutableListOf<Either<Token, Expression>>())
        // NB: Start with implicit pair of parentheses around the whole expression

        for (token in tokens) {
            when (token) {
                is Token.Literal, is Token.Plus, is Token.Times ->
                    expressionStack.last().add(Either(t = token))
                is Token.ParenL ->
                    expressionStack.add(mutableListOf())
                is Token.ParenR -> {
                    val parsed = precedence.chainReducer(expressionStack.last())
                    expressionStack.removeLast()
                    expressionStack.last().add(Either(u = parsed))
                }
            }
        }

        assert(expressionStack.size == 1) { "Stack not cleared -- bad expression!" }
        return precedence.chainReducer(expressionStack.last())
    }
}

val input = File(args[0]).readLines()

// Part 1:
input
    .map { Expression(it, Parser.Precedence.LeftAssociative) }
//    .onEach { println("$it\n\t= ${it.evaluate()}") }
    .map { it.evaluate() }
    .sum()
    .apply { println(this) }

// Part 2:
input
    .map { Expression(it, Parser.Precedence.PlusBindsStronger) }
//    .onEach { println("$it\n\t= ${it.evaluate()}") }
    .map { it.evaluate() }
    .sum()
    .apply { println(this) }
