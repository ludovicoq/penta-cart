package com.pentacart.app.ui.meals

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pentacart.app.data.model.MealCategory
import com.pentacart.app.ui.components.LoadingScreen
import com.pentacart.app.ui.components.SubScreenTopBar
import com.pentacart.app.ui.shopping.AddItemField

@Composable
fun RecipeEditScreen(
    onDone: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: RecipeEditViewModel = hiltViewModel(),
) {
    val suggestions by viewModel.ingredientSuggestions.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    val save = { if (viewModel.save()) onDone() }

    Scaffold(
        topBar = {
            SubScreenTopBar(title = if (viewModel.isNew) "Nuovo piatto" else "Modifica piatto", onBack = onDone) {
                TextButton(onClick = save, enabled = viewModel.canSave) {
                    Text("Salva", style = MaterialTheme.typography.labelLarge)
                }
            }
        },
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        if (viewModel.isLoading) {
            LoadingScreen(Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = viewModel.title,
                onValueChange = { viewModel.title = it },
                label = { Text("Nome del piatto") },
                placeholder = { Text("es. Polpette al sugo") },
                textStyle = MaterialTheme.typography.titleMedium,
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Categoria (facoltativa)", style = MaterialTheme.typography.titleMedium)
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MealCategory.entries.forEach { option ->
                    FilterChip(
                        selected = viewModel.category == option,
                        // Un secondo tocco sulla stessa categoria la toglie.
                        onClick = { viewModel.category = if (viewModel.category == option) null else option },
                        label = { Text(option.label, style = MaterialTheme.typography.labelLarge) },
                        modifier = Modifier.height(48.dp),
                    )
                }
            }

            Text("Ingredienti", style = MaterialTheme.typography.titleMedium)
            viewModel.ingredients.forEachIndexed { index, ingredient ->
                Row(Modifier.heightIn(min = 52.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("•  $ingredient", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.removeIngredient(index) }, modifier = Modifier.size(52.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Togli $ingredient")
                    }
                }
            }
            AddItemField(
                query = viewModel.ingredientQuery,
                onQueryChange = { viewModel.ingredientQuery = it },
                onAdd = { viewModel.addIngredient() },
                suggestions = suggestions,
                onSuggestionClick = { viewModel.addIngredient(it.item.name) },
                placeholder = "Ingrediente",
                alreadyAddedLabel = "già aggiunto",
            )

            Text("Procedimento", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = viewModel.instructions,
                onValueChange = { viewModel.instructions = it },
                placeholder = { Text("Come si prepara (facoltativo)") },
                textStyle = MaterialTheme.typography.bodyLarge,
                minLines = 4,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = save,
                enabled = viewModel.canSave,
                modifier = Modifier.fillMaxWidth().height(60.dp),
            ) { Text("Salva piatto", style = MaterialTheme.typography.labelLarge) }

            if (!viewModel.isNew) {
                OutlinedButton(
                    onClick = { confirmDelete = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) { Text("Elimina piatto", style = MaterialTheme.typography.labelLarge) }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Eliminare «${viewModel.title}»?") },
            text = {
                Text(
                    "Il piatto sparirà dal ricettario. Nei giorni in cui era in calendario resterà solo il nome.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(); confirmDelete = false; onDeleted() }) {
                    Text("Sì, elimina", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Annulla") }
            },
        )
    }
}
