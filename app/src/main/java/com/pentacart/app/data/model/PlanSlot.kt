package com.pentacart.app.data.model

import java.time.LocalDate

enum class MealType(val label: String) {
    LUNCH("Pranzo"),
    DINNER("Cena"),
}

/**
 * Un pasto pianificato in una data reale. Contiene un piatto del ricettario ([mealId])
 * oppure una nota libera ([customNote], es. "Pizza fuori").
 */
data class PlanSlot(
    val date: LocalDate,
    val mealType: MealType,
    val mealId: String? = null,
    /** Copia del titolo al momento della scelta: serve se il piatto viene poi eliminato. */
    val mealTitle: String? = null,
    val customNote: String? = null,
) {
    val id: String get() = slotId(date, mealType)

    companion object {
        /** Es. "2026-11-02_LUNCH". */
        fun slotId(date: LocalDate, mealType: MealType): String = "${date}_${mealType.name}"
    }
}
