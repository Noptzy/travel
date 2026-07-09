package com.oop.traveloop.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.oop.traveloop.R
import com.oop.traveloop.ui.components.GradientHeader
import com.oop.traveloop.ui.components.PrimaryButton
import com.oop.traveloop.ui.theme.SenjaCanvas
import com.oop.traveloop.ui.theme.SenjaMist

@Composable
fun LoginScreen(viewModel: AuthViewModel, onNavigateToRegister: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.login_illustration))
    Column(Modifier.fillMaxSize().background(SenjaCanvas)) {
        GradientHeader {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Selamat datang kembali", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Text("Masuk untuk lanjut menyusun perjalanan.", color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.bodyMedium)
                LottieAnimation(composition, iterations = 1, modifier = Modifier.size(150.dp))
            }
        }
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().padding(24.dp),
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { viewModel.onAction(AuthAction.EmailChanged(it)) },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { viewModel.onAction(AuthAction.PasswordChanged(it)) },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                AnimatedVisibility(
                    visible = state.error != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Text(state.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(4.dp))
                PrimaryButton("Masuk", { viewModel.onAction(AuthAction.SubmitLogin) }, loading = state.isLoading)
                TextButton(onClick = onNavigateToRegister, modifier = Modifier.fillMaxWidth()) {
                    Text("Belum punya akun? Daftar")
                }
                Text("makeYour Jurney", color = SenjaMist, style = MaterialTheme.typography.labelMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}
