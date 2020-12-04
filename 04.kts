import _04.Fields.*

enum class Fields(val key: String) {
    BirthYear("byr") {
        override fun admits(value: String): Boolean =
                value.matches(Regex("^\\d{4}$"))
                        && value.toInt() in 1920..2002
    },
    IssueYear("iyr"){
        override fun admits(value: String): Boolean =
                value.matches(Regex("^\\d{4}$"))
                    && value.toInt() in 2010..2020
    },
    ExpirationYear("eyr"){
        override fun admits(value: String): Boolean =
                value.matches(Regex("^\\d{4}$"))
                    && value.toInt() in 2020..2030
    },
    Height("hgt"){
        override fun admits(value: String): Boolean {
            val measure = value.dropLast(2).toIntOrNull() ?: 0
            val unit = value.takeLast(2)
            return when (unit) {
                "cm" -> measure in 150..193
                "in" -> measure in 59..76
                else -> false
            }
        }
    },
    HairColor("hcl"){
        override fun admits(value: String): Boolean =
            value.matches(Regex("^#[0-9a-f]{6}$"))
    },
    EyeColor("ecl"){
        override fun admits(value: String): Boolean =
                value in listOf("amb", "blu", "brn", "gry", "grn", "hzl", "oth")
    },
    PassportID("pid"){
        override fun admits(value: String): Boolean =
                value.matches(Regex("^\\d{9}$"))
    },
    CountryID("cid"){
        override fun admits(value: String): Boolean = true
    };

    abstract fun admits(value: String): Boolean;

    companion object {
        fun valueOfKey(key: String): Fields {
            return values().filter { it.key == key }.firstOrNull()
                    ?: throw IllegalArgumentException("No Field with key ${key}")
        }
    }
}

data class Passport(val values: Map<Fields, String>) {
    var hasNecessaryFields: Boolean =
            Fields.values().filter { it != CountryID }
                    .all { values.containsKey(it) }

    var isValid: Boolean =
            hasNecessaryFields
                    && values.all { it.key.admits(it.value) }
}

fun <T> Array<T>.split(splitEntry: T): List<List<T>> =
        this.fold(mutableListOf(mutableListOf<T>())) { acc, line ->
            if (line == splitEntry) {
                acc.add(mutableListOf())
            } else {
                acc.last().add(line)
            }
            return@fold acc
        }

val passports = args.split("")
        .map { it.flatMap { it.split("\\s+".toRegex()) } }
        .map {
            it.map {
                it.split(":").let {
                    Pair(Fields.valueOfKey(it[0]), it[1])
                }
            }.toMap()
        }
        .map { Passport(it) }


// Part 1:
println(passports.count { it.hasNecessaryFields })
// Part 2:
println(passports.count { it.isValid })

// --> kscript 04.kts $(cat 04_input)