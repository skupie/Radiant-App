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

    // ✅ IMPORTANT: always use TokenStore-backed API
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

                // ✅ Save token to SharedPreferences
                tokenStore.saveToken(res.token, res.user.role)

                _state.value = AuthState(
                    isLoading = false,
                    role = res.user.role,
                    tokenPresent = true
                )

            } catch (e: HttpException) {
                val code = e.code()
                val errorBody = try {
                    e.response()?.errorBody()?.string()
                } catch (_: Exception) {
                    null
                }

                Log.e("API_HTTP", "Login failed: HTTP $code body=$errorBody", e)

                _state.value = AuthState(
                    isLoading = false,
                    error = buildString {
                        append("HTTP ").append(code)
                        if (!errorBody.isNullOrBlank()) {
                            append("\n\n").append(errorBody)
                        }
                    },
                    tokenPresent = false
                )

            } catch (e: JsonDataException) {
                Log.e("API_HTTP", "JSON parse error", e)

                _state.value = AuthState(
                    isLoading = false,
                    error = "Server returned invalid JSON",
                    tokenPresent = false
                )

            } catch (e: Exception) {
                Log.e("API_HTTP", "Login failed", e)

                _state.value = AuthState(
                    isLoading = false,
                    error = e.message ?: "Login failed",
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
