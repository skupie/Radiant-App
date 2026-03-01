package com.radiant.sms.ui.screens.member

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.radiant.sms.data.Repository
import com.radiant.sms.network.NetworkModule
import kotlinx.coroutines.launch

@Composable
fun MemberProfileScreen(nav: NavController) {
    val context = LocalContext.current

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var raw by remember { mutableStateOf("No data") }

    val api = remember { NetworkModule.api(context) }
    val repo = remember { Repository(api) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                loading = true
                error = null
                val res = repo.memberProfile()
                raw = res.toString()
            } catch (e: Exception) {
                error = e.message ?: "Failed to load profile"
            } finally {
                loading = false
            }
        }
    }

    ScreenScaffold(
        nav = nav,
        title = "",
        hideTitle = true,
        showHamburger = true,
        showBack = false
    ) {
        when {
            loading -> CircularProgressIndicator()
            error != null -> Text("Error: ${error!!}", color = MaterialTheme.colorScheme.error)
            else -> Text(raw)
        }
    }
}
