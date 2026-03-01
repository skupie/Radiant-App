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

data class AppState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val role: String? = null,
    val tokenPresent: Boolean = false
)

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val tokenStore = TokenStore(app.applicationContext)

    private val api = NetworkModule.createApiService(context)

    private val repo = Repository(api)

    private val _state = MutableStateFlow(AppState(isLoading = true))
    val state: StateFlow<AppState> = _state

    init {
        viewModelScope.launch {
            val token = tokenStore.tokenFlow.first()
            val role = tokenStore.roleFlow.first()
            _state.value = AppState(
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

                _state.value = AppState(
                    isLoading = false,
                    role = res.user.role,
                    tokenPresent = true
                )

            } catch (e: HttpException) {

                val code = e.code()

                val errorBody = try {
                    e.response()?.errorBody()?.string()
                } catch (ex: Exception) {
                    "Unable to read error body"
                }

                _state.value = AppState(
                    isLoading = false,
                    error = "HTTP $code\n\n$errorBody",
                    tokenPresent = false
                )

            } catch (e: JsonDataException) {
                _state.value = AppState(
                    isLoading = false,
                    error = "JSON Parse Error:\n${e.message}",
                    tokenPresent = false
                )
            } catch (e: Exception) {
                _state.value = AppState(
                    isLoading = false,
                    error = "Error:\n${e.message}",
                    tokenPresent = false
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try { repo.logout() } catch (_: Exception) {}
            tokenStore.clear()
            _state.value = AppState(
                isLoading = false,
                role = null,
                tokenPresent = false
            )
        }
    }
}
