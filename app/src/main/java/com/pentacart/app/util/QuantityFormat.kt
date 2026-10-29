package com.pentacart.app.util

import com.pentacart.app.data.model.QuantityUnit
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object QuantityFormat {
    private val number = DecimalFormat("#.##", DecimalFormatSymbols(Locale.ITALIAN))

    /** 2.0 -> "2", 1.5 -> "1,5". */
    fun amount(value: Double): String = number.format(value)

    /** "500 g", "2 pz"; null se la quantità non è impostata. */
    fun label(quantity: Double?, unit: QuantityUnit?): String? =
        if (quantity == null || unit == null) null else "${amount(quantity)} ${unit.label}"

    /** Accetta sia la virgola sia il punto. null se il testo non è un numero maggiore di zero. */
    fun parse(text: String): Double? =
        text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }
}
