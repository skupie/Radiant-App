package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.network.NetworkModule
import com.radiant.sms.network.MemberProfileResponse
import kotlinx.coroutines.launch

@Composable
fun MemberProfileScreen(nav: NavController) {
    val context = LocalContext.current
    val api = remember { NetworkModule.api(context) }

    var response by remember { mutableStateOf<MemberProfileResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            isLoading = true
            error = null
            try {
                response = api.getMemberProfile()
            } catch (e: Exception) {
                error = e.message ?: "Failed to load profile"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    ScreenScaffold(nav = nav) {

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@ScreenScaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (error != null) {
                Text(error ?: "", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
            }

            response?.let { profile ->
                Text("Profile", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))

                Text("Name: ${profile.member.displayName}")
                Text("Email: ${profile.member.email}")
                Text("Mobile: ${profile.member.mobileNumber}")
                Text("NID: ${profile.member.displayNid}")
            }
        }
    }
}
