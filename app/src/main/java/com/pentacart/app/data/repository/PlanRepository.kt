package com.pentacart.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pentacart.app.data.firebase.Collections
import com.pentacart.app.data.firebase.Fields
import com.pentacart.app.data.firebase.logFailure
import com.pentacart.app.data.firebase.snapshotFlow
import com.pentacart.app.data.firebase.toFirestore
import com.pentacart.app.data.firebase.toPlanSlot
import com.pentacart.app.data.model.Meal
import com.pentacart.app.data.model.MealType
import com.pentacart.app.data.model.PlanSlot
import com.pentacart.app.util.WeekDates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pianificazione dei pasti su date reali. Un documento per slot, con ID "yyyy-MM-dd_LUNCH",
 * quindi scegliere un pasto significa sovrascrivere quel documento.
 */
@Singleton
class PlanRepository @Inject constructor(
    private val db: FirebaseFirestore,
) {
    private val plan get() = db.collection(Collections.PLAN)

    /**
     * Gli slot pianificati della settimana che contiene [anyDayOfWeek]. Gli slot vuoti non esistono
     * nel database: la UI mostra comunque tutti i 14 riquadri.
     * Le date sono stringhe ISO, che si ordinano come le date: basta un filtro per intervallo.
     */
    fun observeWeek(anyDayOfWeek: LocalDate): Flow<List<PlanSlot>> {
        val monday = WeekDates.mondayOf(anyDayOfWeek)
        return plan
            .whereGreaterThanOrEqualTo(Fields.DATE, monday.toString())
            .whereLessThanOrEqualTo(Fields.DATE, monday.plusDays(6).toString())
            .snapshotFlow()
            .map { snapshot -> snapshot.documents.mapNotNull { it.toPlanSlot() } }
    }

    fun setMeal(date: LocalDate, mealType: MealType, meal: Meal) =
        save(PlanSlot(date, mealType, mealId = meal.id, mealTitle = meal.title))

    fun setNote(date: LocalDate, mealType: MealType, note: String) =
        save(PlanSlot(date, mealType, customNote = note.trim()))

    fun clearSlot(date: LocalDate, mealType: MealType) {
        plan.document(PlanSlot.slotId(date, mealType)).delete().logFailure("clearSlot")
    }

    private fun save(slot: PlanSlot) {
        plan.document(slot.id).set(slot.toFirestore()).logFailure("savePlanSlot")
    }
}
