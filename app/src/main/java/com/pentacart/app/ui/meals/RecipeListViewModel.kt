package com.pentacart.app.ui.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pentacart.app.data.model.Meal
import com.pentacart.app.data.model.MealCategory
import com.pentacart.app.data.repository.MealRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class RecipeListUiState(
    val meals: List<Meal> = emptyList(),
    /** null = tutte le categorie. */
    val category: MealCategory? = null,
    val hasAnyMeal: Boolean = false,
    val isLoading: Boolean = true,
)

@HiltViewModel
class RecipeListViewModel @Inject constructor(
    meals: MealRepository,
) : ViewModel() {
    private val category = MutableStateFlow<MealCategory?>(null)

    val uiState: StateFlow<RecipeListUiState> = combine(meals.observeMeals(), category) { all, selected ->
        RecipeListUiState(
            meals = if (selected == null) all else all.filter { it.category == selected },
            category = selected,
            hasAnyMeal = all.isNotEmpty(),
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipeListUiState())

    fun selectCategory(value: MealCategory?) {
        category.value = value
    }
}
