package com.pentacart.app.data.repository

import com.pentacart.app.data.model.ShoppingItem
import org.junit.Assert.assertEquals
import org.junit.Test

class AdditionPlanTest {
    private val current = listOf(
        ShoppingItem(id = "1", name = "Latte"),
        ShoppingItem(id = "2", name = "Pane", isChecked = true),
    )

    @Test
    fun `divide nuovi, da rimettere in lista e già presenti`() {
        val plan = planAdditions(listOf("uova", " LATTE ", "pane"), current)
        assertEquals(listOf("uova"), plan.toAdd)
        assertEquals(listOf("2"), plan.toUncheck.map { it.id })
        assertEquals(listOf("1"), plan.alreadyToBuy.map { it.id })
    }

    @Test
    fun `ignora nomi vuoti e ripetuti`() {
        val plan = planAdditions(listOf("Uova", "uova", "  "), current)
        assertEquals(listOf("Uova"), plan.toAdd)
    }

    @Test
    fun `se è sia da comprare sia nel carrello non crea niente`() {
        val both = current + ShoppingItem(id = "3", name = "latte", isChecked = true)
        val plan = planAdditions(listOf("Latte"), both)
        assertEquals(listOf("1"), plan.alreadyToBuy.map { it.id })
        assertEquals(emptyList<ShoppingItem>(), plan.toUncheck)
    }
}
