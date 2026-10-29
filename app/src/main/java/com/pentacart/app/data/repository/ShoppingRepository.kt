package com.pentacart.app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.WriteBatch
import com.pentacart.app.data.firebase.Collections
import com.pentacart.app.data.firebase.Fields
import com.pentacart.app.data.firebase.logFailure
import com.pentacart.app.data.firebase.snapshotFlow
import com.pentacart.app.data.firebase.toCatalogItem
import com.pentacart.app.data.firebase.toShoppingItem
import com.pentacart.app.data.model.CatalogItem
import com.pentacart.app.data.model.QuantityUnit
import com.pentacart.app.data.model.ShoppingItem
import com.pentacart.app.data.model.User
import com.pentacart.app.util.TextNormalizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/** Lista della spesa condivisa e catalogo storico dei prodotti (per i suggerimenti). */
@Singleton
class ShoppingRepository @Inject constructor(
    private val db: FirebaseFirestore,
) {
    private val list get() = db.collection(Collections.SHOPPING)
    private val catalog get() = db.collection(Collections.CATALOG)

    /**
     * Prima i prodotti da comprare (in ordine di inserimento), poi quelli già nel carrello
     * (l'ultimo spuntato in cima, così si vede subito cosa si è appena preso).
     */
    fun observeList(): Flow<List<ShoppingItem>> =
        list.snapshotFlow().map { snapshot ->
            val items = snapshot.documents.map { it.toShoppingItem() }
            val (checked, toBuy) = items.partition { it.isChecked }
            toBuy.sortedBy { it.createdAt } + checked.sortedByDescending { it.checkedAt ?: 0 }
        }

    /**
     * Tutto il catalogo, dal più usato. È piccolo (qualche centinaio di prodotti al massimo),
     * quindi i suggerimenti si filtrano in memoria: Firestore non sa cercare per sottostringa.
     */
    fun observeCatalog(): Flow<List<CatalogItem>> =
        catalog.orderBy(Fields.USAGE_COUNT, Query.Direction.DESCENDING)
            .snapshotFlow()
            .map { snapshot -> snapshot.documents.map { it.toCatalogItem() } }

    /**
     * Aggiunge i prodotti evitando i doppioni (vedi [planAdditions]): quelli nuovi vengono inseriti,
     * quelli già nel carrello tornano da comprare. Restituisce cosa è stato fatto, per i messaggi.
     */
    fun addWithoutDuplicates(rawNames: List<String>, addedBy: User, current: List<ShoppingItem>): AdditionPlan {
        val plan = planAdditions(rawNames, current)
        addItems(plan.toAdd, addedBy)
        plan.toUncheck.forEach { setChecked(it.id, false) }
        return plan
    }

    /** Aggiunge i prodotti alla lista e aggiorna il catalogo, in un'unica scrittura atomica. */
    private fun addItems(rawNames: List<String>, addedBy: User) {
        val names = rawNames.map(TextNormalizer::display).filter { it.isNotEmpty() }
        if (names.isEmpty()) return
        val batch = db.batch()
        names.forEach { name ->
            batch.set(
                list.document(),
                mapOf(
                    Fields.NAME to name,
                    Fields.IS_CHECKED to false,
                    Fields.ADDED_BY_UID to addedBy.uid,
                    Fields.ADDED_BY_NAME to addedBy.displayName,
                    Fields.CREATED_AT to FieldValue.serverTimestamp(),
                    Fields.CHECKED_AT to null,
                ),
            )
            batch.recordUsage(name)
        }
        batch.commit().logFailure("addItems")
    }

    fun setChecked(itemId: String, checked: Boolean) {
        list.document(itemId)
            .update(
                Fields.IS_CHECKED, checked,
                Fields.CHECKED_AT, if (checked) FieldValue.serverTimestamp() else null,
            )
            .logFailure("setChecked")
    }

    /** [quantity] e [unit] null tolgono la quantità. */
    fun setQuantity(itemId: String, quantity: Double?, unit: QuantityUnit?) {
        list.document(itemId)
            .update(Fields.QUANTITY, quantity, Fields.UNIT, unit?.name)
            .logFailure("setQuantity")
    }

    fun removeItem(itemId: String) {
        list.document(itemId).delete().logFailure("removeItem")
    }

    /** Annulla un'eliminazione: rimette il prodotto con lo stesso ID e la stessa posizione. */
    fun restoreItem(item: ShoppingItem) {
        list.document(item.id)
            .set(
                mapOf(
                    Fields.NAME to item.name,
                    Fields.IS_CHECKED to item.isChecked,
                    Fields.ADDED_BY_UID to item.addedByUid,
                    Fields.ADDED_BY_NAME to item.addedByName,
                    Fields.CREATED_AT to Timestamp(Date(item.createdAt)),
                    Fields.CHECKED_AT to item.checkedAt?.let { Timestamp(Date(it)) },
                    Fields.QUANTITY to item.quantity,
                    Fields.UNIT to item.unit?.name,
                ),
            )
            .logFailure("restoreItem")
    }

    /**
     * "Svuota comprati". Riceve gli ID dalla UI invece di interrogare il server,
     * così funziona anche offline.
     */
    fun clearChecked(itemIds: List<String>) {
        if (itemIds.isEmpty()) return
        val batch = db.batch()
        itemIds.forEach { batch.delete(list.document(it)) }
        batch.commit().logFailure("clearChecked")
    }

    /** Per togliere dai suggerimenti un prodotto scritto male. */
    fun removeFromCatalog(catalogId: String) {
        catalog.document(catalogId).delete().logFailure("removeFromCatalog")
    }

    /**
     * Crea la voce di catalogo alla prima occorrenza e incrementa il contatore le volte successive.
     * L'ID è il nome normalizzato, quindi "Latte" e " latte " finiscono nello stesso documento.
     */
    private fun WriteBatch.recordUsage(name: String) {
        set(
            catalog.document(TextNormalizer.key(name)),
            mapOf(Fields.NAME to name, Fields.USAGE_COUNT to FieldValue.increment(1)),
            SetOptions.merge(),
        )
    }
}
