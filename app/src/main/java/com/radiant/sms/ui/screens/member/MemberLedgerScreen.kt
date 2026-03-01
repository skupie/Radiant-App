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
import com.radiant.sms.network.MemberDueSummaryResponse
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun MemberDueSummaryScreen(nav: NavController) {
    val context = LocalContext.current
    val api = remember { NetworkModule.api(context) }

    var response by remember { mutableStateOf<MemberDueSummaryResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val year = remember { LocalDate.now().year }

    fun load() {
        scope.launch {
            isLoading = true
            error = null
            try {
                response = api.getMemberDueSummary(year)
            } catch (e: Exception) {
                error = e.message ?: "Failed to load due summary"
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

            response?.let { due ->
                Text("Year: $year", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))

                Text("Total Due: ${due.summary.total}", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))

                Text("Monthly Due:", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                due.summary.months.forEach { m ->
                    Text("• ${m.year}-${m.month}: ${m.amount}")
                }
            }
        }
    }
}
