package com.pentacart.app.ui.meals

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pentacart.app.data.model.Meal
import com.pentacart.app.data.model.MealCategory
import com.pentacart.app.data.repository.MealRepository
import com.pentacart.app.data.repository.ShoppingRepository
import com.pentacart.app.ui.shopping.Suggestion
import com.pentacart.app.ui.shopping.suggestionsFor
import com.pentacart.app.util.TextNormalizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Creazione (senza mealId) o modifica di un piatto. Il modulo vive qui come stato Compose. */
@HiltViewModel
class RecipeEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val meals: MealRepository,
    shopping: ShoppingRepository,
) : ViewModel() {
    private val mealId: String? = savedStateHandle.get<String>(MEAL_ID_ARG)?.takeIf { it.isNotBlank() }
    val isNew: Boolean = mealId == null

    var isLoading by mutableStateOf(!isNew)
        private set
    var title by mutableStateOf("")
    var category by mutableStateOf<MealCategory?>(null)
    var instructions by mutableStateOf("")
    val ingredients = mutableStateListOf<String>()
    var ingredientQuery by mutableStateOf("")

    val canSave: Boolean get() = title.isNotBlank()

    /** Suggerimenti dal catalogo della spesa, così gli ingredienti si scrivono come i prodotti. */
    val ingredientSuggestions: StateFlow<List<Suggestion>> = combine(
        shopping.observeCatalog(),
        snapshotFlow { ingredientQuery },
        snapshotFlow { ingredients.toList() },
    ) { catalog, query, current ->
        suggestionsFor(query, catalog, current.mapTo(HashSet(), TextNormalizer::key))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        if (mealId != null) {
            viewModelScope.launch {
                meals.observeMeal(mealId).firstOrNull()?.let { meal ->
                    title = meal.title
                    category = meal.category
                    instructions = meal.instructions
                    ingredients.addAll(meal.ingredients)
                }
                isLoading = false
            }
        }
    }

    fun addIngredient(raw: String = ingredientQuery) {
        val name = TextNormalizer.display(raw)
        if (name.isNotEmpty() && ingredients.none { TextNormalizer.key(it) == TextNormalizer.key(name) }) {
            ingredients += name
        }
        ingredientQuery = ""
    }

    fun removeIngredient(index: Int) {
        ingredients.removeAt(index)
    }

    /** Salva e restituisce true; false se manca il nome. Un ingrediente scritto ma non aggiunto viene aggiunto. */
    fun save(): Boolean {
        if (!canSave) return false
        if (ingredientQuery.isNotBlank()) addIngredient()
        meals.saveMeal(
            Meal(
                id = mealId.orEmpty(),
                title = title,
                category = category,
                ingredients = ingredients.toList(),
                instructions = instructions,
            ),
        )
        return true
    }

    fun delete() {
        mealId?.let(meals::deleteMeal)
    }
}
