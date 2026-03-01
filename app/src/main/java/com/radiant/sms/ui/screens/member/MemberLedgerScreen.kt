package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.data.Repository
import com.radiant.sms.network.NetworkModule
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun MemberLedgerScreen(
    navController: NavController,
    memberId: Int
) {
    val context = LocalContext.current
    val api = remember { NetworkModule.api(context) }
    val repo = remember { Repository(api) }

    val scope = rememberCoroutineScope()

    val calendar = remember { Calendar.getInstance() }
    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH) + 1) } // 1..12

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // If you have a real model, replace Any with your entry type:
    var ledgerResponseText by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            ledgerResponseText = null

            try {
                val res = repo.getMemberLedger(memberId, selectedYear, selectedMonth)
                if (res.isSuccessful) {
                    ledgerResponseText = res.body()?.toString()
                } else {
                    error = "Failed: ${res.code()} ${res.message()}"
                }
            } catch (e: Exception) {
                error = e.message ?: "Unknown error"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(memberId, selectedYear, selectedMonth) {
        load()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Member Ledger (ID: $memberId)", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = selectedYear.toString(),
                onValueChange = { it.toIntOrNull()?.let { y -> selectedYear = y } },
                label = { Text("Year") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = selectedMonth.toString(),
                onValueChange = { it.toIntOrNull()?.let { m -> selectedMonth = m.coerceIn(1, 12) } },
                label = { Text("Month") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        Button(onClick = { load() }, enabled = !loading) {
            Text(if (loading) "Loading..." else "Reload")
        }

        Spacer(Modifier.height(12.dp))

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Text(ledgerResponseText ?: "No data yet.")
    }
}
