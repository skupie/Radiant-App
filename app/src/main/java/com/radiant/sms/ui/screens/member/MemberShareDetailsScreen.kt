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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.data.TokenStore
import com.radiant.sms.network.MemberShareDetailsResponse
import com.radiant.sms.network.NetworkModule
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun MemberShareDetailsScreen(nav: NavController) {
    val tokenStore = remember { TokenStore(nav.context) }

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var data by remember { mutableStateOf<MemberShareDetailsResponse?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        error = null
        try {
            val token = tokenStore.tokenFlow.first()
            val api = NetworkModule.createApiService { token }
            data = api.getMemberShareDetails()
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    ScreenScaffold(title = "Share Details", nav = nav) {
        when {
            loading -> {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            data != null -> {
                ShareDetailsContent(res = data!!)
            }
        }
    }
}

@Composable
private fun ShareDetailsContent(res: MemberShareDetailsResponse) {
    val m = res.member

    val fullName = m.full_name ?: "-"
    val nid = m.nid ?: "-"
    val email = m.email ?: "-"
    val phone = m.phone ?: "-"
    val shareCount = m.share ?: 0

    val profileCreated = formatPrettyDate(m.created_at)
    val totalDeposited = res.total_deposited ?: 0.0
    val totalDue = res.total_due ?: 0.0

    val nomineeName = m.nominee_name ?: "-"
    val nomineeNid = m.nominee_nid ?: "-"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(initials = initialsFromName(fullName))

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Member NID: $nid",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.width(12.dp))

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Total Shares: $shareCount",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Each monthly deposit is based on this share count.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Divider()

            // Contact + Account summary (2 columns)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    SectionTitle("CONTACT DETAILS")
                    KeyValueRow("Email", email)
                    KeyValueRow("Mobile Number", phone)
                }

                Column(modifier = Modifier.weight(1f)) {
                    SectionTitle("ACCOUNT SUMMARY")
                    KeyValueRow("Share Count", shareCount.toString())
                    KeyValueRow("Profile Created", profileCreated)
                    KeyValueRow("Total Deposited", formatMoney(totalDeposited))
                    KeyValueRow("Total Due Amount", formatMoney(totalDue))
                }
            }
        }

        // Nominee card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Nominee Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "The nominated beneficiary information linked to your shares.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Avatar(initials = initialsFromName(nomineeName))

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = nomineeName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Nominee NID: $nomineeNid",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Text(
                        text = "Nominee information is read-only. Please contact the administrator for updates.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.widthIn(max = 240.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun KeyValueRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun Avatar(initials: String) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun initialsFromName(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    if (parts.isEmpty()) return "?"
    val first = parts.getOrNull(0)?.firstOrNull()?.uppercaseChar() ?: '?'
    val second = parts.getOrNull(1)?.firstOrNull()?.uppercaseChar()
    return if (second != null) "$first$second" else "$first"
}

private fun formatMoney(v: Double): String {
    // simple formatting without extra deps
    return "%,.2f".format(v)
}

private fun formatPrettyDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "-"
    // If backend sends "2025-10-19" or "2025-10-19T..." this will handle both
    return try {
        val datePart = raw.take(10)
        val dt = LocalDate.parse(datePart)
        dt.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    } catch (_: Exception) {
        raw
    }
}
