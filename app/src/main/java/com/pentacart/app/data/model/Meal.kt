package com.pentacart.app.data.model

enum class MealCategory(val label: String) {
    PRANZO("Pranzo"),
    CENA("Cena"),
    PRIMO("Primo"),
    SECONDO("Secondo"),
}

/** Un piatto del ricettario. [id] vuoto = piatto nuovo non ancora salvato. */
data class Meal(
    val id: String = "",
    val title: String = "",
    val category: MealCategory? = null,
    val ingredients: List<String> = emptyList(),
    val instructions: String = "",
)
