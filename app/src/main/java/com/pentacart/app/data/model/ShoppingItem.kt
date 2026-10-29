package com.pentacart.app.data.model

import com.pentacart.app.util.QuantityFormat

data class ShoppingItem(
    val id: String,
    val name: String,
    val isChecked: Boolean = false,
    val addedByUid: String = "",
    val addedByName: String = "",
    /** Millisecondi epoch; per le scritture non ancora confermate dal server è una stima locale. */
    val createdAt: Long = 0,
    val checkedAt: Long? = null,
    /** Facoltativa: un prodotto può essere in lista anche senza quantità. */
    val quantity: Double? = null,
    val unit: QuantityUnit? = null,
) {
    val quantityLabel: String? get() = QuantityFormat.label(quantity, unit)
}
