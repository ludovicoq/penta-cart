package com.pentacart.app.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pentacart.app.data.model.User
import com.pentacart.app.ui.meals.MEAL_ID_ARG
import com.pentacart.app.ui.meals.RecipeDetailScreen
import com.pentacart.app.ui.meals.RecipeEditScreen
import com.pentacart.app.ui.meals.RecipeListScreen
import com.pentacart.app.ui.plan.WeeklyPlanScreen
import com.pentacart.app.ui.shopping.ShoppingScreen

/**
 * Ogni tab ha il suo grafo di navigazione ([graph]) con la schermata principale ([start]) e
 * le eventuali schermate di dettaglio, così il Ricettario aperto dal calendario resta nella tab Pasti.
 */
private enum class Tab(val graph: String, val start: String, val label: String, val title: String, val icon: ImageVector) {
    SHOPPING("tab_spesa", "spesa", "Spesa", "Lista della spesa", Icons.Filled.ShoppingCart),
    PLAN("tab_pasti", "pasti", "Pasti", "Pasti della settimana", Icons.Filled.CalendarMonth),
    RECIPES("tab_ricettario", "ricettario", "Ricettario", "Ricettario", Icons.AutoMirrored.Filled.MenuBook),
}

private fun recipeRoute(prefix: String, mealId: String) = "$prefix/ricetta/$mealId"

private fun editRoute(prefix: String, mealId: String? = null) =
    if (mealId == null) "$prefix/modifica" else "$prefix/modifica?$MEAL_ID_ARG=$mealId"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(user: User, onRename: (String) -> Unit, onSignOut: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val currentTab = Tab.entries.firstOrNull { tab -> destination?.hierarchy?.any { it.route == tab.graph } == true }
        ?: Tab.SHOPPING
    // Le schermate di dettaglio hanno la loro barra con la freccia indietro.
    val onTabRoot = destination == null || destination.route == currentTab.start
    var showAccount by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (onTabRoot) {
                TopAppBar(
                    title = { Text(currentTab.title) },
                    actions = {
                        TextButton(onClick = { showAccount = true }, modifier = Modifier.height(56.dp)) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = null, modifier = Modifier.size(26.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(user.displayName, style = MaterialTheme.typography.labelLarge)
                        }
                    },
                )
            }
        },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = tab == currentTab,
                        onClick = {
                            if (tab == currentTab) {
                                // Ritoccare la tab attiva riporta alla sua schermata principale.
                                navController.popBackStack(tab.start, inclusive = false)
                            } else {
                                navController.navigate(tab.graph) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null, modifier = Modifier.size(28.dp)) },
                        label = { Text(tab.label, style = MaterialTheme.typography.labelLarge) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Tab.SHOPPING.graph,
            modifier = Modifier.padding(padding),
        ) {
            navigation(startDestination = Tab.SHOPPING.start, route = Tab.SHOPPING.graph) {
                composable(Tab.SHOPPING.start) { ShoppingScreen() }
            }
            navigation(startDestination = Tab.PLAN.start, route = Tab.PLAN.graph) {
                composable(Tab.PLAN.start) {
                    WeeklyPlanScreen(onOpenMeal = { navController.navigate(recipeRoute(Tab.PLAN.start, it)) })
                }
                recipeScreens(Tab.PLAN.start, navController)
            }
            navigation(startDestination = Tab.RECIPES.start, route = Tab.RECIPES.graph) {
                composable(Tab.RECIPES.start) {
                    RecipeListScreen(
                        onOpenMeal = { navController.navigate(recipeRoute(Tab.RECIPES.start, it)) },
                        onCreateMeal = { navController.navigate(editRoute(Tab.RECIPES.start)) },
                    )
                }
                recipeScreens(Tab.RECIPES.start, navController)
            }
        }
    }

    if (showAccount) {
        AccountDialog(
            currentName = user.displayName,
            onRename = { onRename(it); showAccount = false },
            onSignOut = { showAccount = false; onSignOut() },
            onDismiss = { showAccount = false },
        )
    }
}

/** Dettaglio e modifica di un piatto, registrati sotto la tab indicata da [prefix]. */
private fun NavGraphBuilder.recipeScreens(prefix: String, navController: NavController) {
    composable(
        route = "$prefix/ricetta/{$MEAL_ID_ARG}",
        arguments = listOf(navArgument(MEAL_ID_ARG) { type = NavType.StringType }),
    ) {
        RecipeDetailScreen(
            onBack = { navController.popBackStack() },
            onEdit = { navController.navigate(editRoute(prefix, it)) },
        )
    }
    composable(
        route = "$prefix/modifica?$MEAL_ID_ARG={$MEAL_ID_ARG}",
        arguments = listOf(
            navArgument(MEAL_ID_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
        ),
    ) {
        RecipeEditScreen(
            onDone = { navController.popBackStack() },
            // Il dettaglio del piatto eliminato non ha più senso: si torna alla schermata della tab.
            onDeleted = { navController.popBackStack(prefix, inclusive = false) },
        )
    }
}

/** Cambio nome ed uscita. L'uscita chiede conferma: rientrare richiede la password. */
@Composable
private fun AccountDialog(
    currentName: String,
    onRename: (String) -> Unit,
    onSignOut: () -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(currentName) }
    var confirmSignOut by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (confirmSignOut) "Vuoi davvero uscire?" else "Il tuo nome") },
        text = {
            if (confirmSignOut) {
                Text("Per rientrare servirà di nuovo email e password.", style = MaterialTheme.typography.bodyLarge)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        textStyle = MaterialTheme.typography.titleMedium,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = { onRename(name) },
                        enabled = name.isNotBlank() && name.trim() != currentName,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                    ) { Text("Salva nome", style = MaterialTheme.typography.labelLarge) }
                    HorizontalDivider()
                    OutlinedButton(
                        onClick = { confirmSignOut = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                    ) { Text("Esci dall'account", style = MaterialTheme.typography.labelLarge) }
                }
            }
        },
        confirmButton = {
            if (confirmSignOut) {
                TextButton(onClick = onSignOut) { Text("Sì, esci", color = MaterialTheme.colorScheme.error) }
            } else {
                TextButton(onClick = onDismiss) { Text("Chiudi") }
            }
        },
        dismissButton = {
            if (confirmSignOut) TextButton(onClick = { confirmSignOut = false }) { Text("No") }
        },
    )
}
