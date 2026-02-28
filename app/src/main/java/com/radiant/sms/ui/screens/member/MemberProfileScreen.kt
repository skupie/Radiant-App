package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.network.ApiClient
import kotlinx.coroutines.launch

@Composable
fun MemberProfileScreen(nav: NavController) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Keep it generic so it compiles even if response shape changes
    var raw by remember { mutableStateOf("No data") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                loading = true
                error = null
                val res = ApiClient.api.getMemberProfile()
                raw = res.toString()
            } catch (e: Exception) {
                error = e.message ?: "Failed to load profile"
            } finally {
                loading = false
            }
        }
    }

    ScreenScaffold(title = "Profile", nav = nav) {
        when {
            loading -> CircularProgressIndicator()
            error != null -> Text("Error: ${error!!}", color = MaterialTheme.colorScheme.error)
            else -> Text(raw)
        }
    }
}
