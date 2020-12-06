import java.io.File

fun seatIdOf(ticketCode: String): Int =
        ticketCode
                .replace('F', '0')
                .replace('B', '1')
                .replace('L', '0')
                .replace('R', '1')
                .toInt(radix = 2)

// Test:
//@file:KotlinOpts("-J-ea")
//assert(seatIdOf("BFFFBBFRRR") == 567)
//assert(seatIdOf("FFFBBBFRRR") == 119)
//assert(seatIdOf("BBFFBBFRLL") == 820)

fun mySeatId(otherTicketCodes: List<String>): Int {
    val otherTicketIds = otherTicketCodes.map(::seatIdOf)
    return (0..1024)
            .filter { !otherTicketIds.contains(it) }
            .zipWithNext()
            .first { it.first < it.second - 1 }
            .second
}

val input = File(args[0]).readLines()
// Part 1:
println(input.map(::seatIdOf).max())
// Part 2:
println(mySeatId(input))