package com.pentacart.app.ui.shopping

import com.pentacart.app.data.model.CatalogItem
import com.pentacart.app.util.TextNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SuggestionsTest {
    // Come nel database: già ordinato per usageCount decrescente, ID = nome normalizzato.
    private val catalog = listOf("Pane", "Latte intero", "Panna da cucina", "Latte di soia", "Spinaci")
        .mapIndexed { i, name -> CatalogItem(TextNormalizer.key(name), name, usageCount = 10L - i) }

    private fun names(query: String) = suggestionsFor(query, catalog, emptySet()).map { it.item.name }

    @Test
    fun `testo vuoto non mostra suggerimenti`() {
        assertTrue(names("   ").isEmpty())
    }

    @Test
    fun `ignora maiuscole e spazi`() {
        assertEquals(listOf("Latte intero", "Latte di soia"), names("  LATTE "))
    }

    @Test
    fun `prima chi inizia con il testo, poi chi lo contiene`() {
        // "Latte di soia" è più usato, ma "Spinaci" inizia con "s".
        assertEquals(listOf("Spinaci", "Latte di soia"), names("s"))
    }

    @Test
    fun `a parità di corrispondenza vince il più usato`() {
        assertEquals(listOf("Latte intero", "Panna da cucina", "Spinaci"), names("in"))
    }

    @Test
    fun `segna i prodotti già presenti`() {
        val result = suggestionsFor("latte", catalog, setOf("latte intero"))
        assertEquals(listOf(true, false), result.map { it.alreadyAdded })
    }
}
