// app/src/main/java/com/radiant/sms/data/TokenStore.kt
package com.radiant.sms.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Simple SharedPreferences-based token store + in-memory StateFlows.
 * This matches what AuthViewModel expects:
 * - tokenFlow, roleFlow
 * - saveToken(token, role)
 * - clear()
 *
 * NOTE: For the OkHttp interceptor we also expose getTokenSync().
 */
class TokenStore(context: Context) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _tokenFlow = MutableStateFlow(prefs.getString(KEY_TOKEN, null))
    val tokenFlow: StateFlow<String?> = _tokenFlow

    private val _roleFlow = MutableStateFlow(prefs.getString(KEY_ROLE, null))
    val roleFlow: StateFlow<String?> = _roleFlow

    fun saveToken(token: String, role: String?) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_ROLE, role)
            .apply()

        _tokenFlow.value = token
        _roleFlow.value = role
    }

    fun getTokenSync(): String? = prefs.getString(KEY_TOKEN, null)

    fun clear() {
        prefs.edit().clear().apply()
        _tokenFlow.value = null
        _roleFlow.value = null
    }

    private companion object {
        const val KEY_TOKEN = "token"
        const val KEY_ROLE = "role"
    }
}
