package com.learning.app.data.remote.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit
import com.learning.app.data.local.preferences.TokenManager

object ApiClient {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Auth Interceptor for adding tokens to requests
    private fun createAuthInterceptor(tokenManager: TokenManager) = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = tokenManager.getAccessToken()

        val requestBuilder = originalRequest.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("User-Agent", "LearningApp/2.0.0")

        if (!token.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        chain.proceed(requestBuilder.build())
    }

    // Token Refresh Interceptor
    private fun createTokenRefreshInterceptor(tokenManager: TokenManager) = Interceptor { chain ->
        val response = chain.proceed(chain.request())

        if (response.code == 401) {
            // Token expired, try to refresh
            val refreshToken = tokenManager.getRefreshToken()
            if (!refreshToken.isNullOrEmpty()) {
                // Attempt refresh logic here
                // If successful, retry original request
            }
        }

        response
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    fun createRetrofit(baseUrl: String, tokenManager: TokenManager): Retrofit {
        val clientWithAuth = httpClient.newBuilder()
            .addInterceptor(createAuthInterceptor(tokenManager))
            .addInterceptor(createTokenRefreshInterceptor(tokenManager))
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(clientWithAuth)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}
