package com.pentacart.app.ui.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pentacart.app.data.model.QuantityUnit
import com.pentacart.app.data.model.ShoppingItem
import com.pentacart.app.util.QuantityFormat

/** Quantità di un prodotto: pulsanti − / + grandi, numero modificabile e unità di misura con un tocco. */
@Composable
fun QuantityDialog(
    item: ShoppingItem,
    onSave: (Double, QuantityUnit) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    val startUnit = item.unit ?: QuantityUnit.PIECES
    var unit by remember { mutableStateOf(startUnit) }
    var amountText by remember { mutableStateOf(QuantityFormat.amount(item.quantity ?: startUnit.defaultAmount)) }
    val amount = QuantityFormat.parse(amountText)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(
                        onClick = {
                            val next = (amount ?: 0.0) - unit.step
                            if (next > 0) amountText = QuantityFormat.amount(next)
                        },
                        modifier = Modifier.size(56.dp),
                    ) { Icon(Icons.Filled.Remove, contentDescription = "Meno") }
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { text -> amountText = text.filter { it.isDigit() || it == ',' || it == '.' } },
                        textStyle = MaterialTheme.typography.headlineMedium.copy(textAlign = TextAlign.Center),
                        singleLine = true,
                        isError = amount == null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    )
                    FilledTonalIconButton(
                        onClick = { amountText = QuantityFormat.amount((amount ?: 0.0) + unit.step) },
                        modifier = Modifier.size(56.dp),
                    ) { Icon(Icons.Filled.Add, contentDescription = "Più") }
                }

                QuantityUnit.entries.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { option ->
                            FilterChip(
                                selected = option == unit,
                                onClick = {
                                    // Se il numero non è stato toccato, proponi quello tipico della nuova unità (es. 500 g).
                                    if (amount == unit.defaultAmount) amountText = QuantityFormat.amount(option.defaultAmount)
                                    unit = option
                                },
                                label = {
                                    Text(
                                        option.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                },
                                modifier = Modifier.weight(1f).height(48.dp),
                            )
                        }
                    }
                }

                if (item.quantity != null) {
                    TextButton(onClick = onClear) {
                        Text("Togli la quantità", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { amount?.let { onSave(it, unit) } }, enabled = amount != null) { Text("Salva") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        },
    )
}
