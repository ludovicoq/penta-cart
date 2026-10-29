package com.pentacart.app.ui.shopping

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pentacart.app.data.model.CatalogItem
import com.pentacart.app.data.model.QuantityUnit
import com.pentacart.app.data.model.ShoppingItem
import com.pentacart.app.data.repository.AuthRepository
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

/** Un prodotto del catalogo proposto mentre si scrive. [alreadyAdded] = è già in lista (o tra gli ingredienti). */
data class Suggestion(val item: CatalogItem, val alreadyAdded: Boolean)

data class ShoppingUiState(
    val toBuy: List<ShoppingItem> = emptyList(),
    val inCart: List<ShoppingItem> = emptyList(),
    val suggestions: List<Suggestion> = emptyList(),
    val isLoading: Boolean = true,
)

/** Messaggi una tantum da mostrare nello snackbar. */
sealed interface ShoppingEvent {
    data class Removed(val item: ShoppingItem) : ShoppingEvent
    data class AlreadyInList(val name: String) : ShoppingEvent
    data class BackInList(val name: String) : ShoppingEvent
}

@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val shopping: ShoppingRepository,
    private val auth: AuthRepository,
) : ViewModel() {
    /** Testo del campo "Cosa serve?". Stato Compose, così il cursore non salta mentre si scrive. */
    var query by mutableStateOf("")
        private set

    val uiState: StateFlow<ShoppingUiState> = combine(
        shopping.observeList(),
        shopping.observeCatalog(),
        snapshotFlow { query },
    ) { items, catalog, text ->
        val (inCart, toBuy) = items.partition { it.isChecked }
        ShoppingUiState(
            toBuy = toBuy,
            inCart = inCart,
            suggestions = suggestionsFor(text, catalog, toBuy.mapTo(HashSet()) { TextNormalizer.key(it.name) }),
            isLoading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ShoppingUiState())

    private val _events = Channel<ShoppingEvent>(Channel.BUFFERED)
    val events: Flow<ShoppingEvent> = _events.receiveAsFlow()

    fun onQueryChange(value: String) {
        query = value
    }

    fun addTyped() = add(query)

    fun addSuggestion(suggestion: Suggestion) = add(suggestion.item.name)

    /** Toglie un prodotto (es. scritto male) dai suggerimenti. Se è in lista, lì resta. */
    fun removeSuggestion(item: CatalogItem) = shopping.removeFromCatalog(item.id)

    fun toggle(item: ShoppingItem) = shopping.setChecked(item.id, !item.isChecked)

    fun setQuantity(item: ShoppingItem, quantity: Double?, unit: QuantityUnit?) =
        shopping.setQuantity(item.id, quantity, unit)

    fun remove(item: ShoppingItem) {
        shopping.removeItem(item.id)
        _events.trySend(ShoppingEvent.Removed(item))
    }

    fun undoRemove(item: ShoppingItem) = shopping.restoreItem(item)

    fun clearCart() = shopping.clearChecked(uiState.value.inCart.map { it.id })

    private fun add(rawName: String) {
        val user = auth.currentUser ?: return
        val state = uiState.value
        val plan = shopping.addWithoutDuplicates(listOf(rawName), user, state.toBuy + state.inCart)
        plan.toUncheck.firstOrNull()?.let { _events.trySend(ShoppingEvent.BackInList(it.name)) }
        plan.alreadyToBuy.firstOrNull()?.let { _events.trySend(ShoppingEvent.AlreadyInList(it.name)) }
        query = ""
    }
}

private const val MAX_SUGGESTIONS = 5

/**
 * Prodotti del catalogo che contengono il testo scritto: prima quelli che iniziano così,
 * poi gli altri, ognuno dal più usato (il catalogo arriva già ordinato per usageCount).
 * [alreadyAdded] sono i nomi normalizzati da segnare come "già presenti".
 */
internal fun suggestionsFor(
    query: String,
    catalog: List<CatalogItem>,
    alreadyAdded: Set<String>,
): List<Suggestion> {
    val q = TextNormalizer.key(query)
    if (q.isEmpty()) return emptyList()
    return catalog
        .filter { it.id.contains(q) }
        .sortedByDescending { it.id.startsWith(q) }
        .take(MAX_SUGGESTIONS)
        .map { Suggestion(it, alreadyAdded = it.id in alreadyAdded) }
}
