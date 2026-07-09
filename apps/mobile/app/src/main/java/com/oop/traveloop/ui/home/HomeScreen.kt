package com.oop.traveloop.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Car
import com.composables.icons.lucide.CircleCheckBig
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Luggage
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPinPlus
import com.composables.icons.lucide.Sparkles
import com.oop.traveloop.domain.model.TripPlan
import com.oop.traveloop.ui.common.formatDate
import com.oop.traveloop.ui.components.BrandTopBar
import com.oop.traveloop.ui.components.GradientHeader
import com.oop.traveloop.ui.theme.SenjaInk
import com.oop.traveloop.ui.theme.SenjaMist
import com.oop.traveloop.ui.theme.SenjaSand
import com.oop.traveloop.ui.theme.SenjaTeal
import com.oop.traveloop.ui.theme.SenjaTealDeep

@Composable
fun HomeScreen(onStart: () -> Unit, plan: TripPlan?) {
    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        item { BrandTopBar("AI travel planner personalmu") }
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text("Mau pergi ke mana?", style = MaterialTheme.typography.displayLarge, color = SenjaInk)
                Text("Susun perjalanan, budget, dan itinerary dalam sekali jalan.", style = MaterialTheme.typography.bodyMedium, color = SenjaMist, modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))
                GradientHeader {
                    Column {
                        Icon(Lucide.Sparkles, null, tint = SenjaSand, modifier = Modifier.size(32.dp))
                        Text("Rencana cerdas,\ntanpa ribet", color = Color.White, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 12.dp))
                        Text("Ceritakan tujuan dan budget. AI kami mengerjakan sisanya.", color = Color.White.copy(alpha = 0.82f), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 12.dp))
                        Button(
                            onClick = onStart,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = SenjaTealDeep),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                        ) {
                            Text("Mulai Rencanakan", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Icon(Lucide.ArrowRight, null)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text("Cara kerjanya", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                listOf(
                    Triple(Lucide.MapPinPlus, "Isi kebutuhan", "Asal, tujuan, durasi, orang, dan budget."),
                    Triple(Lucide.Sparkles, "AI meracik", "Data hotel, aktivitas, rute, dan budget dianalisis."),
                    Triple(Lucide.Luggage, "Pilih & berangkat", "Bandingkan paket lalu ikuti itinerary harian."),
                ).forEachIndexed { index, item -> StepCard(index + 1, item.first, item.second, item.third) }
                plan?.let {
                    Text("Rencana terakhir", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp, bottom = 10.dp))
                    SummaryCard(it)
                }
            }
        }
    }
}

@Composable
fun SummaryCard(plan: TripPlan, modifier: Modifier = Modifier) {
    Card(colors = CardDefaults.cardColors(SenjaSand.copy(alpha = 0.15f)), shape = RoundedCornerShape(18.dp), modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Lucide.CircleCheckBig, null, tint = SenjaTeal)
                Spacer(Modifier.width(9.dp))
                Text(if (plan.budget.remaining >= 0) "Budget Aman" else "Perlu Penyesuaian", color = SenjaTeal, fontWeight = FontWeight.Bold)
            }
            Text("${plan.origin} -> ${plan.destination}", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 12.dp))
            Text("${formatDate(plan.startDate)} - ${formatDate(plan.endDate)}", color = SenjaMist, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
            Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Metric(Lucide.Car, "${plan.distanceKm.toInt()} km", Modifier.weight(1f))
                Metric(Lucide.Clock, plan.durationLabel, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StepCard(number: Int, icon: ImageVector, title: String, text: String) {
    Card(Modifier.fillMaxWidth().padding(bottom = 10.dp), colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(16.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(SenjaTeal.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = SenjaTeal)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("$number. $title", fontWeight = FontWeight.Bold)
                Text(text, color = SenjaMist, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun Metric(icon: ImageVector, value: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = SenjaTeal)
        Spacer(Modifier.width(6.dp))
        Text(value, fontWeight = FontWeight.Bold)
    }
}
