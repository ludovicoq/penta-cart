package com.pentacart.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/** L'account esiste ma il suo UID non è ancora in config/family.members. */
@Composable
fun NotAuthorizedScreen(uid: String, onSignOut: () -> Unit) {
    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text("Account non ancora abilitato", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 32.dp))
            Text(
                "Chiedi a chi gestisce l'app di aggiungere questo codice all'elenco della famiglia:",
                style = MaterialTheme.typography.bodyLarge,
            )
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium) {
                SelectionContainer {
                    Text(
                        uid,
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
            OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth().height(64.dp)) {
                Text("Esci", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
