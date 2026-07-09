package com.oop.traveloop.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.User
import com.oop.traveloop.domain.model.UserProfile
import com.oop.traveloop.ui.components.BrandTopBar
import com.oop.traveloop.ui.theme.SenjaMist
import com.oop.traveloop.ui.theme.SenjaSand
import com.oop.traveloop.ui.theme.SenjaSunset

@Composable
fun ProfileScreen(profile: UserProfile?, onLogout: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        item { BrandTopBar("Profil penjelajah") }
        item {
            Box(
                Modifier.size(112.dp).clip(CircleShape).background(Brush.linearGradient(listOf(SenjaSunset, SenjaSand))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Lucide.User, null, tint = Color.White, modifier = Modifier.size(68.dp))
            }
        }
        item {
            Text(profile?.name?.takeIf(String::isNotBlank) ?: "Memuat profil", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 16.dp))
            Text(profile?.email?.takeIf(String::isNotBlank) ?: "Akun aktif", color = SenjaMist)
        }
        item {
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Lucide.LogOut, null)
                Spacer(Modifier.width(8.dp))
                Text("Keluar")
            }
        }
    }
}
