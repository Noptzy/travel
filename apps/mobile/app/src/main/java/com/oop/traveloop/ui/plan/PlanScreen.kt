package com.oop.traveloop.ui.plan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.composables.icons.lucide.Bus
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Compass
import com.composables.icons.lucide.ExternalLink
import com.composables.icons.lucide.FerrisWheel
import com.composables.icons.lucide.Hotel
import com.composables.icons.lucide.ListFilter
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Map
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Plane
import com.composables.icons.lucide.Route
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Star
import com.oop.traveloop.domain.model.Activity
import com.oop.traveloop.domain.model.Hotel
import com.oop.traveloop.domain.model.ItineraryDay
import com.oop.traveloop.domain.model.TransportOption
import com.oop.traveloop.domain.model.TravelPackage
import com.oop.traveloop.domain.model.TripPlan
import com.oop.traveloop.ui.common.formatDate
import com.oop.traveloop.ui.common.mapsUrl
import com.oop.traveloop.ui.common.rupiah
import com.oop.traveloop.ui.components.BrandTopBar
import com.oop.traveloop.ui.components.EmptyState
import com.oop.traveloop.ui.components.PrimaryButton
import com.oop.traveloop.ui.components.SelectableCard
import com.oop.traveloop.ui.components.SectionTitle
import com.oop.traveloop.ui.home.SummaryCard
import com.oop.traveloop.ui.theme.SenjaCanvas
import com.oop.traveloop.ui.theme.SenjaInk
import com.oop.traveloop.ui.theme.SenjaMist
import com.oop.traveloop.ui.theme.SenjaSand
import com.oop.traveloop.ui.theme.SenjaTeal

@Composable
fun PlanScreen(
    plan: TripPlan?,
    history: List<TripPlan>,
    packageIndex: Int,
    onPackageSelected: (Int) -> Unit,
    transportIndex: Int,
    onTransportSelected: (Int) -> Unit,
    onPlanSelected: (String) -> Unit,
    onCreate: () -> Unit,
) {
    if (plan == null && history.isEmpty()) {
        EmptyPlan(onCreate)
        return
    }
    var mode by remember(plan?.id, history.size) { mutableStateOf(if (plan == null) "Riwayat" else "Detail") }
    if (mode == "Riwayat") {
        PlanHistory(
            currentPlan = plan,
            history = history,
            onCreate = onCreate,
            onShowDetail = { mode = "Detail" },
            onPlanSelected = { planId ->
                onPlanSelected(planId)
                mode = "Detail"
            },
        )
        return
    }
    if (plan == null) {
        PlanHistory(
            currentPlan = null,
            history = history,
            onCreate = onCreate,
            onShowDetail = {},
            onPlanSelected = onPlanSelected,
        )
        return
    }
    val selected = plan.packages.getOrNull(packageIndex)
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { BrandTopBar("${plan.origin} -> ${plan.destination}") }
        item { PlanModeSwitch(selected = "Detail", canShowDetail = true, onDetail = {}, onHistory = { mode = "Riwayat" }) }
        item { SummaryCard(plan) }
        item { PrimaryButton("Buat Rencana Baru", onCreate) }
        item { RouteMap(plan) }
        item {
            Text("Pilih paket", style = MaterialTheme.typography.headlineMedium)
            Text("Hotel dan jumlah aktivitas berbeda untuk tiap paket.", color = SenjaMist)
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(plan.packages) { pack ->
                    val index = plan.packages.indexOf(pack)
                    PackageCard(pack, selected = index == packageIndex, onClick = { onPackageSelected(index) })
                }
            }
        }
        selected?.hotel?.let { hotel -> item { SectionTitle("Penginapan Terbaik", Lucide.Hotel) } }
        selected?.hotel?.let { hotel -> item { Entrance { HotelCard(hotel, selected.reason) } } }
        selected?.let { pack ->
            item { SectionTitle("Pilih Transportasi", if (pack.transport.mode == "PESAWAT") Lucide.Plane else Lucide.Bus) }
            item { TransportChoices(pack.transportOptions, transportIndex, onTransportSelected) }
            item { SectionTitle("Aktivitas Seru", Lucide.FerrisWheel) }
            items(pack.activities) { activity -> Entrance { ActivityCard(activity) } }
        }
        item { SectionTitle("Itinerary", Lucide.Route) }
        items(plan.itinerary) { day -> Entrance { ItineraryCard(day) } }
    }
}

