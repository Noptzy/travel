package com.oop.traveloop.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.composables.icons.lucide.CalendarDays
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.User
import com.composables.icons.lucide.Wallet
import com.oop.traveloop.domain.model.UserProfile
import com.oop.traveloop.ui.budget.BudgetScreen
import com.oop.traveloop.ui.home.HomeScreen
import com.oop.traveloop.ui.plan.PlanScreen
import com.oop.traveloop.ui.planner.PlannerAction
import com.oop.traveloop.ui.planner.PlannerScreen
import com.oop.traveloop.ui.planner.PlannerViewModel
import com.oop.traveloop.ui.profile.ProfileScreen
import com.oop.traveloop.ui.theme.SenjaCanvas
import com.oop.traveloop.ui.theme.SenjaTeal

private enum class Destination(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "Beranda", Lucide.House),
    Plan("plan", "Rencana", Lucide.CalendarDays),
    Budget("budget", "Anggaran", Lucide.Wallet),
    Profile("profile", "Profil", Lucide.User),
}

@Composable
fun TravelApp(viewModel: PlannerViewModel, profile: UserProfile?, onLogout: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val bottomRoutes = Destination.entries.map { it.route }
    var selectedPackageIndex by remember { mutableIntStateOf(1) }
    val selectedTransportIndices = remember { mutableStateMapOf<Int, Int>() }
    LaunchedEffect(state.plan) {
        val plan = state.plan ?: return@LaunchedEffect
        selectedPackageIndex = 1.coerceAtMost(plan.packages.lastIndex)
        selectedTransportIndices.clear()
        plan.packages.forEachIndexed { index, pack ->
            selectedTransportIndices[index] = pack.transportOptions.indexOfFirst { it.provider == pack.transport.provider }.coerceAtLeast(0)
        }
        navController.navigate(Destination.Plan.route) { launchSingleTop = true }
    }
    Scaffold(
        containerColor = SenjaCanvas,
        bottomBar = {
            if (currentRoute in bottomRoutes) {
                NavigationBar(containerColor = Color.White, tonalElevation = 3.dp) {
                    Destination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, destination.label) },
                            label = { Text(destination.label) },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = SenjaTeal.copy(alpha = 0.12f), selectedIconColor = SenjaTeal),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(navController, startDestination = Destination.Home.route, modifier = Modifier.padding(padding)) {
            val enter = slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { it / 3 } + fadeIn(tween(300))
            val exit = slideOutHorizontally(tween(300)) { -it / 3 } + fadeOut(tween(300))
            composable(Destination.Home.route, enterTransition = { enter }, exitTransition = { exit }) { HomeScreen({ navController.navigate("planner") }, state.plan) }
            composable("planner", enterTransition = { enter }, exitTransition = { exit }) { PlannerScreen(state, viewModel::onAction) { navController.popBackStack() } }
            composable(Destination.Plan.route, enterTransition = { enter }, exitTransition = { exit }) {
                PlanScreen(
                    plan = state.plan,
                    history = state.planHistory,
                    packageIndex = selectedPackageIndex,
                    onPackageSelected = { selectedPackageIndex = it },
                    transportIndex = selectedTransportIndices[selectedPackageIndex] ?: 0,
                    onTransportSelected = { selectedTransportIndices[selectedPackageIndex] = it },
                    onPlanSelected = { viewModel.onAction(PlannerAction.PlanSelected(it)) },
                    onCreate = { navController.navigate("planner") },
                )
            }
            composable(Destination.Budget.route, enterTransition = { enter }, exitTransition = { exit }) {
                val pack = state.plan?.packages?.getOrNull(selectedPackageIndex)
                BudgetScreen(state.plan, pack, pack?.transportOptions?.getOrNull(selectedTransportIndices[selectedPackageIndex] ?: 0))
            }
            composable(Destination.Profile.route, enterTransition = { enter }, exitTransition = { exit }) { ProfileScreen(profile, onLogout) }
        }
    }
}
