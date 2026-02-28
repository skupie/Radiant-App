package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.data.TokenStore
import com.radiant.sms.network.NetworkModule
import com.radiant.sms.network.MemberDueSummaryResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun MemberDueSummaryScreen(nav: NavController) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val tokenStore = remember { TokenStore(ctx) }

    var yearText by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var data by remember { mutableStateOf<MemberDueSummaryResponse?>(null) }

    fun load() {
        val year = yearText.trim().toIntOrNull()
        loading = true
        error = null

        scope.launch {
            try {
                val token = tokenStore.tokenFlow.first()
                val api = NetworkModule.createApiService { token }

                val res = api.getMemberDueSummary(year)
                data = res
            } catch (e: Exception) {
                error = e.message
            }
            loading = false
        }
    }

    ScreenScaffold(title = "Due Summary", nav = nav) {
        OutlinedTextField(
            value = yearText,
            onValueChange = { yearText = it },
            label = { Text("Year (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = { load() }, modifier = Modifier.fillMaxWidth()) {
            Text("Load Due Summary")
        }

        Spacer(Modifier.height(10.dp))

        when {
            loading -> Text("Loading...")
            error != null -> Text("Error: $error")
            data == null -> Text("Enter a year and tap Load (or leave blank).")
            else -> {
                val res = data!!
                Text("Selected Year: ${res.selected_year ?: "All"}")
                Text("Total Due: ${res.summary.total}")
                Text("Available Years: ${res.available_years.joinToString()}")

                Spacer(Modifier.height(10.dp))

                Column(Modifier.verticalScroll(rememberScrollState())) {
                    res.summary.months.forEach { m ->
                        Text("Year ${m.year} Month ${m.month} | Base ${m.base_amount} | Amount ${m.amount}")
                    }
                }
            }
        }
    }
}
