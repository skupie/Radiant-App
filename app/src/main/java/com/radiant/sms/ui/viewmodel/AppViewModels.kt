package com.radiant.sms.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.radiant.sms.data.Repository
import com.radiant.sms.data.TokenStore
import com.radiant.sms.network.NetworkModule
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val role: String? = null,
    val tokenPresent: Boolean = false
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val tokenStore = TokenStore(app.applicationContext)
    private val api = NetworkModule.createApiService {
        // Note: synchronous read is not ideal; used only for interceptor.
        // We'll keep a cached token in memory.
        cachedToken
    }
    private val repo = Repository(api)

    private var cachedToken: String? = null

    private val _state = MutableStateFlow(AuthState(isLoading = true))
    val state: StateFlow<AuthState> = _state

    init {
        viewModelScope.launch {
            cachedToken = tokenStore.tokenFlow.first()
            val role = tokenStore.roleFlow.first()
            _state.value = AuthState(
                isLoading = false,
                role = role,
                tokenPresent = !cachedToken.isNullOrBlank()
            )
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val res = repo.login(email, password)
                cachedToken = res.token
                tokenStore.saveToken(res.token, res.user.role)
                _state.value = AuthState(isLoading = false, role = res.user.role, tokenPresent = true)
            } catch (e: HttpException) {
                if (e.response()?.headers()?.get("Content-Type")?.contains("text/html") == true) {
                    _state.value = AuthState(isLoading = false, error = "The server returned an unexpected response. This might be due to a security check. Please try again later.")
                } else {
                    _state.value = AuthState(isLoading = false, error = e.message ?: "Login failed")
                }
            } catch (e: JsonDataException) {
                _state.value = AuthState(isLoading = false, error = "Invalid credentials")
            } catch (e: Exception) {
                _state.value = AuthState(isLoading = false, error = e.message ?: "Login failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try { repo.logout() } catch (_: Exception) {}
            cachedToken = null
            tokenStore.clear()
            _state.value = AuthState(isLoading = false, role = null, tokenPresent = false)
        }
    }
}
