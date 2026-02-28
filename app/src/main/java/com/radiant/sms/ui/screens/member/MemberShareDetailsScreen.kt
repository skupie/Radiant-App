package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.data.Repository
import com.radiant.sms.network.MemberShareDetailsResponse
import com.radiant.sms.network.NetworkModule
import kotlinx.coroutines.launch

@Composable
fun MemberShareDetailsScreen(nav: NavController) {

    val context = LocalContext.current
    val repo = remember { Repository(NetworkModule.api(context)) }
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var data by remember { mutableStateOf<MemberShareDetailsResponse?>(null) }

    fun load() {
        scope.launch {
            try {
                loading = true
                error = null
                data = repo.memberShareDetails()
            } catch (e: Exception) {
                error = e.message ?: "Failed to load share details"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        load()
    }

    ScreenScaffold(title = "Share Details", nav = nav) {

        if (loading) {
            CircularProgressIndicator()
            return@ScreenScaffold
        }

        if (error != null) {
            Text("Error: ${error!!}", color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(12.dp))
            Button(onClick = { load() }) { Text("Retry") }
            return@ScreenScaffold
        }

        val member = data?.member
        val share = data?.share
        val nominee = data?.nominee

        Button(
            onClick = { load() },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Refresh") }

        Spacer(Modifier.height(12.dp))

        InfoCard(title = "Member Information") {
            InfoRow("Name", member?.name)
            InfoRow("Email", member?.email)
            InfoRow("Phone", member?.phone)
            InfoRow("Member ID", member?.member_id)
        }

        InfoCard(title = "Share Information") {
            InfoRow("Share No", share?.share_no)
            InfoRow("Share Amount", share?.share_amount)
            InfoRow("Total Deposit", share?.total_deposit)
            InfoRow("Created At", share?.created_at)
        }

        InfoCard(title = "Nominee Information") {
            InfoRow("Name", nominee?.name)
            InfoRow("Phone", nominee?.phone)
            InfoRow("Relation", nominee?.relation)
            InfoRow("NID", nominee?.nid)
            InfoRow("Address", nominee?.address)
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Divider()
            content()
        }
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun InfoRow(label: String, value: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(value ?: "-")
    }
}
