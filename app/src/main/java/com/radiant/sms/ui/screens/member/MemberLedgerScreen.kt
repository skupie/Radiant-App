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
import com.radiant.sms.network.MemberProfileResponse
import com.radiant.sms.network.NetworkModule
import kotlinx.coroutines.launch

@Composable
fun MemberLedgerScreen(nav: NavController) {
    val context = LocalContext.current
    val api = remember { NetworkModule.api(context) }
    val scope = rememberCoroutineScope()

    var response by remember { mutableStateOf<MemberProfileResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            isLoading = true
            error = null
            try {
                response = api.getMemberProfile()
            } catch (e: Exception) {
                error = e.message ?: "Failed to load member data"
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (error != null) {
                Text(error ?: "", color = MaterialTheme.colorScheme.error)
            }

            val member = response?.member
            Text("Ledger", style = MaterialTheme.typography.titleLarge)

            Text("Name: ${member?.fullName ?: "-"}")
            Text("Email: ${member?.email ?: "-"}")
            Text("Mobile: ${member?.mobileNumber ?: "-"}")
            Text("NID: ${member?.nid ?: "-"}")

            Spacer(Modifier.height(8.dp))
            Text("Total Deposited: ${response?.totalDeposited ?: 0.0}")
            Text("Total Due: ${response?.totalDue ?: 0.0}")
        }
    }
}
