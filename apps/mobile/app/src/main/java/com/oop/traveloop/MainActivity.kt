package com.oop.traveloop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import coil3.SingletonImageLoader
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oop.traveloop.ui.TravelApp
import com.oop.traveloop.ui.auth.AuthNavHost
import com.oop.traveloop.ui.auth.AuthViewModel
import com.oop.traveloop.ui.planner.PlannerViewModel
import com.oop.traveloop.ui.theme.TraveloopTheme

class MainActivity : ComponentActivity() {
    private val container by lazy { AppContainer(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SingletonImageLoader.setSafe { container.imageLoader }
        setContent {
            TraveloopTheme {
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModel.factory(container.login, container.register, container.logout, container.observeAuthSession, container.observeUserProfile, container.refreshUserProfile)
                )
                val session by authViewModel.session.collectAsStateWithLifecycle()
                val profile by authViewModel.profile.collectAsStateWithLifecycle()
                if (session == null) {
                    AuthNavHost(authViewModel)
                } else {
                    val plannerViewModel: PlannerViewModel = viewModel(factory = PlannerViewModel.factory(container.createTripPlan, container.planHistoryStore))
                    TravelApp(plannerViewModel, profile, onLogout = { authViewModel.logout() })
                }
            }
        }
    }
}
