package com.oop.traveloop.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oop.traveloop.domain.model.TripPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.planHistoryDataStore by preferencesDataStore(name = "plan_history")

class PlanHistoryStore(private val context: Context) {
    private val gson = Gson()
    private val historyKey = stringPreferencesKey("plans")
    private val historyType = object : TypeToken<List<TripPlan>>() {}.type

    val history: Flow<List<TripPlan>> = context.planHistoryDataStore.data
        .map { prefs ->
            prefs[historyKey]
                ?.let { runCatching { gson.fromJson<List<TripPlan>>(it, historyType) }.getOrNull() }
                .orEmpty()
        }
        .catch { emit(emptyList()) }

    suspend fun save(plans: List<TripPlan>) {
        context.planHistoryDataStore.edit { prefs ->
            prefs[historyKey] = gson.toJson(plans.take(20), historyType)
        }
    }
}
