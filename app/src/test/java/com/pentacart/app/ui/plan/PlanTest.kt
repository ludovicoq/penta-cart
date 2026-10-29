package com.pentacart.app.ui.plan

import com.pentacart.app.data.model.Meal
import com.pentacart.app.data.model.MealCategory
import com.pentacart.app.data.model.MealType
import com.pentacart.app.data.model.PlanSlot
import com.pentacart.app.util.WeekDates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PlanTest {
    private val monday = LocalDate.of(2026, 11, 2)
    private val tuesday = monday.plusDays(1)

    @Test
    fun `la settimana parte dal lunedì`() {
        assertEquals(monday, WeekDates.mondayOf(LocalDate.of(2026, 11, 8)))
        assertEquals(monday, WeekDates.mondayOf(monday))
    }

    @Test
    fun `etichette in italiano`() {
        assertEquals("Lunedì 2", WeekDates.dayLabel(monday))
        assertEquals("2 – 8 novembre", WeekDates.weekLabel(monday))
        assertEquals("26 ottobre – 1 novembre", WeekDates.weekLabel(LocalDate.of(2026, 10, 26)))
    }

    @Test
    fun `sette giorni con riquadri vuoti dove non c'è niente`() {
        val days = buildDays(monday, emptyList(), emptyList())
        assertEquals(7, days.size)
        assertTrue(days.all { it.lunch.isEmpty && it.dinner.isEmpty })
    }

    @Test
    fun `piatto rinominato mostra il titolo nuovo, eliminato quello salvato`() {
        val meals = listOf(Meal(id = "m1", title = "Polpette al sugo"))
        val slots = listOf(
            PlanSlot(monday, MealType.LUNCH, mealId = "m1", mealTitle = "Polpette"),
            PlanSlot(monday, MealType.DINNER, mealId = "gone", mealTitle = "Lasagne"),
            PlanSlot(tuesday, MealType.DINNER, customNote = "Pizza fuori"),
        )
        val days = buildDays(monday, slots, meals)

        assertEquals("Polpette al sugo", days[0].lunch.title)
        assertEquals("m1", days[0].lunch.mealId)
        assertEquals("Lasagne", days[0].dinner.title)
        assertNull(days[0].dinner.mealId)
        assertEquals("Pizza fuori", days[1].dinner.title)
        assertTrue(days[1].dinner.isNote)
    }

    @Test
    fun `per la cena prima i piatti da cena`() {
        val meals = listOf(
            Meal(id = "1", title = "Pasta al pesto", category = MealCategory.PRANZO),
            Meal(id = "2", title = "Minestrone", category = MealCategory.CENA),
        )
        assertEquals(listOf("2", "1"), filterMeals(meals, "", MealType.DINNER).map { it.id })
        assertEquals(listOf("1"), filterMeals(meals, "PESTO", MealType.DINNER).map { it.id })
    }
}
