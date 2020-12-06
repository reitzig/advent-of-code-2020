//@file:KotlinOpts("-J-ea")

import java.io.File
import kotlin.IllegalArgumentException

fun rowOf(ticketCode: String): Int {
    assert(ticketCode.length == 10)

    tailrec fun rowOf(searchCode: String, minRow: Int, maxRow: Int): Int =
            if (minRow == maxRow) {
                minRow
            } else {
                when (searchCode.first()) {
                    'F' -> rowOf(searchCode.drop(1), minRow, minRow + (maxRow - minRow) / 2)
                    'B' -> rowOf(searchCode.drop(1), minRow + (maxRow - minRow + 1) / 2, maxRow)
                    else -> throw IllegalArgumentException()
                }
            }

    return rowOf(ticketCode.substring(0..6), 0, 127)
}

fun colOf(ticketCode: String): Int {
    assert(ticketCode.length == 10)

    return when (ticketCode.substring(7..9)) {
        "LLL" -> 0
        "LLR" -> 1
        "LRL" -> 2
        "LRR" -> 3
        "RLL" -> 4
        "RLR" -> 5
        "RRL" -> 6
        "RRR" -> 7
        else -> throw IllegalArgumentException()
    }
}

fun seatIdOf(row: Int, col: Int): Int =
        row * 8 + col

fun seatIdOf(ticketCode: String): Int =
        seatIdOf(rowOf(ticketCode), colOf(ticketCode))

fun test(impl: (String) -> Int) {
    assert(impl.invoke("BFFFBBFRRR") == 567)
    assert(impl.invoke("FFFBBBFRRR") == 119)
    assert(impl.invoke("BBFFBBFRLL") == 820)
}

//test(::seatIdOf)

// Okay, that was a cute exercise. ^_^ But now for real:

fun fastSeatIdOf(ticketCode: String): Int =
        ticketCode
                .replace('F', '0')
                .replace('B', '1')
                .replace('L', '0')
                .replace('R', '1')
                .toInt(radix = 2)
// Well, it's not actually _faster_, but a lot more concise at least!

//test(::fastSeatIdOf)

fun fasterSeatIdOf(ticketCode: String): Int {
    var seatId = 0
    for (i in 0..6) {
        if ( ticketCode[i] == 'B' ) {
            seatId += 1
        }
        // 'F' -> += 0
        // else -> doesn't happen, not here to be defensive

        seatId = seatId.shl(1)
    }
    for (i in 7..9) {
        if (ticketCode[i] == 'R' ) {
            seatId += 1
        }
        seatId = seatId.shl(1)
    }
    seatId = seatId.shr(1)
    return seatId
}

//test(::fasterSeatIdOf)

fun mySeatId(otherTicketCodes: List<String>): Int {
    val otherTicketIds = otherTicketCodes.map(::fasterSeatIdOf)
    return (0..1024)
            .filter { !otherTicketIds.contains(it) }
            .zipWithNext()
            .first { it.first < it.second - 1 }
            .second
}

// That's a _little_ slow. Let's try this:

fun fastMySeatId(otherTicketCodes: List<String>): Int {
    val otherTicketIds = HashSet<Int>()
    otherTicketCodes.mapTo(otherTicketIds, ::fastSeatIdOf)
    return (0..1023)
            .first { !otherTicketIds.contains(it) && otherTicketIds.contains(it - 1) }
}

// Huh. That isn't faster, either? Well then:

fun fasterMySeatId(otherTicketCodes: List<String>): Int {
    val otherTicketIds = HashSet<Int>()
    otherTicketCodes.mapTo(otherTicketIds, ::fasterSeatIdOf)
    for (i in 0..1023) {
        if (!otherTicketIds.contains(i) && otherTicketIds.contains(i - 1)) {
            return i
        }
    }

    throw IllegalArgumentException()
}

// Not faster, either. Apparently, the mapping is what takes the time?
// That's what I get for optimizing code I _think_ is slow instead of profiling.
// --> that's when I wrote fasterSeatIdOf
// Which didn't help, either. In fact, it seems thatt all combinations of
//
// seatIdOf             mySeatId
// fastSeatIdOf     x   fastMySeatId
// fasterSeatIdOf       fasterMySeatId
//
// are roughly equally fast. Not sure whether that's good or bad news
// -- apparently, I'm being an idiot either way, since I don't know what's going on.
// Whoops!

//println(args.map(::fasterSeatIdOf).max())
//println(fastMySeatId(args))

// Huh. Turns out, calling the script with $(cat input_file) is what sucks.
// Rewriting with file IO:

val input = File("05_input").readLines()
// Part 1:
println(input.map(::fasterSeatIdOf).max())
// Part 2:
println(mySeatId(input))