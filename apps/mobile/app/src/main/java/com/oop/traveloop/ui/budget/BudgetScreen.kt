package com.oop.traveloop.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oop.traveloop.domain.model.Budget
import com.oop.traveloop.domain.model.TransportOption
import com.oop.traveloop.domain.model.TravelPackage
import com.oop.traveloop.domain.model.TripPlan
import com.oop.traveloop.ui.common.adjustedBudget
import com.oop.traveloop.ui.common.rupiah
import com.oop.traveloop.ui.components.BrandTopBar
import com.oop.traveloop.ui.components.EmptyState
import com.oop.traveloop.ui.components.PriceText
import com.oop.traveloop.ui.theme.SenjaMist
import com.oop.traveloop.ui.theme.SenjaSand
import com.oop.traveloop.ui.theme.SenjaSunset
import com.oop.traveloop.ui.theme.SenjaTeal
import kotlin.math.roundToLong

@Composable
fun BudgetScreen(plan: TripPlan?, selectedPackage: TravelPackage?, selectedTransport: TransportOption?) {
    val budget = if (selectedPackage != null && selectedTransport != null) adjustedBudget(selectedPackage, selectedTransport) else selectedPackage?.budget
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { BrandTopBar("Pantau anggaran perjalanan") }
        item { Text("Anggaran Perjalanan", style = MaterialTheme.typography.headlineMedium) }
        if (budget == null) {
            item { EmptyState("Belum ada data", "Buat rencana agar rincian anggaran tampil.") }
        } else {
            item { BudgetTotalCard(budget) }
            val days = plan?.itinerary?.size ?: 1
            val nights = (days - 1).coerceAtLeast(0)
            val people = (budget.food / (150_000.0 * days)).roundToLong().toInt().coerceAtLeast(1)
            item { Text("Paket ${selectedPackage?.type} - ${selectedTransport?.provider}", color = SenjaTeal, fontWeight = FontWeight.Bold) }
            item { Text("Alokasi Anggaran", style = MaterialTheme.typography.titleLarge) }
            val rows = listOf(
                Triple("Hotel & Penginapan", budget.hotel, "${rupiah(selectedPackage?.hotel?.price ?: 0.0)} x $nights malam"),
                Triple("Aktivitas", budget.activity, "${selectedPackage?.activities?.size ?: 0} tempat x $people orang"),
                Triple("Makan & Minum", budget.food, "${rupiah(150_000.0)} x $people orang x $days hari"),
                Triple("Transport antarkota", budget.intercityTransport, "${selectedTransport?.provider} - pulang-pergi"),
                Triple("Transport lokal", budget.localTransport, "${rupiah(200_000.0)} x $days hari"),
                Triple("Dana cadangan (10%)", budget.buffer, "10% x ${rupiah(budget.estimated - budget.buffer)}"),
            )
            itemsIndexed(rows) { index, (label, value, detail) -> BudgetRow(label, value, detail, budget.estimated, index) }
            item { Text("Dana cadangan untuk perubahan harga, biaya tak terduga, atau kebutuhan darurat. Bukan biaya wajib.", color = SenjaMist, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@Composable
private fun BudgetTotalCard(budget: Budget) {
    val color = if (budget.estimated <= budget.total) SenjaTeal else SenjaSunset
    Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text("TOTAL ANGGARAN", color = SenjaMist, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            PriceText(budget.total)
            LinearProgressIndicator(
                progress = { (budget.estimated / budget.total).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                color = color,
            )
            Text("Estimasi pengeluaran", color = SenjaMist, style = MaterialTheme.typography.bodyMedium)
            PriceText(budget.estimated)
            Text(
                if (budget.remaining >= 0) "Hemat ${rupiah(budget.remaining)}" else "Melebihi ${rupiah(-budget.remaining)}",
                color = color,
                modifier = Modifier.padding(top = 10.dp),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun BudgetRow(label: String, value: Double, detail: String, total: Double, index: Int) {
    val color = listOf(SenjaTeal, SenjaSunset, SenjaSand, SenjaMist)[index % 4]
    Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.Bold)
                Text(detail, color = SenjaMist, style = MaterialTheme.typography.bodyMedium)
                LinearProgressIndicator(
                    progress = { if (total == 0.0) 0f else (value / total).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    color = color,
                )
            }
            Text(rupiah(value), fontWeight = FontWeight.Bold)
        }
    }
}
