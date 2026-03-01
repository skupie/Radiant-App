package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.data.Repository
import com.radiant.sms.network.MemberLedgerResponse
import com.radiant.sms.network.NetworkModule
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun MemberLedgerScreen(navController: NavController) {
    val context = LocalContext.current
    val api = remember { NetworkModule.api(context) }
    val repo = remember { Repository(api) }

    val scope = rememberCoroutineScope()

    val calendar = remember { Calendar.getInstance() }
    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var data by remember { mutableStateOf<MemberLedgerResponse?>(null) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                data = repo.memberLedger(year = selectedYear)
            } catch (e: Exception) {
                error = e.message ?: "Unknown error"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(selectedYear) { load() }

    ScreenScaffold(title = "Ledger", nav = navController) {

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            OutlinedTextField(
                value = selectedYear.toString(),
                onValueChange = { it.toIntOrNull()?.let { y -> selectedYear = y } },
                label = { Text("Year") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = { load() }, enabled = !loading) {
                Text(if (loading) "Loading..." else "Reload")
            }

            if (loading) {
                CircularProgressIndicator()
                return@Column
            }

            if (error != null) {
                Text("Error: ${error!!}", color = MaterialTheme.colorScheme.error)
                return@Column
            }

            Text(data?.toString() ?: "No data yet.")
        }
    }
}
