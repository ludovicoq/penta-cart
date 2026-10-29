package com.pentacart.app.ui.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pentacart.app.data.model.CatalogItem
import com.pentacart.app.data.model.ShoppingItem
import com.pentacart.app.ui.components.EmptyState
import com.pentacart.app.ui.components.KeepScreenOn
import com.pentacart.app.ui.components.LoadingScreen
import com.pentacart.app.ui.components.SectionHeader
import kotlinx.coroutines.launch

@Composable
fun ShoppingScreen(viewModel: ShoppingViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var confirmClear by remember { mutableStateOf(false) }
    var editingQuantity by remember { mutableStateOf<ShoppingItem?>(null) }
    var removingSuggestion by remember { mutableStateOf<CatalogItem?>(null) }

    KeepScreenOn()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            // Un messaggio alla volta: il nuovo sostituisce quello ancora visibile.
            snackbar.currentSnackbarData?.dismiss()
            launch {
                when (event) {
                    is ShoppingEvent.Removed -> {
                        val result = snackbar.showSnackbar(
                            "${event.item.name} eliminato",
                            actionLabel = "Annulla",
                            duration = SnackbarDuration.Long,
                        )
                        if (result == SnackbarResult.ActionPerformed) viewModel.undoRemove(event.item)
                    }
                    is ShoppingEvent.AlreadyInList -> snackbar.showSnackbar("${event.name} è già nella lista")
                    is ShoppingEvent.BackInList -> snackbar.showSnackbar("${event.name} rimesso tra le cose da comprare")
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            AddItemField(
                query = viewModel.query,
                onQueryChange = viewModel::onQueryChange,
                onAdd = viewModel::addTyped,
                suggestions = state.suggestions,
                onSuggestionClick = viewModel::addSuggestion,
                onSuggestionRemove = { removingSuggestion = it },
                modifier = Modifier.padding(16.dp),
            )
            when {
                state.isLoading -> LoadingScreen()
                state.toBuy.isEmpty() && state.inCart.isEmpty() ->
                    EmptyState("La lista è vuota.\nScrivi qui sopra cosa serve.")
                else -> ShoppingList(
                    toBuy = state.toBuy,
                    inCart = state.inCart,
                    onToggle = viewModel::toggle,
                    onQuantityClick = { editingQuantity = it },
                    onRemove = viewModel::remove,
                    onClearCart = { confirmClear = true },
                )
            }
        }
    }

    editingQuantity?.let { item ->
        QuantityDialog(
            item = item,
            onSave = { quantity, unit -> viewModel.setQuantity(item, quantity, unit); editingQuantity = null },
            onClear = { viewModel.setQuantity(item, null, null); editingQuantity = null },
            onDismiss = { editingQuantity = null },
        )
    }

    removingSuggestion?.let { item ->
        AlertDialog(
            onDismissRequest = { removingSuggestion = null },
            title = { Text("Togliere «${item.name}» dai suggerimenti?") },
            text = {
                Text(
                    "Non comparirà più mentre scrivi. Se è già nella lista della spesa, lì resta.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.removeSuggestion(item); removingSuggestion = null }) {
                    Text("Sì, togli", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { removingSuggestion = null }) { Text("Annulla") }
            },
        )
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Svuotare il carrello?") },
            text = {
                Text(
                    "I ${state.inCart.size} prodotti già comprati verranno tolti dalla lista.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearCart(); confirmClear = false }) { Text("Sì, svuota") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Annulla") }
            },
        )
    }
}

@Composable
private fun ShoppingList(
    toBuy: List<ShoppingItem>,
    inCart: List<ShoppingItem>,
    onToggle: (ShoppingItem) -> Unit,
    onQuantityClick: (ShoppingItem) -> Unit,
    onRemove: (ShoppingItem) -> Unit,
    onClearCart: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "header-to-buy") {
            SectionHeader("Da comprare", toBuy.size, Modifier.animateItem())
        }
        if (toBuy.isEmpty()) {
            item(key = "all-done") {
                Text(
                    "Hai preso tutto!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.animateItem().padding(vertical = 8.dp),
                )
            }
        }
        items(toBuy, key = { it.id }) { item ->
            ShoppingRow(
                item = item,
                onToggle = { onToggle(item) },
                onQuantityClick = { onQuantityClick(item) },
                onRemove = { onRemove(item) },
                modifier = Modifier.animateItem(),
            )
        }

        if (inCart.isNotEmpty()) {
            item(key = "header-in-cart") {
                SectionHeader("Nel carrello", inCart.size, Modifier.animateItem()) {
                    OutlinedButton(onClick = onClearCart) {
                        Text("Svuota comprati", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            items(inCart, key = { it.id }) { item ->
                ShoppingRow(
                    item = item,
                    onToggle = { onToggle(item) },
                    onQuantityClick = null,
                    onRemove = null,
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}
