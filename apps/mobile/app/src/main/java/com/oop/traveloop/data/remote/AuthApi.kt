package com.oop.traveloop.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterApiRequest): AuthApiResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginApiRequest): AuthApiResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshApiRequest): AuthApiResponse

    @POST("auth/logout")
    suspend fun logout(@Body request: RefreshApiRequest)

    @GET("auth/me")
    suspend fun me(@Header("Authorization") authorization: String): MeApiResponse
}
