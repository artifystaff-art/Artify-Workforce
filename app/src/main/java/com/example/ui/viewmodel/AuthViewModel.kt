package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.UserEntity
import com.example.data.repository.WorkforceRepository
import com.example.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val currentUser: UserEntity? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel(private val repository: WorkforceRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun login(email: String, passwordAttempt: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.login(email, passwordAttempt)
            result.onSuccess { user ->
                _uiState.value = _uiState.value.copy(
                    currentUser = user,
                    isLoading = false,
                    errorMessage = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Authentication failed."
                )
            }
        }
    }

    fun quickLoginAs(role: UserRole) {
        val email = when (role) {
            UserRole.WORKER -> "worker@artify.demo"
            UserRole.STAFF -> "staff@artify.demo"
            UserRole.SUPERVISOR -> "supervisor@artify.demo"
        }
        login(email, "password123")
    }

    fun quickLoginAsEmail(email: String) {
        login(email, "password123")
    }

    fun register(
        fullName: String,
        email: String,
        phone: String,
        role: UserRole,
        assignedProjectId: String,
        department: String,
        password: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.register(
                fullName = fullName,
                email = email,
                phone = phone,
                role = role,
                assignedProjectId = assignedProjectId,
                department = department,
                password = password
            )
            result.onSuccess { user ->
                _uiState.value = _uiState.value.copy(
                    currentUser = user,
                    isLoading = false,
                    successMessage = "Account registered successfully! Employee ID: ${user.employeeId}"
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Registration failed."
                )
            }
        }
    }

    fun logout() {
        _uiState.value = AuthUiState()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
