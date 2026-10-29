package com.pentacart.app.data.firebase

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.DocumentSnapshot.ServerTimestampBehavior
import com.pentacart.app.data.model.CatalogItem
import com.pentacart.app.data.model.Meal
import com.pentacart.app.data.model.MealCategory
import com.pentacart.app.data.model.MealType
import com.pentacart.app.data.model.PlanSlot
import com.pentacart.app.data.model.QuantityUnit
import com.pentacart.app.data.model.ShoppingItem
import com.pentacart.app.data.model.User
import java.time.LocalDate

// Mappatura esplicita invece di toObject(): niente sorprese con "isChecked" -> "checked",
// nessun costruttore vuoto obbligatorio e nessuna regola ProGuard da mantenere.

fun DocumentSnapshot.toUser(): User? {
    if (!exists()) return null
    return User(uid = id, displayName = getString(Fields.DISPLAY_NAME).orEmpty())
}

fun DocumentSnapshot.toCatalogItem() = CatalogItem(
    id = id,
    name = getString(Fields.NAME).orEmpty(),
    category = getString(Fields.CATEGORY).orEmpty(),
    usageCount = getLong(Fields.USAGE_COUNT) ?: 0,
)

fun DocumentSnapshot.toShoppingItem() = ShoppingItem(
    id = id,
    name = getString(Fields.NAME).orEmpty(),
    isChecked = getBoolean(Fields.IS_CHECKED) ?: false,
    addedByUid = getString(Fields.ADDED_BY_UID).orEmpty(),
    addedByName = getString(Fields.ADDED_BY_NAME).orEmpty(),
    createdAt = getTimestamp(Fields.CREATED_AT, ServerTimestampBehavior.ESTIMATE)?.toDate()?.time ?: 0,
    checkedAt = getTimestamp(Fields.CHECKED_AT, ServerTimestampBehavior.ESTIMATE)?.toDate()?.time,
    quantity = getDouble(Fields.QUANTITY),
    unit = getString(Fields.UNIT)?.let { name -> QuantityUnit.entries.firstOrNull { it.name == name } },
)

fun DocumentSnapshot.toMeal() = Meal(
    id = id,
    title = getString(Fields.TITLE).orEmpty(),
    category = getString(Fields.CATEGORY)?.let { name -> MealCategory.entries.firstOrNull { it.name == name } },
    ingredients = (get(Fields.INGREDIENTS) as? List<*>)?.filterIsInstance<String>().orEmpty(),
    instructions = getString(Fields.INSTRUCTIONS).orEmpty(),
)

fun Meal.toFirestore(): Map<String, Any?> = mapOf(
    Fields.TITLE to title,
    Fields.CATEGORY to category?.name,
    Fields.INGREDIENTS to ingredients,
    Fields.INSTRUCTIONS to instructions,
)

fun DocumentSnapshot.toPlanSlot(): PlanSlot? {
    val date = getString(Fields.DATE)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return null
    val type = getString(Fields.MEAL_TYPE)?.let { name -> MealType.entries.firstOrNull { it.name == name } } ?: return null
    return PlanSlot(
        date = date,
        mealType = type,
        mealId = getString(Fields.MEAL_ID),
        mealTitle = getString(Fields.MEAL_TITLE),
        customNote = getString(Fields.CUSTOM_NOTE),
    )
}

fun PlanSlot.toFirestore(): Map<String, Any?> = mapOf(
    Fields.DATE to date.toString(),
    Fields.MEAL_TYPE to mealType.name,
    Fields.MEAL_ID to mealId,
    Fields.MEAL_TITLE to mealTitle,
    Fields.CUSTOM_NOTE to customNote,
)
