package com.oop.traveloop.ui.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.oop.traveloop.data.local.PlanHistoryStore
import com.oop.traveloop.domain.model.TripPlan
import com.oop.traveloop.domain.repository.PlanInput
import com.oop.traveloop.domain.usecase.CreateTripPlanUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class PlannerForm(
    val origin: String = "",
    val destination: String = "",
    val budget: String = "",
    val days: String = "",
    val people: String = "",
    val startDate: String = LocalDate.now().plusDays(7).toString(),
    val endDate: String = LocalDate.now().plusDays(9).toString(),
    val style: String = "BALANCED",
    val mode: String = "CAR",
)

data class PlannerUiState(
    val form: PlannerForm = PlannerForm(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val plan: TripPlan? = null,
    val planHistory: List<TripPlan> = emptyList(),
)

sealed interface PlannerAction {
    data class OriginChanged(val value: String) : PlannerAction
    data class DestinationChanged(val value: String) : PlannerAction
    data class BudgetChanged(val value: String) : PlannerAction
    data class DaysChanged(val value: String) : PlannerAction
    data class PeopleChanged(val value: String) : PlannerAction
    data class DatesChanged(val startDate: String, val endDate: String) : PlannerAction
    data class StyleChanged(val value: String) : PlannerAction
    data class PlanSelected(val planId: String) : PlannerAction
    data object Submit : PlannerAction
    data object ClearError : PlannerAction
}

class PlannerViewModel(
    private val createTripPlan: CreateTripPlanUseCase,
    private val planHistoryStore: PlanHistoryStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlannerUiState())
    val uiState: StateFlow<PlannerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            planHistoryStore.history.collect { history ->
                _uiState.update { state ->
                    state.copy(
                        planHistory = history,
                        plan = state.plan ?: history.firstOrNull(),
                    )
                }
            }
        }
    }

    fun onAction(action: PlannerAction) {
        when (action) {
            is PlannerAction.OriginChanged -> updateForm { copy(origin = action.value) }
            is PlannerAction.DestinationChanged -> updateForm { copy(destination = action.value) }
            is PlannerAction.BudgetChanged -> updateForm { copy(budget = action.value.filter(Char::isDigit)) }
            is PlannerAction.DaysChanged -> updateForm { copy(days = action.value.filter(Char::isDigit)) }
            is PlannerAction.PeopleChanged -> updateForm { copy(people = action.value.filter(Char::isDigit)) }
            is PlannerAction.DatesChanged -> updateForm {
                val start = LocalDate.parse(action.startDate)
                val end = LocalDate.parse(action.endDate)
                copy(startDate = action.startDate, endDate = action.endDate,
                    days = (java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1).coerceAtLeast(1).toString())
            }
            is PlannerAction.StyleChanged -> updateForm { copy(style = action.value) }
            is PlannerAction.PlanSelected -> _uiState.update { state ->
                state.planHistory.firstOrNull { it.id == action.planId }
                    ?.let { state.copy(plan = it, error = null) }
                    ?: state
            }
            PlannerAction.Submit -> submit()
            PlannerAction.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun updateForm(transform: PlannerForm.() -> PlannerForm) =
        _uiState.update { it.copy(form = it.form.transform(), error = null) }

    private fun submit() = viewModelScope.launch {
        if (_uiState.value.isLoading) return@launch
        val form = _uiState.value.form
        val start = LocalDate.parse(form.startDate)
        val end = LocalDate.parse(form.endDate)
        val days = (java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1).toInt()
        val people = form.people.toIntOrNull() ?: 0
        val budget = form.budget.toLongOrNull() ?: 0
        if (form.origin.isBlank() || form.destination.isBlank() || days <= 0 || people <= 0 || budget <= 0) {
            _uiState.update { it.copy(error = "Lengkapi asal, tujuan, tanggal, orang, dan budget IDR.") }
            return@launch
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        runCatching {
            createTripPlan(
                PlanInput(
                    form.origin, form.destination, start.toString(), end.toString(),
                    days, people, budget, form.style, form.mode, emptyList(),
                )
            ).getOrThrow()
        }.onSuccess { plan ->
            var updatedHistory: List<TripPlan> = emptyList()
            _uiState.update { state ->
                updatedHistory = (listOf(plan) + state.planHistory.filterNot { it.id == plan.id }).take(20)
                state.copy(isLoading = false, plan = plan, planHistory = updatedHistory)
            }
            viewModelScope.launch { planHistoryStore.save(updatedHistory) }
        }
            .onFailure { error ->
                val message = when {
                    error.message?.contains("Failed to connect", true) == true -> "Backend tidak terhubung. Pastikan server port 8080 aktif."
                    else -> error.message ?: "Gagal membuat rencana perjalanan"
                }
                _uiState.update { it.copy(isLoading = false, error = message) }
            }
    }

    companion object {
        fun factory(useCase: CreateTripPlanUseCase, planHistoryStore: PlanHistoryStore) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = PlannerViewModel(useCase, planHistoryStore) as T
        }
    }
}