@Composable
private fun PlanHistory(
    currentPlan: TripPlan?,
    history: List<TripPlan>,
    onCreate: () -> Unit,
    onShowDetail: () -> Unit,
    onPlanSelected: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("Semua") }
    val filters = listOf("Semua", "Akan datang", "Budget aman", "Perlu penyesuaian")
    val filteredPlans = remember(history, query, filter) {
        history.filter { plan ->
            val searchText = listOf(
                plan.origin,
                plan.destination,
                plan.startDate,
                plan.endDate,
                plan.budget.status,
                rupiah(plan.budget.total),
                plan.packages.joinToString(" ") { it.type },
            ).joinToString(" ").lowercase()
            val matchesSearch = query.isBlank() || searchText.contains(query.lowercase())
            val matchesFilter = when (filter) {
                "Akan datang" -> runCatching { java.time.LocalDate.parse(plan.endDate) >= java.time.LocalDate.now() }.getOrDefault(false)
                "Budget aman" -> plan.budget.status in listOf("UNDER_BUDGET", "WITHIN_BUDGET")
                "Perlu penyesuaian" -> plan.budget.status in listOf("ALMOST_OVER_BUDGET", "OVER_BUDGET")
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { BrandTopBar("Riwayat rencana perjalanan") }
        item { PlanModeSwitch(selected = "Riwayat", canShowDetail = currentPlan != null, onDetail = onShowDetail, onHistory = {}) }
        item { PrimaryButton("Buat Rencana Baru", onCreate) }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Cari rencana") },
                leadingIcon = { Icon(Lucide.Search, null, tint = SenjaTeal) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SenjaTeal,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                ),
            )
        }
        item {
            Column {
                SectionTitle("Filter", Lucide.ListFilter)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(top = 10.dp)) {
                    items(filters) { item ->
                        FilterChip(
                            selected = filter == item,
                            onClick = { filter = item },
                            label = { Text(item) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SenjaSand,
                                selectedLabelColor = SenjaInk,
                            ),
                        )
                    }
                }
            }
        }
        if (history.isEmpty()) {
            item {
                EmptyState("Belum ada riwayat", "Rencana yang kamu buat akan muncul di sini.") {
                    PrimaryButton("Mulai Rencanakan", onCreate, modifier = Modifier.padding(top = 20.dp))
                }
            }
        } else if (filteredPlans.isEmpty()) {
            item { EmptyState("Tidak ada hasil", "Coba ubah kata kunci atau filter.") }
        } else {
            items(filteredPlans, key = { it.id }) { plan ->
                Entrance {
                    PlanHistoryCard(
                        plan = plan,
                        selected = plan.id == currentPlan?.id,
                        onClick = { onPlanSelected(plan.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanModeSwitch(
    selected: String,
    canShowDetail: Boolean,
    onDetail: () -> Unit,
    onHistory: () -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (canShowDetail) {
            item {
                FilterChip(
                    selected = selected == "Detail",
                    onClick = onDetail,
                    label = { Text("Detail") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SenjaSand,
                        selectedLabelColor = SenjaInk,
                    ),
                )
            }
        }
        item {
            FilterChip(
                selected = selected == "Riwayat",
                onClick = onHistory,
                label = { Text("Riwayat") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SenjaSand,
                    selectedLabelColor = SenjaInk,
                ),
            )
        }
    }
}

@Composable
private fun PlanHistoryCard(plan: TripPlan, selected: Boolean, onClick: () -> Unit) {
    val activityCount = plan.packages.maxOfOrNull { it.activities.size } ?: 0
    val hotelRating = plan.packages.mapNotNull { it.hotel?.rating }.maxOrNull() ?: 0.0
    val statusText = when (plan.budget.status) {
        "UNDER_BUDGET", "WITHIN_BUDGET" -> "Budget aman"
        "ALMOST_OVER_BUDGET" -> "Perlu penyesuaian"
        "OVER_BUDGET" -> "Melebihi budget"
        else -> plan.budget.status
    }
    SelectableCard(selected = selected, onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(42.dp).clip(CircleShape).background(SenjaTeal.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(Lucide.MapPin, null, tint = SenjaTeal)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("${plan.origin} -> ${plan.destination}", fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${formatDate(plan.startDate)} - ${formatDate(plan.endDate)}", color = SenjaMist, style = MaterialTheme.typography.bodyMedium)
                }
                Icon(Lucide.ChevronRight, null, tint = SenjaMist)
            }
            Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = SenjaTeal.copy(alpha = 0.1f), shape = RoundedCornerShape(999.dp)) {
                    Text(statusText, color = SenjaTeal, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
                Surface(color = SenjaSand.copy(alpha = 0.18f), shape = RoundedCornerShape(999.dp)) {
                    Text("$activityCount aktivitas", color = SenjaInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
            Row(Modifier.padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${plan.distanceKm.toInt()} km", color = SenjaMist, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(12.dp))
                Text(plan.durationLabel, color = SenjaMist, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                Icon(Lucide.Star, null, tint = SenjaSand, modifier = Modifier.size(15.dp))
                Text(" $hotelRating", color = SenjaInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            }
            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Budget ${rupiah(plan.budget.total)}", color = SenjaMist, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                Text(rupiah(plan.budget.estimated), color = SenjaInk, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PackageCard(pack: TravelPackage, selected: Boolean, onClick: () -> Unit) {
    SelectableCard(selected = selected, onClick = onClick, modifier = Modifier.width(174.dp).animateContentSize()) {
        Column(Modifier.padding(16.dp)) {
            Text(pack.type, color = if (selected) SenjaTeal else SenjaMist, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelMedium)
            Text("${pack.activities.size} aktivitas", color = SenjaInk, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
            Text("Hotel ${pack.hotel?.rating ?: 0.0}", color = SenjaMist, style = MaterialTheme.typography.bodyMedium)
            Text("${pack.transport.mode.lowercase().replaceFirstChar(Char::uppercase)} - ${pack.transport.provider}", color = SenjaTeal, style = MaterialTheme.typography.labelMedium, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 5.dp))
            Text(rupiah(pack.estimatedTotal), color = SenjaInk, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
        }
    }
}

@Composable
private fun TransportChoices(options: List<TransportOption>, selectedIndex: Int, onSelected: (Int) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(options.size) { index ->
            val transport = options[index]
            val selected = index == selectedIndex
            SelectableCard(selected = selected, onClick = { onSelected(index) }, modifier = Modifier.width(286.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(CircleShape).background(SenjaTeal.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Icon(if (transport.mode == "PESAWAT") Lucide.Plane else Lucide.Bus, null, tint = SenjaTeal)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(transport.provider, fontWeight = FontWeight.ExtraBold)
                        Text("${transport.mode.lowercase().replaceFirstChar(Char::uppercase)} - ${transport.service}", color = SenjaMist, style = MaterialTheme.typography.bodyMedium)
                        Text(transport.description, color = SenjaMist, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 4.dp))
                    }
                    Text(rupiah(transport.estimatedCost), fontWeight = FontWeight.Bold, color = SenjaTeal)
                }
            }
        }
    }
}

@Composable
private fun HotelCard(hotel: Hotel, reason: String) {
    val uriHandler = LocalUriHandler.current
    Card(
        onClick = { uriHandler.openUri(mapsUrl(hotel.sourceUrl, hotel.name, hotel.address)) },
        colors = CardDefaults.cardColors(Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFEAEAEA)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (!hotel.imageUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = hotel.imageUrl,
                contentDescription = hotel.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(190.dp),
                loading = {
                    Box(Modifier.fillMaxWidth().height(190.dp).background(SenjaTeal.copy(alpha = 0.06f)), contentAlignment = Alignment.Center) {
                        Icon(Lucide.Hotel, null, Modifier.size(44.dp), tint = SenjaTeal.copy(alpha = 0.4f))
                    }
                },
                error = {
                    Box(Modifier.fillMaxWidth().height(130.dp).background(SenjaTeal.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Icon(Lucide.Hotel, null, Modifier.size(44.dp), tint = SenjaTeal)
                    }
                },
                success = { SubcomposeAsyncImageContent() },
            )
        } else {
            Box(Modifier.fillMaxWidth().height(130.dp).background(SenjaTeal.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(Lucide.Hotel, null, Modifier.size(44.dp), tint = SenjaTeal)
            }
        }
        Column(Modifier.padding(18.dp)) {
            Text(hotel.name, style = MaterialTheme.typography.titleLarge)
            Text(hotel.address, color = SenjaMist, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(if (hotel.price > 0) "${rupiah(hotel.price)} / malam" else "Harga belum tersedia", fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Icon(Lucide.Star, null, tint = SenjaSand, modifier = Modifier.size(16.dp))
                Text(" ${hotel.rating}")
            }
            Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Buka di Google Maps", color = SenjaTeal, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.weight(1f))
                Icon(Lucide.ExternalLink, null, tint = SenjaTeal, modifier = Modifier.size(16.dp))
            }
            Surface(color = SenjaCanvas, shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(top = 14.dp)) {
                Text(reason, color = SenjaMist, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
            }
        }
    }
}

@Composable
private fun ActivityCard(activity: Activity) {
    val uriHandler = LocalUriHandler.current
    Card(
        onClick = { uriHandler.openUri(mapsUrl(activity.sourceUrl, activity.name, activity.address)) },
        colors = CardDefaults.cardColors(Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFEAEAEA)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (!activity.imageUrl.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = activity.imageUrl,
                    contentDescription = activity.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(82.dp).clip(RoundedCornerShape(8.dp)),
                    loading = {
                        Box(Modifier.size(82.dp).clip(RoundedCornerShape(8.dp)).background(SenjaSand.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                            Icon(Lucide.Compass, null, tint = SenjaSand.copy(alpha = 0.4f))
                        }
                    },
                    error = {
                        Box(Modifier.size(82.dp).clip(RoundedCornerShape(8.dp)).background(SenjaSand.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                            Icon(Lucide.Compass, null, tint = SenjaSand)
                        }
                    },
                    success = { SubcomposeAsyncImageContent() },
                )
            } else {
                Box(Modifier.size(82.dp).clip(RoundedCornerShape(8.dp)).background(SenjaSand.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                    Icon(Lucide.Compass, null, tint = SenjaSand)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(activity.name, fontWeight = FontWeight.ExtraBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(activity.address, color = SenjaMist, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Lucide.Star, null, tint = SenjaSand, modifier = Modifier.size(14.dp))
                    Text(" ${if (activity.rating > 0) activity.rating.toString() else "Belum ada rating"}", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.width(10.dp))
                    Text("Google Maps", color = SenjaTeal, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
            Icon(Lucide.ChevronRight, null, tint = SenjaMist)
        }
    }
}

@Composable
private fun ItineraryCard(day: ItineraryDay) {
    Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Hari ${day.day} - ${formatDate(day.date)}", color = SenjaTeal, fontWeight = FontWeight.ExtraBold)
            day.items.forEach { item ->
                Row(Modifier.padding(top = 12.dp)) {
                    Box(Modifier.width(62.dp), contentAlignment = Alignment.TopStart) {
                        Divider(color = SenjaMist.copy(alpha = 0.3f), modifier = Modifier.padding(start = 44.dp).width(2.dp).height(54.dp))
                        Text(item.time, color = SenjaTeal, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    }
                    Column(Modifier.weight(1f)) {
                        Text(item.title, fontWeight = FontWeight.Bold)
                        Text(item.description, color = SenjaMist, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyPlan(onCreate: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        EmptyState("Belum ada rencana", "Buat perjalanan pertamamu bersama AI.") {
            PrimaryButton("Mulai Rencanakan", onCreate, modifier = Modifier.padding(top = 20.dp))
        }
    }
}

@Composable
private fun Entrance(content: @Composable () -> Unit) {
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 } + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        content()
    }
}
