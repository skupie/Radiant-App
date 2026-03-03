package com.radiant.sms.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.data.Repository
import com.radiant.sms.network.AdminMemberDto
import com.radiant.sms.network.NetworkModule
import java.util.Locale

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

    // ---- Fintech style ----
    val screenBg = Color(0xFFF6F7FB)
    val cardBg = Color.White
    val subtleText = Color(0xFF6B7280)
    val cardShape = RoundedCornerShape(22.dp)
    val chipShape = RoundedCornerShape(999.dp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        // Header Card (matches Deposits page look)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = cardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Members",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(12.dp))

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
            }
        }

        Spacer(Modifier.height(14.dp))

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            error != null -> Text(error ?: "", color = MaterialTheme.colorScheme.error)

            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(members) { member ->
                    AdminMemberCardModern(
                        member = member,
                        cardBg = cardBg,
                        subtleText = subtleText,
                        cardShape = cardShape,
                        chipShape = chipShape,
                        onClick = { member.id?.let { onMemberClick(it.toInt()) } }
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun AdminMemberCardModern(
    member: AdminMemberDto,
    cardBg: Color,
    subtleText: Color,
    cardShape: RoundedCornerShape,
    chipShape: RoundedCornerShape,
    onClick: () -> Unit
) {
    val shareCount = member.share ?: 0
    val depositCount = member.depositsCount ?: 0
    val totalAmount = member.totalDeposited ?: 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            // Top row: Name + Total deposited badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = member.fullName ?: "Unnamed Member",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = member.email ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtleText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(10.dp))

                // Total deposited pill (fintech style like deposit page)
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            shape = chipShape
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "BDT ${formatBDT(totalAmount)}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Bottom row: Share | Deposits
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Share", style = MaterialTheme.typography.labelSmall, color = subtleText)
                    Text(
                        text = shareCount.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Deposits", style = MaterialTheme.typography.labelSmall, color = subtleText)
                    Text(
                        text = depositCount.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun formatBDT(value: Double): String {
    // Always 2 decimals, modern finance look
    return String.format(Locale.getDefault(), "%.2f", value)
}
