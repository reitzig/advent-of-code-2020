import java.io.File

typealias Ingredient = String
typealias Allergen = String

data class Food(val ingredients: List<Ingredient>, val allergens: List<Allergen>) {
    companion object {
        val linePattern = Regex("^([^(]*)\\(contains ([^)]*)\\)$")

        operator fun invoke(foodLine: String): Food {
            with(linePattern.matchEntire(foodLine) ?: throw IllegalArgumentException("invalid line $foodLine")) {
                val ingredients = groupValues[1].trim().split(Regex("\\s+"))
                val allergens = groupValues[2].trim().split(",").map { it.trim() }
                return@invoke Food(ingredients, allergens)
            }
        }
    }
}

val foods = File(args[0]).readLines()
    .map { Food(it) }
val ingredients = foods.flatMap { it.ingredients }.distinct().sorted() as List<Ingredient>
val allergens = foods.flatMap { it.allergens }.distinct().sorted() as List<Allergen>

// Part 1:
val ingredientCandidates = allergens.map { allergen ->
    ingredients
        .filter { ingredient ->
            foods.none { it.allergens.contains(allergen) && !it.ingredients.contains(ingredient) }
        }
        .let { Pair(allergen, it) }
}.toMap()
ingredients
    .filterNot { ingredientCandidates.values.flatten().contains(it) }
    .map { ingredient -> foods.count { it.ingredients.contains(ingredient) } }
    .sum()
    .also { println(it) }

// Part 2:
val ingredientAllergens = ingredientCandidates.toMutableMap()
do {
    val assigned = ingredientAllergens
        .filterValues { it.size == 1 }
        .flatMap { (_, i) -> i }
    val notAssigned = ingredientAllergens.filterValues { it.size > 1 }
    for ((allergen, ingredients) in notAssigned) {
        ingredientAllergens[allergen] = ingredients.subtract(assigned).toList()
    }
} while (notAssigned.isNotEmpty())
ingredientAllergens
    .toList()
    .sortedBy { (all, _) -> all }
    .flatMap { (_, ing) -> ing }
    .joinToString(",")
    .also { println(it) }
