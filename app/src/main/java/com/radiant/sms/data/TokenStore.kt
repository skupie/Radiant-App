// app/src/main/java/com/radiant/sms/data/TokenStore.kt
package com.radiant.sms.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * SharedPreferences-based token store + in-memory StateFlows.
 *
 * IMPORTANT:
 * We use commit() (sync) instead of apply() (async) so that
 * screens that immediately call APIs after login don't read null token and get 401.
 */
class TokenStore(context: Context) {

    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private val _tokenFlow = MutableStateFlow(prefs.getString(KEY_TOKEN, null))
    val tokenFlow: StateFlow<String?> = _tokenFlow

    private val _roleFlow = MutableStateFlow(prefs.getString(KEY_ROLE, null))
    val roleFlow: StateFlow<String?> = _roleFlow

    fun saveToken(token: String, role: String?) {
        // commit() is synchronous (prevents race -> null token -> 401)
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_ROLE, role)
            .commit()

        _tokenFlow.value = token
        _roleFlow.value = role
    }

    fun getTokenSync(): String? = prefs.getString(KEY_TOKEN, null)

    fun clear() {
        prefs.edit().clear().commit()
        _tokenFlow.value = null
        _roleFlow.value = null
    }

    private companion object {
        const val KEY_TOKEN = "token"
        const val KEY_ROLE = "role"
    }
}
