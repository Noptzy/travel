package com.oop.traveloop

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.oop.traveloop.data.local.PlanHistoryStore
import com.oop.traveloop.data.local.TokenStore
import com.oop.traveloop.data.remote.AuthApi
import com.oop.traveloop.data.remote.TokenRefreshAuthenticator
import com.oop.traveloop.data.remote.TravelApi
import com.oop.traveloop.data.repository.AuthRepositoryImpl
import com.oop.traveloop.data.repository.TravelRepositoryImpl
import com.oop.traveloop.domain.usecase.CreateTripPlanUseCase
import com.oop.traveloop.domain.usecase.LoginUseCase
import com.oop.traveloop.domain.usecase.LogoutUseCase
import com.oop.traveloop.domain.usecase.ObserveAuthSessionUseCase
import com.oop.traveloop.domain.usecase.ObserveUserProfileUseCase
import com.oop.traveloop.domain.usecase.RefreshUserProfileUseCase
import com.oop.traveloop.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okio.Path.Companion.toOkioPath
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {
    private val tokenStore = TokenStore(context)
    val planHistoryStore = PlanHistoryStore(context)

    private val authClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    private val authApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(authClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AuthApi::class.java)

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .callTimeout(320, TimeUnit.SECONDS)
        .cache(Cache(File(context.cacheDir, "http_cache"), 10L * 1024 * 1024))
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .addInterceptor { chain ->
            val token = runBlocking { tokenStore.session.first()?.accessToken }
            val request = if (token != null) {
                chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }
        .authenticator(TokenRefreshAuthenticator(tokenStore, authApi))
        .build()

    private val api = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TravelApi::class.java)

    val imageLoader = ImageLoader.Builder(context)
        .components { add(OkHttpNetworkFetcherFactory(callFactory = { authClient })) }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("image_cache").toOkioPath())
                .maxSizeBytes(50L * 1024 * 1024)
                .build()
        }
        .build()

    private val authRepository = AuthRepositoryImpl(authApi, tokenStore)

    val createTripPlan = CreateTripPlanUseCase(TravelRepositoryImpl(api))
    val login = LoginUseCase(authRepository)
    val register = RegisterUseCase(authRepository)
    val logout = LogoutUseCase(authRepository)
    val observeAuthSession = ObserveAuthSessionUseCase(authRepository)
    val observeUserProfile = ObserveUserProfileUseCase(authRepository)
    val refreshUserProfile = RefreshUserProfileUseCase(authRepository)
}
