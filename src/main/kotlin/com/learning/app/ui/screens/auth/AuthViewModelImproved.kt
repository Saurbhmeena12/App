package com.learning.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learning.app.data.remote.api.NetworkException
import com.learning.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModelImproved @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthUiStateImproved>(AuthUiStateImproved.Idle)
    val authState: StateFlow<AuthUiStateImproved> = _authState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthUiStateImproved.Loading
            authRepository.login(email, password).collect { result ->
                result.onSuccess { userId ->
                    _authState.value = AuthUiStateImproved.Success(userId)
                }
                result.onFailure { exception ->
                    val errorMessage = when (exception) {
                        is NetworkException.ConnectionError -> "Network connection failed. Check your internet."
                        is NetworkException.Timeout -> "Request timeout. Please try again."
                        is NetworkException.ServerError -> "Server error: ${exception.code}"
                        else -> exception.message ?: "Login failed"
                    }
                    _authState.value = AuthUiStateImproved.Error(errorMessage)
                }
            }
        }
    }

    fun register(email: String, password: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            _authState.value = AuthUiStateImproved.Loading
            authRepository.register(email, password, firstName, lastName).collect { result ->
                result.onSuccess { userId ->
                    _authState.value = AuthUiStateImproved.Success(userId)
                }
                result.onFailure { exception ->
                    val errorMessage = when (exception) {
                        is NetworkException.ConnectionError -> "Network connection failed."
                        is NetworkException.Timeout -> "Request timeout."
                        is NetworkException.ServerError -> "Registration failed: ${exception.code}"
                        else -> "Registration failed"
                    }
                    _authState.value = AuthUiStateImproved.Error(errorMessage)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout().collect { result ->
                result.onSuccess {
                    _authState.value = AuthUiStateImproved.Idle
                }
                result.onFailure { exception ->
                    // Still consider logout successful for local state
                    _authState.value = AuthUiStateImproved.Idle
                }
            }
        }
    }
}
