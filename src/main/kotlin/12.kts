@file:Include("shared.kt")

import java.io.File

typealias Position = Pair<Int, Int>

sealed class Direction(val vector: Pair<Int, Int>) {
    data class Arbitrary(val v: Pair<Int, Int>) : Direction(v) {
        constructor(x: Int, y: Int) : this(Pair(x, y))
    }

    object North : Direction(Pair(0, 1))
    object East : Direction(Pair(1, 0))
    object South : Direction(Pair(0, -1))
    object West : Direction(Pair(-1, 0))

    operator fun plus(other: Direction): Direction =
        Arbitrary(this.vector + other.vector)

    operator fun times(factor: Int): Direction =
        Arbitrary(this.vector * factor)

    fun adjustBy(turn: TurnAngle): Direction =
        when (turn) {
            TurnAngle.QuarterRight ->
                Arbitrary(vector.second, -vector.first)
            TurnAngle.QuarterLeft ->
                Arbitrary(-vector.second, vector.first)
            TurnAngle.Half ->
                Arbitrary(-vector.first, -vector.second)
        }

    companion object {
        operator fun invoke(x: Int, y: Int): Direction =
            Arbitrary(Pair(x, y))

        operator fun invoke(shorthand: String): Direction =
            when (shorthand) {
                "N" -> North
                "E" -> East
                "S" -> South
                "W" -> West
                else -> throw IllegalArgumentException("Invalid direction shorthand $shorthand")
            }
    }
}

enum class TurnAngle(val angle: Int) {
    QuarterRight(90),
    Half(180),
    QuarterLeft(270);

    companion object {
        operator fun invoke(angle: Int): TurnAngle =
            values().firstOrNull { it.angle == (angle % 360) }
                ?: throw IllegalArgumentException("Invalid angle $angle")

        fun right(angle: Int): TurnAngle = invoke(angle)
        fun left(angle: Int): TurnAngle = invoke(360 - angle)
    }
}

data class ShipState(
    val position: Position = Position(0, 0),
    val orientation: Direction
) {
    /**
     * Adjust position resp. orientation according to part 1 rules.
     * Ignores waypoint.
     */
    fun adjustBy(step: Instruction): ShipState =
        when (step) {
            is Instruction.Shift ->
                ShipState(position + step.direction.vector * step.distance, orientation)
            is Instruction.Turn ->
                ShipState(position, orientation.adjustBy(step.angle))
            is Instruction.Move ->
                ShipState(position + orientation.vector * step.distance, orientation)
        }

    /**
     * Adjust waypoint resp. position according to part 1 rules.
     * Ignores orientation.
     */
    fun adjustByWaypoint(step: Instruction): ShipState =
        when (step) {
            is Instruction.Shift ->
                copy(orientation = orientation + (step.direction * step.distance))
            is Instruction.Turn ->
                copy(orientation = orientation.adjustBy(step.angle))
            is Instruction.Move ->
                this.copy(position = position + (orientation.vector * step.distance))
        }
}

sealed class Instruction {
    data class Shift(val direction: Direction, val distance: Int) : Instruction()
    data class Turn(val angle: TurnAngle) : Instruction()
    data class Move(val distance: Int) : Instruction()

    companion object {
        operator fun invoke(raw: String): Instruction =
            Instruction(raw.take(1), raw.drop(1).toInt())

        operator fun invoke(type: String, argument: Int): Instruction =
            when (type) {
                "N" -> Shift(Direction.North, argument)
                "E" -> Shift(Direction.East, argument)
                "S" -> Shift(Direction.South, argument)
                "W" -> Shift(Direction.West, argument)
                "L" -> Turn(TurnAngle.left(argument))
                "R" -> Turn(TurnAngle.right(argument))
                "F" -> Move(argument)
                else -> throw IllegalArgumentException("Invalid instruction $type$argument")
            }
    }
}

val instructions = File(args[0])
    .readLines()
    .map { Instruction(it) }

// Part 1:
println(
    instructions.fold(ShipState(orientation = Direction.East)) { state, step ->
        state.adjustBy(step)
    }.position.manhattanFrom()
)
// Part 2:
println(
    instructions.fold(ShipState(orientation = Direction(10, 1))) { state, step ->
        state.adjustByWaypoint(step)
    }.position.manhattanFrom()
)
