package com.pentacart.app.ui.meals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pentacart.app.data.model.Meal
import com.pentacart.app.data.model.MealCategory
import com.pentacart.app.ui.components.EmptyState
import com.pentacart.app.ui.components.LoadingScreen

@Composable
fun RecipeListScreen(
    onOpenMeal: (String) -> Unit,
    onCreateMeal: () -> Unit,
    viewModel: RecipeListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateMeal,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nuovo piatto", style = MaterialTheme.typography.labelLarge) },
            )
        },
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            CategoryFilter(selected = state.category, onSelect = viewModel::selectCategory)
            when {
                state.isLoading -> LoadingScreen()
                !state.hasAnyMeal -> EmptyState("Il ricettario è vuoto.\nTocca «Nuovo piatto» per aggiungere il primo.")
                state.meals.isEmpty() -> EmptyState("Nessun piatto in questa categoria.")
                else -> LazyColumn(
                    // Spazio in fondo per non finire sotto il pulsante "Nuovo piatto".
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.meals, key = { it.id }) { meal ->
                        MealCard(meal, onClick = { onOpenMeal(meal.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryFilter(selected: MealCategory?, onSelect: (MealCategory?) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text("Tutti", style = MaterialTheme.typography.labelLarge) },
            modifier = Modifier.height(48.dp),
        )
        MealCategory.entries.forEach { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelect(category) },
                label = { Text(category.label, style = MaterialTheme.typography.labelLarge) },
                modifier = Modifier.height(48.dp),
            )
        }
    }
}

@Composable
private fun MealCard(meal: Meal, onClick: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .clickable(onClick = onClick)
                .heightIn(min = 72.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(meal.title, style = MaterialTheme.typography.titleMedium)
                val details = listOfNotNull(
                    meal.category?.label,
                    meal.ingredients.size.takeIf { it > 0 }?.let { if (it == 1) "1 ingrediente" else "$it ingredienti" },
                )
                if (details.isNotEmpty()) {
                    Text(
                        details.joinToString(" · "),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
