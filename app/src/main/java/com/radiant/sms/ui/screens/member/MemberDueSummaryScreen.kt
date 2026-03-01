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
fun MemberDueSummaryScreen(
    navController: NavController,
    memberId: Int
) {
    val context = LocalContext.current
    val api = remember { NetworkModule.api(context) }
    val repo = remember { Repository(api) }

    val scope = rememberCoroutineScope()

    val calendar = remember { Calendar.getInstance() }
    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var dueSummaryText by remember { mutableStateOf<String?>(null) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            dueSummaryText = null

            try {
                val res = repo.getMemberDueSummary(memberId, selectedYear)
                if (res.isSuccessful) {
                    dueSummaryText = res.body()?.toString()
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

    LaunchedEffect(memberId, selectedYear) {
        load()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Member Due Summary (ID: $memberId)", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = selectedYear.toString(),
            onValueChange = { it.toIntOrNull()?.let { y -> selectedYear = y } },
            label = { Text("Year") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Button(onClick = { load() }, enabled = !loading) {
            Text(if (loading) "Loading..." else "Reload")
        }

        Spacer(Modifier.height(12.dp))

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Text(dueSummaryText ?: "No data yet.")
    }
}
