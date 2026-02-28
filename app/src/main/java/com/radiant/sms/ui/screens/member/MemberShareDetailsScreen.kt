package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.network.ApiService
import com.radiant.sms.network.NetworkModule
import com.radiant.sms.network.models.MemberShareDetailsResponse
import kotlinx.coroutines.launch

@Composable
fun MemberShareDetailsScreen(
    nav: NavController,
    api: ApiService = NetworkModule.api
) {
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var data by remember { mutableStateOf<MemberShareDetailsResponse?>(null) }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                data = api.getMemberShareDetails()
            } catch (e: Exception) {
                error = e.message ?: "Failed to load share details"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    ScreenScaffold(
        title = "Share Details",
        nav = nav
    ) {
        // Top actions row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = { load() }, enabled = !loading) {
                Text(if (loading) "Loading..." else "Refresh")
            }
        }

        if (loading) {
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@ScreenScaffold
        }

        if (error != null) {
            Spacer(Modifier.height(12.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Error",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            return@ScreenScaffold
        }

        val member = data?.member
        val share = data?.share
        val nominee = data?.nominee

        // --- Profile / Header Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar placeholder
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (member?.name?.trim()?.take(1) ?: "M").uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = member?.name ?: "Member",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = member?.email ?: "-",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Phone: ${member?.phone ?: "-"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!member?.member_id.isNullOrBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Member ID: ${member?.member_id}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // --- Share Summary Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Share Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(10.dp))

                InfoRow(label = "Share No", value = share?.share_no ?: "-")
                Divider(Modifier.padding(vertical = 8.dp))
                InfoRow(label = "Share Amount", value = share?.share_amount ?: "-")
                Divider(Modifier.padding(vertical = 8.dp))
                InfoRow(label = "Total Deposit", value = share?.total_deposit ?: "-")
                Divider(Modifier.padding(vertical = 8.dp))
                InfoRow(label = "Created At", value = share?.created_at ?: "-")
            }
        }

        Spacer(Modifier.height(14.dp))

        // --- Nominee Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Nominee Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(10.dp))

                InfoRow(label = "Name", value = nominee?.name ?: "-")
                Divider(Modifier.padding(vertical = 8.dp))
                InfoRow(label = "Phone", value = nominee?.phone ?: "-")
                Divider(Modifier.padding(vertical = 8.dp))
                InfoRow(label = "Relation", value = nominee?.relation ?: "-")
                Divider(Modifier.padding(vertical = 8.dp))
                InfoRow(label = "NID", value = nominee?.nid ?: "-")
                if (!nominee?.address.isNullOrBlank()) {
                    Divider(Modifier.padding(vertical = 8.dp))
                    InfoRow(label = "Address", value = nominee?.address ?: "-")
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        // Optional action button (you can remove)
        Button(
            onClick = { /* TODO: navigate to edit/ledger/etc */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("OK")
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
