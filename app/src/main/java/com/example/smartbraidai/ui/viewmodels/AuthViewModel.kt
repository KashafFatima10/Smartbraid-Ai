package com.example.smartbraidai.ui.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartbraidai.data.models.User
import com.example.smartbraidai.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _userState = mutableStateOf<UserState>(UserState.Idle)
    val userState: State<UserState> = _userState

    init {
        checkUserStatus()
    }

    private fun checkUserStatus() {
        val firebaseUser = repository.getCurrentUser()
        if (firebaseUser != null) {
            _userState.value = UserState.Loading
            viewModelScope.launch {
                val result = repository.getUserData(firebaseUser.uid)
                result.onSuccess { user ->
                    _userState.value = UserState.Authenticated(user)
                }.onFailure {
                    _userState.value = UserState.Idle
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            val result = repository.login(email, password)
            result.onSuccess { user ->
                _userState.value = UserState.Authenticated(user)
            }.onFailure { error ->
                _userState.value = UserState.Error(error.message ?: "Login Failed")
            }
        }
    }

    fun signup(name: String, email: String, password: String, role: String) {
        viewModelScope.launch {
            _userState.value = UserState.Loading
            val result = repository.signup(name, email, password, role)
            result.onSuccess { user ->
                _userState.value = UserState.Authenticated(user)
            }.onFailure { error ->
                _userState.value = UserState.Error(error.message ?: "Signup Failed")
            }
        }
    }

    fun logout() {
        repository.logout()
        _userState.value = UserState.Idle
    }
}

sealed class UserState {
    object Idle : UserState()
    object Loading : UserState()
    data class Authenticated(val user: User) : UserState()
    data class Error(val message: String) : UserState()
}
