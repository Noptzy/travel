package com.oop.traveloop.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oop.traveloop.domain.model.TripPlan
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.util.Locale

private val Context.planHistoryDataStore by preferencesDataStore(name = "plan_history")

class PlanHistoryStore(private val context: Context) {
    private val gson = Gson()
    private val legacyHistoryKey = stringPreferencesKey("plans")
    private val historyType = object : TypeToken<List<TripPlan>>() {}.type
    private val ownerKey = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val history: Flow<List<TripPlan>> = ownerKey
        .flatMapLatest { owner ->
            if (owner == null) {
                flowOf(emptyList())
            } else {
                context.planHistoryDataStore.data.map { prefs ->
                    prefs[historyKey(owner)]
                        ?.let { runCatching { gson.fromJson<List<TripPlan>>(it, historyType) }.getOrNull() }
                        .orEmpty()
                }
            }
        }
        .catch { emit(emptyList()) }

    fun setOwner(userKey: String?) {
        ownerKey.value = userKey
            ?.trim()
            ?.lowercase(Locale.ROOT)
            ?.takeIf { it.isNotBlank() }
            ?.let(::hash)
    }

    suspend fun save(plans: List<TripPlan>) {
        val owner = ownerKey.value ?: return
        context.planHistoryDataStore.edit { prefs ->
            prefs[historyKey(owner)] = gson.toJson(plans.take(20), historyType)
            prefs.remove(legacyHistoryKey)
        }
    }

    private fun historyKey(owner: String) = stringPreferencesKey("plans_$owner")

    private fun hash(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
