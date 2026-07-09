package com.oop.traveloop.data.remote

import com.oop.traveloop.data.local.TokenStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenRefreshAuthenticator(
    private val tokenStore: TokenStore,
    private val authApi: AuthApi,
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null
        val refreshToken = runBlocking { tokenStore.session.first()?.refreshToken } ?: return null
        val newSession = runCatching {
            runBlocking { authApi.refresh(RefreshApiRequest(refreshToken)) }
        }.getOrElse {
            runBlocking { tokenStore.clear() }
            return null
        }
        runBlocking { tokenStore.save(newSession.accessToken, newSession.refreshToken) }
        return response.request.newBuilder()
            .header("Authorization", "Bearer ${newSession.accessToken}")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
