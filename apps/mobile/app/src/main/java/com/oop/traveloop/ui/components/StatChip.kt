package com.oop.traveloop.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.oop.traveloop.ui.theme.SenjaTeal

@Composable
fun StatChip(icon: ImageVector, label: String, modifier: Modifier = Modifier) {
    Surface(color = SenjaTeal.copy(alpha = 0.08f), shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = SenjaTeal, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = SenjaTeal, modifier = Modifier.padding(start = 6.dp))
        }
    }
}
