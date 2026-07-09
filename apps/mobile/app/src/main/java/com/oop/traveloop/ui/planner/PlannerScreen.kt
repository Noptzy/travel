package com.oop.traveloop.ui.planner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Banknote
import com.composables.icons.lucide.CircleAlert
import com.composables.icons.lucide.LocateFixed
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Plane
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.Users
import com.oop.traveloop.ui.common.IdrVisualTransformation
import com.oop.traveloop.ui.common.formatDate
import com.oop.traveloop.ui.components.LoadingOverlay
import com.oop.traveloop.ui.components.PrimaryButton
import com.oop.traveloop.ui.components.SectionTitle
import com.oop.traveloop.ui.theme.SenjaInk
import com.oop.traveloop.ui.theme.SenjaMist
import com.oop.traveloop.ui.theme.SenjaSand
import com.oop.traveloop.ui.theme.SenjaTeal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun PlannerScreen(state: PlannerUiState, onAction: (PlannerAction) -> Unit, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Lucide.ArrowLeft, "Kembali", tint = SenjaInk) }
                    Column {
                        Text("AI Travel Planner", style = MaterialTheme.typography.headlineMedium, color = SenjaTeal)
                        Text("Ceritakan detail perjalananmu", color = SenjaMist)
                    }
                }
            }
            item { SectionTitle("Detail Perjalanan", Lucide.Plane) }
            item { PlannerField("Kota asal", state.form.origin, Lucide.LocateFixed) { onAction(PlannerAction.OriginChanged(it)) } }
            item { PlannerField("Tujuan", state.form.destination, Lucide.MapPin) { onAction(PlannerAction.DestinationChanged(it)) } }
            item { DateRangeFields(state.form.startDate, state.form.endDate) { start, end -> onAction(PlannerAction.DatesChanged(start, end)) } }
            item { SectionTitle("Peserta & Budget", Lucide.Users) }
            item { PlannerField("Orang", state.form.people, Lucide.Users, true) { onAction(PlannerAction.PeopleChanged(it)) } }
            item { PlannerField("Total budget (IDR)", state.form.budget, Lucide.Banknote, true, IdrVisualTransformation) { onAction(PlannerAction.BudgetChanged(it)) } }
            item { StyleSection(state.form.style) { onAction(PlannerAction.StyleChanged(it)) } }
            item {
                AnimatedVisibility(
                    visible = state.error != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    AssistChip(
                        onClick = { onAction(PlannerAction.ClearError) },
                        label = { Text(state.error.orEmpty()) },
                        leadingIcon = { Icon(Lucide.CircleAlert, null, tint = MaterialTheme.colorScheme.error) },
                    )
                }
            }
            item { PrimaryButton("Buat Rencana Saya", { onAction(PlannerAction.Submit) }, loading = state.isLoading) }
        }
        AnimatedVisibility(
            visible = state.isLoading,
            enter = fadeIn(tween(200, easing = FastOutSlowInEasing)),
            exit = fadeOut(tween(200)),
        ) {
            LoadingOverlay()
        }
    }
}

@Composable
private fun StyleSection(selected: String, onSelected: (String) -> Unit) {
    Column {
        SectionTitle("Gaya Liburan", Lucide.Sparkles)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(top = 10.dp)) {
            items(listOf("HEMAT", "BALANCED", "NYAMAN")) { style ->
                FilterChip(
                    selected = selected == style,
                    onClick = { onSelected(style) },
                    label = { Text(style.lowercase().replaceFirstChar(Char::uppercase)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SenjaSand,
                        selectedLabelColor = SenjaInk,
                    ),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeFields(startDate: String, endDate: String, onDatesChanged: (String, String) -> Unit) {
    var pickingStart by remember { mutableStateOf(false) }
    var pickingEnd by remember { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(true to startDate, false to endDate).forEach { (isStart, value) ->
            OutlinedCard(onClick = { if (isStart) pickingStart = true else pickingEnd = true }, modifier = Modifier.weight(1f)) {
                Column(Modifier.padding(12.dp)) {
                    Text(if (isStart) "Berangkat" else "Pulang", color = SenjaMist, style = MaterialTheme.typography.labelMedium)
                    Text(formatDate(value), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
    if (pickingStart || pickingEnd) {
        val current = if (pickingStart) startDate else endDate
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = LocalDate.parse(current).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { pickingStart = false; pickingEnd = false },
            confirmButton = {
                TextButton(onClick = {
                    val picked = pickerState.selectedDateMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
                    if (picked != null) {
                        val start = if (pickingStart) picked else LocalDate.parse(startDate)
                        val end = if (pickingEnd) picked else LocalDate.parse(endDate)
                        onDatesChanged(start.toString(), maxOf(start, end).toString())
                    }
                    pickingStart = false
                    pickingEnd = false
                }) { Text("Pilih") }
            },
            dismissButton = { TextButton(onClick = { pickingStart = false; pickingEnd = false }) { Text("Batal") } },
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
private fun PlannerField(
    label: String,
    value: String,
    icon: ImageVector,
    numeric: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = SenjaTeal) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = if (numeric) KeyboardType.Number else KeyboardType.Text),
        visualTransformation = visualTransformation,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = SenjaTeal,
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White,
        ),
    )
}
