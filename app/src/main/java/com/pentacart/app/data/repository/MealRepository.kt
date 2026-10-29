package com.pentacart.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pentacart.app.data.firebase.Collections
import com.pentacart.app.data.firebase.logFailure
import com.pentacart.app.data.firebase.snapshotFlow
import com.pentacart.app.data.firebase.toFirestore
import com.pentacart.app.data.firebase.toMeal
import com.pentacart.app.data.model.Meal
import com.pentacart.app.util.TextNormalizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Ricettario dei "soliti pasti". */
@Singleton
class MealRepository @Inject constructor(
    private val db: FirebaseFirestore,
) {
    private val meals get() = db.collection(Collections.MEALS)

    /** In ordine alfabetico, ignorando maiuscole e minuscole. */
    fun observeMeals(): Flow<List<Meal>> =
        meals.snapshotFlow().map { snapshot ->
            snapshot.documents.map { it.toMeal() }.sortedBy { TextNormalizer.key(it.title) }
        }

    /** null se il piatto non esiste (o è stato eliminato). */
    fun observeMeal(mealId: String): Flow<Meal?> =
        meals.document(mealId).snapshotFlow().map { if (it.exists()) it.toMeal() else null }

    /** Crea o aggiorna il piatto e restituisce il suo ID (generato in locale, quindi subito disponibile). */
    fun saveMeal(meal: Meal): String {
        val ref = if (meal.id.isBlank()) meals.document() else meals.document(meal.id)
        val cleaned = meal.copy(
            title = TextNormalizer.display(meal.title),
            ingredients = meal.ingredients.map(TextNormalizer::display).filter { it.isNotEmpty() },
            instructions = meal.instructions.trim(),
        )
        ref.set(cleaned.toFirestore()).logFailure("saveMeal")
        return ref.id
    }

    fun deleteMeal(mealId: String) {
        meals.document(mealId).delete().logFailure("deleteMeal")
    }
}
