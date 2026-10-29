package com.pentacart.app.ui.shopping

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.pentacart.app.data.model.CatalogItem

/**
 * Campo di testo con il pulsante Aggiungi e, sotto, i suggerimenti dal catalogo:
 * un tocco su un suggerimento lo aggiunge subito. Usato per la spesa e per gli ingredienti.
 * Dopo l'aggiunta la tastiera resta aperta, così si può scrivere subito il prodotto successivo.
 * Con [onSuggestionRemove] ogni suggerimento ha anche un cestino per toglierlo dal catalogo.
 */
@Composable
fun AddItemField(
    query: String,
    onQueryChange: (String) -> Unit,
    onAdd: () -> Unit,
    suggestions: List<Suggestion>,
    onSuggestionClick: (Suggestion) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Cosa serve?",
    alreadyAddedLabel: String = "già in lista",
    onSuggestionRemove: ((CatalogItem) -> Unit)? = null,
) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(placeholder, style = MaterialTheme.typography.titleMedium) },
                textStyle = MaterialTheme.typography.titleMedium,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onAdd() }),
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancella il testo")
                        }
                    }
                } else {
                    null
                },
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = onAdd,
                enabled = query.isNotBlank(),
                modifier = Modifier.height(60.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(26.dp))
                Spacer(Modifier.width(4.dp))
                Text("Aggiungi", style = MaterialTheme.typography.labelLarge)
            }
        }

        if (suggestions.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            ) {
                Column {
                    suggestions.forEachIndexed { index, suggestion ->
                        if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        SuggestionRow(
                            suggestion = suggestion,
                            alreadyAddedLabel = alreadyAddedLabel,
                            onClick = { onSuggestionClick(suggestion) },
                            onRemove = onSuggestionRemove?.let { remove -> { remove(suggestion.item) } },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionRow(
    suggestion: Suggestion,
    alreadyAddedLabel: String,
    onClick: () -> Unit,
    onRemove: (() -> Unit)?,
) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = !suggestion.alreadyAdded, onClick = onClick)
            .heightIn(min = 60.dp)
            .padding(start = 16.dp, end = if (onRemove != null) 4.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (suggestion.alreadyAdded) Icons.Filled.Check else Icons.Filled.Add,
            contentDescription = null,
            tint = if (suggestion.alreadyAdded) muted else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                suggestion.item.name,
                style = MaterialTheme.typography.titleMedium,
                color = if (suggestion.alreadyAdded) muted else MaterialTheme.colorScheme.onSurface,
            )
            if (suggestion.alreadyAdded) {
                Text(alreadyAddedLabel, style = MaterialTheme.typography.labelMedium, color = muted)
            }
        }
        if (onRemove != null) {
            IconButton(onClick = onRemove, modifier = Modifier.size(52.dp)) {
                Icon(
                    Icons.Filled.DeleteOutline,
                    contentDescription = "Togli ${suggestion.item.name} dai suggerimenti",
                    tint = muted,
                )
            }
        }
    }
}
