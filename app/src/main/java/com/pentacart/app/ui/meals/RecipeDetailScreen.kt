package com.pentacart.app.ui.meals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pentacart.app.data.model.Meal
import com.pentacart.app.ui.components.EmptyState
import com.pentacart.app.ui.components.LoadingScreen
import com.pentacart.app.ui.components.SubScreenTopBar
import kotlinx.coroutines.launch

@Composable
fun RecipeDetailScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: RecipeDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var choosingIngredients by remember { mutableStateOf(false) }
    val meal = state.meal

    LaunchedEffect(viewModel) {
        viewModel.addedCount.collect { count ->
            snackbar.currentSnackbarData?.dismiss()
            launch {
                snackbar.showSnackbar(
                    when (count) {
                        0 -> "Erano già tutti nella lista della spesa"
                        1 -> "1 ingrediente aggiunto alla spesa"
                        else -> "$count ingredienti aggiunti alla spesa"
                    },
                )
            }
        }
    }

    Scaffold(
        topBar = {
            SubScreenTopBar(title = meal?.title ?: "Ricetta", onBack = onBack) {
                if (meal != null) {
                    IconButton(onClick = { onEdit(meal.id) }, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.Filled.Edit, contentDescription = "Modifica piatto")
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        when {
            state.isLoading -> LoadingScreen(Modifier.padding(padding))
            meal == null -> EmptyState("Questo piatto non esiste più.", Modifier.padding(padding))
            else -> RecipeContent(
                meal = meal,
                ingredients = state.ingredients,
                onAddToShopping = { choosingIngredients = true },
                modifier = Modifier.padding(padding),
            )
        }
    }

    if (choosingIngredients) {
        IngredientsToShoppingDialog(
            ingredients = state.ingredients,
            onConfirm = { names -> viewModel.addToShopping(names); choosingIngredients = false },
            onDismiss = { choosingIngredients = false },
        )
    }
}

@Composable
private fun RecipeContent(
    meal: Meal,
    ingredients: List<IngredientStatus>,
    onAddToShopping: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(meal.title, style = MaterialTheme.typography.headlineMedium)
        meal.category?.let {
            Text(it.label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }

        if (ingredients.isNotEmpty()) {
            Button(onClick = onAddToShopping, modifier = Modifier.fillMaxWidth().height(60.dp)) {
                Icon(Icons.Filled.AddShoppingCart, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text("Aggiungi ingredienti alla spesa", style = MaterialTheme.typography.labelLarge)
            }
        }

        Text("Ingredienti", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
        if (ingredients.isEmpty()) {
            Text("Nessun ingrediente.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        ingredients.forEach { ingredient ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("•  ${ingredient.name}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                if (ingredient.alreadyToBuy) {
                    Text("in lista", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 8.dp))

        Text("Procedimento", style = MaterialTheme.typography.titleLarge)
        Text(
            meal.instructions.ifBlank { "Nessuna nota." },
            style = MaterialTheme.typography.bodyLarge,
            color = if (meal.instructions.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Scelta degli ingredienti da mandare nella spesa. Sono già spuntati quelli che mancano,
 * cioè che non sono ancora tra le cose da comprare: di solito basta toccare "Aggiungi".
 */
@Composable
private fun IngredientsToShoppingDialog(
    ingredients: List<IngredientStatus>,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by remember { mutableStateOf(ingredients.filterNot { it.alreadyToBuy }.map { it.name }.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cosa manca?") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                ingredients.forEach { ingredient ->
                    val isSelected = ingredient.name in selected
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = isSelected,
                                role = Role.Checkbox,
                                onValueChange = { selected = if (it) selected + ingredient.name else selected - ingredient.name },
                            )
                            .heightIn(min = 56.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = isSelected, onCheckedChange = null, modifier = Modifier.padding(end = 12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(ingredient.name, style = MaterialTheme.typography.titleMedium)
                            if (ingredient.alreadyToBuy) {
                                Text(
                                    "già in lista",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected.toList()) }, enabled = selected.isNotEmpty()) {
                Text(if (selected.isEmpty()) "Aggiungi" else "Aggiungi (${selected.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        },
    )
}
