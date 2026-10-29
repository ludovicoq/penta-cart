package com.pentacart.app.data.firebase

/** Nomi di collezioni e campi in un unico posto, così app e regole restano allineate. */
object Collections {
    const val USERS = "users"
    const val CATALOG = "catalog_items"
    const val SHOPPING = "shopping_list"
    const val MEALS = "meals"
    const val PLAN = "weekly_plan"
}

object Fields {
    // users
    const val UID = "uid"
    const val DISPLAY_NAME = "displayName"

    // catalog_items / shopping_list
    const val NAME = "name"
    const val CATEGORY = "category"
    const val USAGE_COUNT = "usageCount"
    const val IS_CHECKED = "isChecked"
    const val ADDED_BY_UID = "addedByUid"
    const val ADDED_BY_NAME = "addedByName"
    const val CREATED_AT = "createdAt"
    const val CHECKED_AT = "checkedAt"
    const val QUANTITY = "quantity"
    const val UNIT = "unit"

    // meals
    const val TITLE = "title"
    const val INGREDIENTS = "ingredients"
    const val INSTRUCTIONS = "instructions"

    // weekly_plan
    const val DATE = "date"
    const val MEAL_TYPE = "mealType"
    const val MEAL_ID = "mealId"
    const val MEAL_TITLE = "mealTitle"
    const val CUSTOM_NOTE = "customNote"
}
