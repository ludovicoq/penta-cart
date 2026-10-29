package com.pentacart.app.ui.meals

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pentacart.app.data.model.Meal
import com.pentacart.app.data.model.ShoppingItem
import com.pentacart.app.data.repository.AuthRepository
import com.pentacart.app.data.repository.MealRepository
import com.pentacart.app.data.repository.ShoppingRepository
import com.pentacart.app.util.TextNormalizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Un ingrediente della ricetta e se è già tra le cose da comprare. */
data class IngredientStatus(val name: String, val alreadyToBuy: Boolean)

data class RecipeDetailUiState(
    val meal: Meal? = null,
    val ingredients: List<IngredientStatus> = emptyList(),
    val shoppingItems: List<ShoppingItem> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    meals: MealRepository,
    private val shopping: ShoppingRepository,
    private val auth: AuthRepository,
) : ViewModel() {
    private val mealId: String = checkNotNull(savedStateHandle[MEAL_ID_ARG])

    val uiState: StateFlow<RecipeDetailUiState> = combine(
        meals.observeMeal(mealId),
        shopping.observeList(),
    ) { meal, items ->
        val toBuy = items.filterNot { it.isChecked }.mapTo(HashSet()) { TextNormalizer.key(it.name) }
        RecipeDetailUiState(
            meal = meal,
            ingredients = meal?.ingredients.orEmpty().map { IngredientStatus(it, TextNormalizer.key(it) in toBuy) },
            shoppingItems = items,
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipeDetailUiState())

    /** Quanti ingredienti sono finiti davvero nella lista (nuovi + rimessi dal carrello). */
    private val _addedCount = Channel<Int>(Channel.BUFFERED)
    val addedCount: Flow<Int> = _addedCount.receiveAsFlow()

    fun addToShopping(names: List<String>) {
        val user = auth.currentUser ?: return
        val plan = shopping.addWithoutDuplicates(names, user, uiState.value.shoppingItems)
        _addedCount.trySend(plan.toAdd.size + plan.toUncheck.size)
    }
}
