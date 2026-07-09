package com.oop.traveloop.data.remote

data class RegisterApiRequest(val email: String, val password: String, val name: String)
data class LoginApiRequest(val email: String, val password: String)
data class RefreshApiRequest(val refreshToken: String)
data class AuthApiResponse(val accessToken: String, val refreshToken: String)
data class MeApiResponse(val id: String, val email: String, val name: String)
