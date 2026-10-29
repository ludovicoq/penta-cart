package com.pentacart.app.util

import java.util.Locale

object TextNormalizer {
    private val spaces = Regex("\\s+")

    /** "  Latte   Intero " -> "latte intero". Usato come ID nel catalogo e per confrontare i nomi. */
    fun key(text: String): String =
        text.trim().lowercase(Locale.ITALIAN).replace(spaces, " ").replace('/', '-')

    /** "  latte   intero" -> "Latte intero". Forma da salvare e mostrare. */
    fun display(text: String): String =
        text.trim().replace(spaces, " ").replaceFirstChar { it.titlecase(Locale.ITALIAN) }
}
