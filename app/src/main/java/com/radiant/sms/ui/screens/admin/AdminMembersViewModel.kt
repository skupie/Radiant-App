package com.radiant.sms.ui.screens.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.radiant.sms.data.Repository
import com.radiant.sms.network.AdminMemberDto
import com.radiant.sms.network.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AdminMembersState(
    val isLoading: Boolean = false,
    val query: String = "",
    val members: List<AdminMemberDto> = emptyList(),
    val error: String? = null
)

class AdminMembersViewModel(app: Application) : AndroidViewModel(app) {

    private val api = NetworkModule.api(app.applicationContext)
    private val repo = Repository(api)

    private val _state = MutableStateFlow(AdminMembersState(isLoading = true))
    val state: StateFlow<AdminMembersState> = _state

    init {
        loadMembers()
    }

    fun setQuery(q: String) {
        _state.value = _state.value.copy(query = q)
    }

    fun loadMembers() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val members = repo.adminMembersAll(
                    search = _state.value.query.takeIf { it.isNotBlank() }
                )
                _state.value = _state.value.copy(isLoading = false, members = members)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Failed")
            }
        }
    }
}
