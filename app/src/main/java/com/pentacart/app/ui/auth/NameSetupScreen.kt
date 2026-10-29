package com.pentacart.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

private val QuickNames = listOf("Mamma", "Papà")

/** Primo accesso: si sceglie il nome che comparirà in "Aggiunto da: ...". */
@Composable
fun NameSetupScreen(onConfirm: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    val submit = { if (name.isNotBlank()) onConfirm(name) }

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text("Come ti chiami?", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(top = 32.dp))
            Text(
                "Il nome comparirà accanto ai prodotti che aggiungi alla spesa.",
                style = MaterialTheme.typography.bodyLarge,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickNames.forEach { quick ->
                    FilterChip(
                        selected = name == quick,
                        onClick = { name = quick },
                        label = { Text(quick, style = MaterialTheme.typography.titleMedium) },
                        modifier = Modifier.height(56.dp),
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome") },
                textStyle = MaterialTheme.typography.titleMedium,
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = submit,
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(64.dp),
            ) {
                Text("Continua", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
