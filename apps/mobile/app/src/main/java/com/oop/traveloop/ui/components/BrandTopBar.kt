package com.oop.traveloop.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oop.traveloop.ui.theme.SenjaMist

@Composable
fun BrandTopBar(subtitle: String? = null) {
    Text(
        text = subtitle.orEmpty(),
        style = MaterialTheme.typography.labelMedium,
        color = SenjaMist,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
    )
}
