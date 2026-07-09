package com.oop.traveloop.ui.auth

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AuthNavHost(viewModel: AuthViewModel) {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "login") {
        composable("login") { LoginScreen(viewModel, onNavigateToRegister = { navController.navigate("register") }) }
        composable("register") { RegisterScreen(viewModel, onNavigateToLogin = { navController.popBackStack() }) }
    }
}
