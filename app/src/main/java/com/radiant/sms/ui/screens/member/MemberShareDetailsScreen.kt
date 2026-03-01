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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.radiant.sms.network.NetworkModule
import com.radiant.sms.network.MemberShareDetailsResponse
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

    ScreenScaffold(nav = nav) {

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
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
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            response?.let { res ->

                val member = res.member
                val share = res.resolvedShare
                val nominee = res.resolvedNominee

                // =========================
                // 👤 MEMBER CARD
                // =========================
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(10.dp))
                        // ✅ FIX: Always show something (placeholder if URL null/fails)
                        ProfileImage(
                            url = member?.displayPhotoUrl,
                            size = 160.dp,
                            contentDescription = "Member Image"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = member?.displayName ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = member?.email ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = member?.displayNid ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // =========================
                // 📌 SHARE INFO
                // =========================
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {

                        Text(
                            "Share Information",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        InfoRow("Share No", share?.displayShareNo ?: "-")
                        InfoRow("Total Deposit", share?.displayTotalDeposit ?: "-")
                        InfoRow("Total Due", totalDue ?: "-")

                        val createdAt = share?.displayCreatedAt
                        val formattedCreatedAt = remember(createdAt) {
                            createdAt?.let { formatDate(it) } ?: "-"
                        }

                        InfoRow("Created At", formattedCreatedAt)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // =========================
                // 👩‍🦰 NOMINEE INFO
                // =========================
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {

                        Text(
                            "Nominee Information",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        ProfileImage(
                            url = nominee?.displayPhotoUrl,
                            size = 150.dp,
                            contentDescription = "Nominee Image"
                            horizontalAlignment = Alignment.CenterHorizontally
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        InfoRow("Name", nominee?.displayName ?: "")
                        InfoRow("NID", nominee?.displayNid ?: "")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
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

    // If URL is null/blank, show placeholder directly
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
        model = ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        modifier = Modifier
            .size(size)
            .clip(CircleShape),
        loading = {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
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
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val odt = OffsetDateTime.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
        odt.format(formatter)
    } catch (_: Exception) {
        // fallback if server sends plain "2025-10-19" etc.
        runCatching {
            val dt = java.time.LocalDate.parse(dateString)
            dt.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault()))
        }.getOrDefault(dateString)
    }
}
