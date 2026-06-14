package com.learning.app.data.repository

import com.learning.app.data.remote.api.LearningApiService
import com.learning.app.data.remote.api.RetryPolicy
import com.learning.app.data.remote.api.NetworkException
import com.learning.app.data.remote.dto.*
import com.learning.app.data.local.preferences.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: LearningApiService,
    private val tokenManager: TokenManager,
    private val retryPolicy: RetryPolicy
) {

    fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String
    ): Flow<Result<String>> = flow {
        try {
            val response = retryPolicy.executeWithRetry {
                apiService.register(
                    RegisterRequest(email, password, firstName, lastName)
                )
            }

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.saveTokens(
                    authResponse.accessToken,
                    authResponse.refreshToken,
                    authResponse.user.id,
                    authResponse.expiresIn
                )
                emit(Result.success(authResponse.user.id))
            } else {
                val errorMessage = response.errorBody()?.string() ?: response.message()
                emit(Result.failure(NetworkException.ServerError(response.code(), errorMessage)))
            }
        } catch (e: IOException) {
            emit(Result.failure(NetworkException.ConnectionError(e.message ?: "Connection failed")))
        } catch (e: HttpException) {
            emit(Result.failure(NetworkException.ServerError(e.code(), e.message())))
        } catch (e: Exception) {
            emit(Result.failure(NetworkException.UnknownError(e.message ?: "Unknown error", e)))
        }
    }

    fun login(email: String, password: String): Flow<Result<String>> = flow {
        try {
            val response = retryPolicy.executeWithRetry {
                apiService.login(LoginRequest(email, password))
            }

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                tokenManager.saveTokens(
                    authResponse.accessToken,
                    authResponse.refreshToken,
                    authResponse.user.id,
                    authResponse.expiresIn
                )
                emit(Result.success(authResponse.user.id))
            } else {
                val errorMessage = response.errorBody()?.string() ?: response.message()
                emit(Result.failure(NetworkException.ServerError(response.code(), errorMessage)))
            }
        } catch (e: IOException) {
            emit(Result.failure(NetworkException.ConnectionError("Network timeout or connection error")))
        } catch (e: HttpException) {
            emit(Result.failure(NetworkException.ServerError(e.code(), "Authentication failed")))
        } catch (e: Exception) {
            emit(Result.failure(NetworkException.UnknownError("Authentication error", e)))
        }
    }

    fun logout(): Flow<Result<Unit>> = flow {
        try {
            val response = retryPolicy.executeWithRetry {
                apiService.logout()
            }

            if (response.isSuccessful) {
                tokenManager.clearTokens()
                emit(Result.success(Unit))
            } else {
                emit(Result.failure(NetworkException.ServerError(response.code(), "Logout failed")))
            }
        } catch (e: Exception) {
            // Clear tokens even if API call fails
            tokenManager.clearTokens()
            emit(Result.failure(NetworkException.UnknownError("Logout error", e)))
        }
    }

    fun getAccessToken(): String? = tokenManager.getAccessToken()

    fun getRefreshToken(): String? = tokenManager.getRefreshToken()

    fun getUserId(): String? = tokenManager.getUserId()

    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()
}
