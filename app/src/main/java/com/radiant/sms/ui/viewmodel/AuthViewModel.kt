package com.radiant.sms.ui.viewmodel

import android.app.Application
import android.util.Log
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
    private val api = NetworkModule.api(app.applicationContext)
    private val repo = Repository(api)

    private val _state = MutableStateFlow(AuthState(isLoading = true))
    val state: StateFlow<AuthState> = _state

    init {
        viewModelScope.launch {
            val token = tokenStore.tokenFlow.first()
            val role = tokenStore.roleFlow.first()

            _state.value = AuthState(
                isLoading = false,
                role = role,
                tokenPresent = !token.isNullOrBlank()
            )
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                val res = repo.login(email, password)

                tokenStore.saveToken(res.token, res.user.role)

                _state.value = AuthState(
                    isLoading = false,
                    role = res.user.role,
                    tokenPresent = true
                )

            } catch (e: HttpException) {

                val code = e.code()
                Log.e("API_HTTP", "Login failed: HTTP $code", e)

                // ✅ Only show simple message for invalid login
                if (code == 401 || code == 422) {
                    _state.value = AuthState(
                        isLoading = false,
                        error = "Wrong Credentials",
                        tokenPresent = false
                    )
                } else {
                    _state.value = AuthState(
                        isLoading = false,
                        error = "Login failed. Please try again.",
                        tokenPresent = false
                    )
                }

            } catch (e: JsonDataException) {

                _state.value = AuthState(
                    isLoading = false,
                    error = "Server error. Please try again.",
                    tokenPresent = false
                )

            } catch (e: Exception) {

                _state.value = AuthState(
                    isLoading = false,
                    error = "Something went wrong",
                    tokenPresent = false
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try { repo.logout() } catch (_: Exception) {}
            tokenStore.clear()
            _state.value = AuthState(isLoading = false, role = null, tokenPresent = false)
        }
    }
}
