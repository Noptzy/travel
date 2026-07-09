package com.oop.traveloop.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.oop.traveloop.ui.theme.SenjaInk
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

@Composable
fun PriceText(value: Double, modifier: Modifier = Modifier, suffix: String = "") {
    val formatted = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).format(value.roundToLong()).replace(",00", "")
    Text("$formatted$suffix", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SenjaInk, modifier = modifier)
}
