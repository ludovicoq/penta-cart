package com.pentacart.app.ui.shopping

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pentacart.app.data.model.ShoppingItem

/**
 * Una riga della spesa. Tutta la riga spunta il prodotto (non solo la checkbox), così si
 * centra facilmente anche camminando. Quantità e cestino ci sono solo per i prodotti da comprare.
 */
@Composable
fun ShoppingRow(
    item: ShoppingItem,
    onToggle: () -> Unit,
    onQuantityClick: (() -> Unit)?,
    onRemove: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val checked = item.isChecked
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (checked) colors.surfaceVariant else colors.surface,
        border = if (checked) null else BorderStroke(2.dp, colors.outline),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier
                .toggleable(value = checked, role = Role.Checkbox, onValueChange = { onToggle() })
                .heightIn(min = 72.dp)
                .padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = null,
                modifier = Modifier.padding(16.dp).scale(1.4f),
            )
            Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (checked) colors.onSurfaceVariant else colors.onSurface,
                    textDecoration = if (checked) TextDecoration.LineThrough else null,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QuantityBadge(item, onQuantityClick)
                    if (item.addedByName.isNotBlank()) {
                        if (onQuantityClick != null || item.quantityLabel != null) Spacer(Modifier.width(10.dp))
                        Text(
                            "Aggiunto da: ${item.addedByName}",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            if (onRemove != null) {
                IconButton(onClick = onRemove, modifier = Modifier.size(56.dp)) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Elimina ${item.name}",
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
        }
    }
}

/** Da comprare: pulsante con la quantità (o "+ Quantità"). Nel carrello: solo il testo, se c'è. */
@Composable
private fun QuantityBadge(item: ShoppingItem, onClick: (() -> Unit)?) {
    val label = item.quantityLabel
    if (onClick != null) {
        AssistChip(
            onClick = onClick,
            label = {
                Text(
                    label ?: "Quantità",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (label != null) FontWeight.Bold else FontWeight.Normal,
                )
            },
            leadingIcon = if (label == null) {
                { Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize)) }
            } else {
                null
            },
            colors = if (label != null) {
                AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            } else {
                AssistChipDefaults.assistChipColors()
            },
        )
    } else if (label != null) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
