package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.radiant.sms.network.MemberShareDetailsResponse
import com.radiant.sms.network.NetworkModule
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MemberShareDetailsScreen(nav: NavController) {

    val context = LocalContext.current
    val api = remember { NetworkModule.api(context) }

    var response by remember { mutableStateOf<MemberShareDetailsResponse?>(null) }
    var totalDue by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val currentYear = remember { java.time.LocalDate.now().year }

    fun load() {
        scope.launch {
            isLoading = true
            error = null
            try {
                val res = api.getMemberShareDetails()
                response = res

                runCatching {
                    val dueRes = api.getMemberDueSummary(currentYear)
                    totalDue = dueRes.summary.total.toString()
                }
            } catch (e: Exception) {
                error = e.message ?: "Failed to load share details"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    ScreenScaffold(
        nav = nav,
        title = "Share Information",
        hideTitle = false
    ) {

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
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
            }

            response?.let { res ->

                val member = res.member
                val share = res.resolvedShare
                val nominee = res.resolvedNominee

                // 👤 MEMBER PROFILE
                ElevatedCard(Modifier.fillMaxWidth()) {

                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        ProfileImage(member?.displayPhotoUrl)

                        Spacer(Modifier.height(10.dp))

                        Text(
                            member?.displayName ?: "",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text(
                            member?.email ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Text(
                            member?.displayNid ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // 💰 SUMMARY CARDS
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    SummaryCard("Shares", share?.displayShareNo ?: "-")

                    SummaryCard("Deposit", share?.displayTotalDeposit ?: "-")

                    SummaryCard("Due", totalDue ?: "-")
                }

                Spacer(Modifier.height(20.dp))

                // 📌 SHARE DETAILS
                ElevatedCard(Modifier.fillMaxWidth()) {

                    Column(Modifier.padding(16.dp)) {

                        Text(
                            "Share Details",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(Modifier.height(10.dp))

                        InfoRow("Share No", share?.displayShareNo ?: "-")
                        InfoRow("Deposit", share?.displayTotalDeposit ?: "-")

                        val formattedCreatedAt = remember(share?.displayCreatedAt) {
                            share?.displayCreatedAt?.let { formatDate(it) } ?: "-"
                        }

                        InfoRow("Created", formattedCreatedAt)
                    }
                }

                Spacer(Modifier.height(20.dp))

                // 👩 NOMINEE
                ElevatedCard(Modifier.fillMaxWidth()) {

                    Column(Modifier.padding(16.dp)) {

                        Text(
                            "Nominee Information",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(Modifier.height(16.dp))

                        Box(
                            Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            ProfileImage(nominee?.displayPhotoUrl, 90)
                        }

                        Spacer(Modifier.height(20.dp))

                        InfoRow("Name", nominee?.displayName ?: "")
                        InfoRow("NID", nominee?.displayNid ?: "")
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String) {

    Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {

        Column(
            Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(title, style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(6.dp))

            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ProfileImage(url: String?, size: Int = 110) {

    val context = LocalContext.current

    if (url.isNullOrBlank()) {

        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null, Modifier.size((size * 0.45).dp))
        }
        return
    }

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape),
        loading = { CircularProgressIndicator() }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {

        Text(label, Modifier.weight(1f))

        Text(value, textAlign = TextAlign.End)
    }
}

private fun formatDate(dateString: String): String {

    return try {

        val odt = OffsetDateTime.parse(dateString)

        val formatter =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

        odt.format(formatter)

    } catch (_: Exception) {

        dateString
    }
}
