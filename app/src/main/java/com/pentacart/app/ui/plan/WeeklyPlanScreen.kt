package com.pentacart.app.ui.plan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pentacart.app.ui.components.LoadingScreen
import com.pentacart.app.util.WeekDates

@Composable
fun WeeklyPlanScreen(
    onOpenMeal: (String) -> Unit,
    viewModel: PlanViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var picking by remember { mutableStateOf<SlotView?>(null) }
    val listState = rememberLazyListState()

    // Nella settimana corrente si parte da oggi, non dal lunedì.
    LaunchedEffect(state.weekStart, state.isLoading) {
        if (!state.isLoading) {
            val todayIndex = state.days.indexOfFirst { it.date == state.today }
            listState.scrollToItem(if (state.isCurrentWeek && todayIndex >= 0) todayIndex else 0)
        }
    }

    Column(Modifier.fillMaxSize()) {
        WeekHeader(
            label = WeekDates.weekLabel(state.weekStart),
            isCurrentWeek = state.isCurrentWeek,
            onPrevious = viewModel::previousWeek,
            onNext = viewModel::nextWeek,
            onToday = viewModel::currentWeek,
        )
        if (state.isLoading) {
            LoadingScreen()
        } else {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.days, key = { it.date.toString() }) { day ->
                    DayCard(
                        day = day,
                        isToday = day.date == state.today,
                        onSlotClick = { slot ->
                            // Un piatto si apre; un riquadro vuoto o una nota si sceglie/modifica.
                            if (slot.mealId != null) onOpenMeal(slot.mealId) else picking = slot
                        },
                        onSlotEdit = { picking = it },
                    )
                }
            }
        }
    }

    picking?.let { slot ->
        SlotPickerSheet(
            slot = slot,
            meals = state.meals,
            onPickMeal = { meal -> viewModel.chooseMeal(slot, meal); picking = null },
            onSaveNote = { note -> viewModel.setNote(slot, note); picking = null },
            onClear = { viewModel.clear(slot); picking = null },
            onDismiss = { picking = null },
        )
    }
}

@Composable
private fun WeekHeader(
    label: String,
    isCurrentWeek: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious, modifier = Modifier.size(56.dp)) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Settimana precedente", modifier = Modifier.size(32.dp))
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            if (isCurrentWeek) {
                Text("Questa settimana", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            } else {
                TextButton(onClick = onToday) { Text("Torna a questa settimana") }
            }
        }
        IconButton(onClick = onNext, modifier = Modifier.size(56.dp)) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Settimana successiva", modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun DayCard(
    day: DayPlan,
    isToday: Boolean,
    onSlotClick: (SlotView) -> Unit,
    onSlotEdit: (SlotView) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(if (isToday) 3.dp else 2.dp, if (isToday) colors.primary else colors.outline),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(WeekDates.dayLabel(day.date), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (isToday) {
                    Text("Oggi", style = MaterialTheme.typography.labelLarge, color = colors.primary)
                }
            }
            SlotRow(day.lunch, onClick = { onSlotClick(day.lunch) }, onEdit = { onSlotEdit(day.lunch) })
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = colors.outline.copy(alpha = 0.3f))
            SlotRow(day.dinner, onClick = { onSlotClick(day.dinner) }, onEdit = { onSlotEdit(day.dinner) })
        }
    }
}

@Composable
private fun SlotRow(slot: SlotView, onClick: () -> Unit, onEdit: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 60.dp)
            .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            slot.mealType.label,
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurfaceVariant,
            modifier = Modifier.width(76.dp),
        )
        if (slot.isEmpty) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(6.dp))
            Text("Scegli", style = MaterialTheme.typography.titleMedium, color = colors.primary, modifier = Modifier.weight(1f))
        } else {
            if (slot.isNote) {
                Icon(Icons.AutoMirrored.Outlined.StickyNote2, contentDescription = "Nota", tint = colors.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(
                slot.title.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                fontStyle = if (slot.isNote) FontStyle.Italic else null,
                modifier = Modifier.weight(1f).padding(vertical = 8.dp),
            )
            IconButton(onClick = onEdit, modifier = Modifier.size(52.dp)) {
                Icon(Icons.Filled.Edit, contentDescription = "Cambia ${slot.mealType.label.lowercase()}", tint = colors.onSurfaceVariant)
            }
        }
    }
}
