package com.pentacart.app.data.model

/**
 * Unità di misura per la quantità di un prodotto.
 * [step] è quanto cambia con i pulsanti − e +, [defaultAmount] il valore proposto quando si sceglie l'unità.
 */
enum class QuantityUnit(val label: String, val step: Double, val defaultAmount: Double) {
    PIECES("pz", 1.0, 1.0),
    PACKS("conf.", 1.0, 1.0),
    GRAMS("g", 100.0, 500.0),
    KILOGRAMS("kg", 0.5, 1.0),
    MILLILITERS("ml", 100.0, 500.0),
    LITERS("l", 0.5, 1.0),
}
