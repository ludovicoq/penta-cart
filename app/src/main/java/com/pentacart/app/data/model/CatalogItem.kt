package com.pentacart.app.data.model

/** Prodotto già usato almeno una volta. L'[id] è il nome normalizzato (vedi TextNormalizer.key). */
data class CatalogItem(
    val id: String,
    val name: String,
    val category: String = "",
    val usageCount: Long = 0,
)
