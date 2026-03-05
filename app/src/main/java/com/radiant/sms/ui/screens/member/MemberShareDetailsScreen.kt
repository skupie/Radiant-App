package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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

                        ProfileImage(
                            url = member?.displayPhotoUrl,
                            size = 120.dp,
                            contentDescription = "Member Image"
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            member?.displayName ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            member?.email ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            member?.displayNid ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // 💰 SUMMARY CARDS (NO weight() used)
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val gap = 10.dp
                    val cardWidth = (maxWidth - (gap * 2)) / 3f

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(gap)
                    ) {
                        SummaryCard(
                            modifier = Modifier.width(cardWidth),
                            title = "Shares",
                            value = share?.displayShareNo ?: "-"
                        )
                        SummaryCard(
                            modifier = Modifier.width(cardWidth),
                            title = "Deposit",
                            value = share?.displayTotalDeposit ?: "-"
                        )
                        SummaryCard(
                            modifier = Modifier.width(cardWidth),
                            title = "Due",
                            value = totalDue ?: "-"
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // 📌 SHARE DETAILS
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Share Details", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(10.dp))

                        InfoRow("Share No", share?.displayShareNo ?: "-")
                        InfoRow("Total Deposit", share?.displayTotalDeposit ?: "-")
                        InfoRow("Total Due", totalDue ?: "-")

                        val formattedCreatedAt = remember(share?.displayCreatedAt) {
                            share?.displayCreatedAt?.let { formatDate(it) } ?: "-"
                        }
                        InfoRow("Created At", formattedCreatedAt)
                    }
                }

                Spacer(Modifier.height(18.dp))

                // 👩‍🦰 NOMINEE DETAILS
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Nominee Details", style = MaterialTheme.typography.titleMedium)

                        Spacer(Modifier.height(16.dp))

                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            ProfileImage(
                                url = nominee?.displayPhotoUrl,
                                size = 100.dp,
                                contentDescription = "Nominee Image"
                            )
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
private fun SummaryCard(
    modifier: Modifier,
    title: String,
    value: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ProfileImage(
    url: String?,
    size: Dp,
    contentDescription: String
) {
    val context = LocalContext.current

    if (url.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = contentDescription,
                modifier = Modifier.size(size * 0.45f)
            )
        }
        return
    }

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context).data(url).crossfade(true).build(),
        contentDescription = contentDescription,
        modifier = Modifier.size(size).clip(CircleShape),
        loading = {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        },
        error = {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(size * 0.45f)
                )
            }
        }
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.End)
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val odt = OffsetDateTime.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
        odt.format(formatter)
    } catch (_: Exception) {
        dateString
    }
}
