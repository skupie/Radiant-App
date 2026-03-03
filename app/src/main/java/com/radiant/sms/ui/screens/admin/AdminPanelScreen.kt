package com.radiant.sms.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.data.Repository
import com.radiant.sms.network.AdminMemberDto
import com.radiant.sms.network.NetworkModule

@Composable
fun AdminPanelScreen(
    nav: NavController,
    modifier: Modifier = Modifier,
    onMemberClick: (Int) -> Unit,
    onCreateMemberClick: () -> Unit,
    onExportPdfClick: () -> Unit,
    onExportExcelClick: () -> Unit
) {
    val api = remember { NetworkModule.api(nav.context) }
    val repo = remember { Repository(api) }

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var members by remember { mutableStateOf<List<AdminMemberDto>>(emptyList()) }

    val refreshFlagFlow = remember(nav) {
        nav.currentBackStackEntry?.savedStateHandle?.getStateFlow("members_refresh", false)
    }
    val refreshFlag by refreshFlagFlow?.collectAsState() ?: remember { mutableStateOf(false) }

    LaunchedEffect(Unit, refreshFlag) {
        if (refreshFlag) {
            nav.currentBackStackEntry?.savedStateHandle?.set("members_refresh", false)
        }

        loading = true
        error = null
        try {
            members = repo.adminMembersAll()
        } catch (e: Exception) {
            error = e.message ?: "Failed to load"
        } finally {
            loading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = onCreateMemberClick
            ) { Text("Create Member") }

            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onExportPdfClick
            ) { Text("PDF") }

            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onExportExcelClick
            ) { Text("Excel") }
        }

        Spacer(Modifier.height(14.dp))

        // Header row: Member | Share | Deposits
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Member",
                    modifier = Modifier.weight(1.4f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Share",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Deposits",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            error != null -> Text(error ?: "", color = MaterialTheme.colorScheme.error)

            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(members) { member ->
                    AdminMemberCard(
                        member = member,
                        onClick = {
                            member.id?.let { onMemberClick(it.toInt()) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminMemberCard(
    member: AdminMemberDto,
    onClick: () -> Unit
) {
    val shareCount = member.share ?: 0
    val depositCount = member.depositsCount ?: 0
    val totalAmount = member.totalDeposited ?: 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(18.dp)) {

            Text(
                text = member.fullName ?: "Unnamed Member",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = member.email ?: "",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(18.dp))

            // Bottom row: Share (left) | Deposits count (center) | Total amount (right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Share: $shareCount",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Deposits: $depositCount",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "৳ ${formatMoney(totalAmount)}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

private fun formatMoney(value: Double): String {
    return if (value % 1.0 == 0.0) "${value.toInt()}.0" else value.toString()
}
