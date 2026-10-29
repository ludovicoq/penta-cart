package com.pentacart.app.ui.plan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pentacart.app.data.model.Meal
import com.pentacart.app.data.model.MealType
import com.pentacart.app.data.model.PlanSlot
import com.pentacart.app.data.repository.MealRepository
import com.pentacart.app.data.repository.PlanRepository
import com.pentacart.app.util.WeekDates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

/** Cosa mostrare in un riquadro Pranzo/Cena. [title] null = vuoto. */
data class SlotView(
    val date: LocalDate,
    val mealType: MealType,
    val title: String? = null,
    /** Solo se il piatto esiste ancora nel ricettario: toccandolo si apre la ricetta. */
    val mealId: String? = null,
    val isNote: Boolean = false,
) {
    val isEmpty: Boolean get() = title == null
}

data class DayPlan(val date: LocalDate, val lunch: SlotView, val dinner: SlotView)

data class PlanUiState(
    val weekStart: LocalDate,
    val today: LocalDate,
    val days: List<DayPlan> = emptyList(),
    val meals: List<Meal> = emptyList(),
    val isLoading: Boolean = true,
) {
    val isCurrentWeek: Boolean get() = weekStart == WeekDates.mondayOf(today)
}

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val plan: PlanRepository,
    meals: MealRepository,
) : ViewModel() {
    private val weekStart = MutableStateFlow(WeekDates.mondayOf(LocalDate.now()))

    @OptIn(ExperimentalCoroutinesApi::class)
    private val week = weekStart.flatMapLatest { monday -> plan.observeWeek(monday).map { monday to it } }

    val uiState: StateFlow<PlanUiState> = combine(week, meals.observeMeals()) { (monday, slots), mealList ->
        PlanUiState(
            weekStart = monday,
            today = LocalDate.now(),
            days = buildDays(monday, slots, mealList),
            meals = mealList,
            isLoading = false,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PlanUiState(weekStart = weekStart.value, today = LocalDate.now()),
    )

    fun previousWeek() = weekStart.update { it.minusWeeks(1) }

    fun nextWeek() = weekStart.update { it.plusWeeks(1) }

    fun currentWeek() {
        weekStart.value = WeekDates.mondayOf(LocalDate.now())
    }

    fun chooseMeal(slot: SlotView, meal: Meal) = plan.setMeal(slot.date, slot.mealType, meal)

    fun setNote(slot: SlotView, note: String) {
        if (note.isBlank()) clear(slot) else plan.setNote(slot.date, slot.mealType, note)
    }

    fun clear(slot: SlotView) = plan.clearSlot(slot.date, slot.mealType)
}

internal fun buildDays(monday: LocalDate, slots: List<PlanSlot>, meals: List<Meal>): List<DayPlan> {
    val slotsById = slots.associateBy { it.id }
    val mealsById = meals.associateBy { it.id }

    fun view(date: LocalDate, type: MealType): SlotView {
        val slot = slotsById[PlanSlot.slotId(date, type)] ?: return SlotView(date, type)
        return when {
            slot.mealId != null -> {
                val meal = mealsById[slot.mealId]
                // Titolo aggiornato se il piatto è stato rinominato; quello salvato se è stato eliminato.
                SlotView(date, type, title = meal?.title ?: slot.mealTitle ?: "Piatto eliminato", mealId = meal?.id)
            }
            !slot.customNote.isNullOrBlank() -> SlotView(date, type, title = slot.customNote, isNote = true)
            else -> SlotView(date, type)
        }
    }

    return WeekDates.daysOf(monday).map { date ->
        DayPlan(date, lunch = view(date, MealType.LUNCH), dinner = view(date, MealType.DINNER))
    }
}
