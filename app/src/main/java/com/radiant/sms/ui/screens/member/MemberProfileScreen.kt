package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.radiant.sms.data.TokenStore
import com.radiant.sms.network.NetworkModule
import com.radiant.sms.network.models.MemberProfileResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun MemberProfileScreen(nav: NavController) {

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokenStore = remember { TokenStore(ctx) }

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var data by remember { mutableStateOf<MemberProfileResponse?>(null) }

    LaunchedEffect(Unit) {
        try {
            val token = tokenStore.tokenFlow.first()

            val api = NetworkModule.createApiService { token }

            data = api.getMemberProfile()
        } catch (e: Exception) {
            error = e.message
        }
        loading = false
    }

    ScreenScaffold(title = "Profile", nav = nav) {
        when {
            loading -> Text("Loading...")
            error != null -> Text("Error: $error")
            else -> {
                val m = data!!.member
                Text("Name: ${m.full_name ?: "-"}")
                Text("Mobile: ${m.mobile_number ?: "-"}")
                Text("Email: ${m.email ?: "-"}")
                Text("NID: ${m.nid ?: "-"}")
                Text("Share: ${m.share ?: 0}")
                Text("Due Total: ${m.due_total ?: 0.0}")
                Text("Total Deposited: ${m.total_deposited ?: 0.0}")
            }
        }
    }
}
