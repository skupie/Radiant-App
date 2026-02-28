package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.data.TokenStore
import com.radiant.sms.network.NetworkModule
import com.radiant.sms.network.MemberShareDetailsResponse
import kotlinx.coroutines.flow.first

@Composable
fun MemberShareDetailsScreen(nav: NavController) {

    val ctx = LocalContext.current
    val tokenStore = remember { TokenStore(ctx) }

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var data by remember { mutableStateOf<MemberShareDetailsResponse?>(null) }

    LaunchedEffect(Unit) {
        try {
            val token = tokenStore.tokenFlow.first()
            val api = NetworkModule.createApiService { token }

            data = api.getMemberShareDetails()
        } catch (e: Exception) {
            error = e.message
        }
        loading = false
    }

    ScreenScaffold(title = "Share Details", nav = nav) {
        when {
            loading -> Text("Loading...")
            error != null -> Text("Error: $error")
            else -> {
                val res = data!!
                val m = res.member

                Text("Name: ${m.full_name ?: "-"}")
                Text("Share: ${m.share ?: 0}")
                Text("Total Deposited: ${res.total_deposited}")
                Text("Total Due: ${res.total_due}")
            }
        }
    }
}
