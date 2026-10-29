package com.pentacart.app.ui.plan

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.pentacart.app.data.model.Meal
import com.pentacart.app.data.model.MealCategory
import com.pentacart.app.data.model.MealType
import com.pentacart.app.util.TextNormalizer
import com.pentacart.app.util.WeekDates

/**
 * Selettore rapido per un riquadro del calendario. Un solo campo fa due cose: filtra i piatti
 * del ricettario e, se non si trova niente, il testo si salva come nota libera (es. "Pizza fuori").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotPickerSheet(
    slot: SlotView,
    meals: List<Meal>,
    onPickMeal: (Meal) -> Unit,
    onSaveNote: (String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val visibleMeals = remember(meals, query, slot.mealType) { filterMeals(meals, query, slot.mealType) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier.padding(horizontal = 16.dp).imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "${WeekDates.dayLabel(slot.date)} – ${slot.mealType.label}",
                style = MaterialTheme.typography.titleLarge,
            )
            if (!slot.isEmpty) {
                Text(
                    "Ora: ${slot.title}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Cerca un piatto o scrivi una nota") },
                textStyle = MaterialTheme.typography.titleMedium,
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (query.isNotBlank()) onSaveNote(query) }),
                modifier = Modifier.fillMaxWidth(),
            )

            if (query.isNotBlank()) {
                FilledTonalButton(
                    onClick = { onSaveNote(query) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                ) {
                    Icon(Icons.AutoMirrored.Outlined.StickyNote2, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Usa come nota: «${query.trim()}»", style = MaterialTheme.typography.labelLarge)
                }
            }

            if (!slot.isEmpty) {
                OutlinedButton(
                    onClick = onClear,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) { Text("Svuota questo pasto", style = MaterialTheme.typography.labelLarge) }
            }

            when {
                meals.isEmpty() -> Text(
                    "Il ricettario è vuoto: puoi scrivere una nota qui sopra, o aggiungere piatti dalla tab Ricettario.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
                visibleMeals.isEmpty() -> Text(
                    "Nessun piatto con questo nome.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
                else -> {
                    Text("Dal ricettario", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    LazyColumn(Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                        items(visibleMeals, key = { it.id }) { meal ->
                            MealOption(meal, onClick = { onPickMeal(meal) })
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MealOption(meal: Meal, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 60.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(meal.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        meal.category?.let {
            Text(it.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Piatti che contengono il testo; per la cena prima quelli segnati "Cena", per il pranzo quelli "Pranzo". */
internal fun filterMeals(meals: List<Meal>, query: String, mealType: MealType): List<Meal> {
    val q = TextNormalizer.key(query)
    val preferred = if (mealType == MealType.LUNCH) MealCategory.PRANZO else MealCategory.CENA
    return meals
        .filter { q.isEmpty() || TextNormalizer.key(it.title).contains(q) }
        .sortedByDescending { it.category == preferred }
}
