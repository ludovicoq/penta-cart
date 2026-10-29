package com.pentacart.app.data.model

/** In che punto è l'utente: decide quale schermata mostrare all'avvio. */
sealed interface Session {
    data object Loading : Session
    data object SignedOut : Session

    /** Collegato, ma il suo UID non è in config/family.members. */
    data class NotAuthorized(val uid: String) : Session

    /** Collegato e autorizzato, ma non ha ancora scelto il nome visualizzato. */
    data class NeedsName(val uid: String) : Session

    data class Ready(val user: User) : Session
}
