package com.radiant.sms.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.network.NetworkModule

@Composable
fun AdminFinancialOverviewScreen(nav: NavController) {

    val context = androidx.compose.ui.platform.LocalContext.current
    val api = remember { NetworkModule.api(context) }

    var totalMembers by remember { mutableStateOf(0) }
    var totalDeposits by remember { mutableStateOf(0.0) }
    var totalDue by remember { mutableStateOf(0.0) }

    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val dashboard = api.getAdminDashboard()
            totalMembers = dashboard.total_members
            totalDeposits = dashboard.total_deposits
            totalDue = dashboard.total_due
        } catch (_: Exception) {
        }
        loading = false
    }

    AdminScaffold(
        nav = nav,
        title = "Financial Overview",
        hideTitle = false
    ) {

        if (loading) {
            CircularProgressIndicator()
            return@AdminScaffold
        }

        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            FinancialCard("Total Members", totalMembers.toString())
            FinancialCard("Total Deposits", "৳$totalDeposits")
            FinancialCard("Total Due", "৳$totalDue")

        }
    }
}

@Composable
private fun FinancialCard(title: String, value: String) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(title, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall)

        }
    }
}
