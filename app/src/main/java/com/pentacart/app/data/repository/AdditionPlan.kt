package com.pentacart.app.data.repository

import com.pentacart.app.data.model.ShoppingItem
import com.pentacart.app.util.TextNormalizer

/** Come aggiungere dei prodotti alla lista senza creare doppioni. */
data class AdditionPlan(
    /** Nomi nuovi da inserire. */
    val toAdd: List<String>,
    /** Già nel carrello: tornano tra le cose da comprare. */
    val toUncheck: List<ShoppingItem>,
    /** Già da comprare: non si tocca niente. */
    val alreadyToBuy: List<ShoppingItem>,
)

fun planAdditions(rawNames: List<String>, current: List<ShoppingItem>): AdditionPlan {
    // Se lo stesso nome compare sia da comprare sia nel carrello, vince quello da comprare.
    val byKey = current.sortedBy { !it.isChecked }.associateBy { TextNormalizer.key(it.name) }
    val seen = HashSet<String>()
    val toAdd = mutableListOf<String>()
    val toUncheck = mutableListOf<ShoppingItem>()
    val alreadyToBuy = mutableListOf<ShoppingItem>()
    rawNames.forEach { raw ->
        val key = TextNormalizer.key(raw)
        if (key.isEmpty() || !seen.add(key)) return@forEach
        val existing = byKey[key]
        when {
            existing == null -> toAdd += raw
            existing.isChecked -> toUncheck += existing
            else -> alreadyToBuy += existing
        }
    }
    return AdditionPlan(toAdd, toUncheck, alreadyToBuy)
}
