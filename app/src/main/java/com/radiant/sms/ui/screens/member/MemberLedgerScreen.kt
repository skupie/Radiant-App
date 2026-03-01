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
import com.radiant.sms.network.MemberLedgerResponse
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun MemberLedgerScreen(nav: NavController) {
    val context = LocalContext.current
    val api = remember { NetworkModule.api(context) }

    var response by remember { mutableStateOf<MemberLedgerResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    var year by remember { mutableStateOf(LocalDate.now().year) }

    fun load() {
        scope.launch {
            isLoading = true
            error = null
            try {
                response = api.getMemberLedger(year)
            } catch (e: Exception) {
                error = e.message ?: "Failed to load ledger"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(year) { load() }

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

            response?.let { ledger ->
                Text("Year: ${ledger.year}", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))

                Text("Year Total: ${ledger.yearTotal}")
                Text("Lifetime Total: ${ledger.lifetimeTotal}")
                Text("Total Due: ${ledger.dueSummary.total}")

                Spacer(Modifier.height(16.dp))
                Text("Entries:", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                ledger.monthlyData.forEach { month ->
                    Text("${month.label} - Total: ${month.total}", style = MaterialTheme.typography.bodyLarge)
                    month.entries.forEach { entry ->
                        Text("• ${entry.type} - ${entry.totalAmount} (${entry.depositedAtLocal})")
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}
