package com.oop.traveloop.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.oop.traveloop.ui.theme.SenjaSand
import com.oop.traveloop.ui.theme.SenjaTeal

@Composable
fun SelectableCard(selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val borderWidth by animateDpAsState(if (selected) 2.dp else 1.dp, label = "selectableCardBorder")
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.98f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "selectableCardScale",
    )
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().scale(scale),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) SenjaSand.copy(alpha = 0.12f) else Color.White),
        border = BorderStroke(borderWidth, if (selected) SenjaTeal else Color(0xFFEAEAEA)),
    ) {
        content()
    }
}
