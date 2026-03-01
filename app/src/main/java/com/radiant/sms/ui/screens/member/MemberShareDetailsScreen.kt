package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.radiant.sms.data.TokenStore
import com.radiant.sms.network.NetworkModule
import com.radiant.sms.network.MemberShareDetailsResponse
import com.radiant.sms.ui.components.ScreenScaffold
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MemberShareDetailsScreen(nav: NavController) {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }
    val api = remember { NetworkModule.api(context) }

    var response by remember { mutableStateOf<MemberShareDetailsResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var totalDue by remember { mutableStateOf<String?>(null) }
    val currentYear = remember { java.time.LocalDate.now().year }

    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            isLoading = true
            error = null
            try {
                response = api.getMemberShareDetails()

                // Due Summary (safe)
                runCatching {
                    val dueRes = api.getMemberDueSummary(currentYear) // Int? expected
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
        title = "",
        nav = nav,
        showBack = false,
        showHamburger = true,
        hideTitle = true
    ) { padding ->

        if (isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@ScreenScaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            if (error != null) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
            }

            response?.let { res ->
                val member = res.member
                val share = res.share
                val nominee = res.nominee

                MemberHeaderCard(
                    memberName = member?.displayName,
                    email = member?.email,
                    nid = member?.displayNid,
                    photoUrl = member?.displayPhotoUrl,
                    tokenStore = tokenStore
                )

                InfoCard(title = "Share Information") {
                    InfoRow("Share No", share?.displayShareNo)
                    InfoRow("Share Amount", share?.displayShareAmount)
                    InfoRow("Total Deposit", share?.displayTotalDeposit)
                    InfoRow("Total Due", totalDue ?: "-")
                    InfoRow("Created At", formatIsoDate(share?.displayCreatedAt))
                }

                Spacer(Modifier.height(12.dp))

                InfoCardWithPhoto(
                    title = "Nominee Information",
                    photoUrl = nominee?.displayPhotoUrl,
                    photoSize = 96.dp,
                    tokenStore = tokenStore
                ) {
                    InfoRow("Name", nominee?.name)
                    InfoRow("NID", nominee?.displayNid)
                }
            }
        }
    }
}

private fun formatIsoDate(value: String?): String? {
    if (value.isNullOrBlank()) return null
    val outFmt = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)

    runCatching { return OffsetDateTime.parse(value).format(outFmt) }
    runCatching { return LocalDateTime.parse(value).format(outFmt) }

    return value
}

@Composable
private fun MemberHeaderCard(
    memberName: String?,
    email: String?,
    nid: String?,
    photoUrl: String?,
    tokenStore: TokenStore
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!photoUrl.isNullOrBlank()) {
                MemberCirclePhoto(
                    photoUrl = photoUrl,
                    tokenStore = tokenStore,
                    size = 140.dp
                )
            }

            Text(
                text = memberName ?: "-",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )

            Text(
                text = email ?: "-",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Text(
                text = "NID: ${nid ?: "-"}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }

    Spacer(Modifier.height(12.dp))
}

@Composable
private fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            content()
        }
    }
}

@Composable
private fun InfoCardWithPhoto(
    title: String,
    photoUrl: String?,
    photoSize: Dp = 46.dp,
    tokenStore: TokenStore,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                if (!photoUrl.isNullOrBlank()) {
                    MemberCirclePhoto(photoUrl, tokenStore, size = photoSize)
                }
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value ?: "-", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun MemberCirclePhoto(
    photoUrl: String,
    tokenStore: TokenStore,
    size: Dp = 46.dp
) {
    val context = LocalContext.current
    val token = remember { tokenStore.getTokenSync() }

    val absUrl = remember(photoUrl) { NetworkModule.absoluteUrl(photoUrl) ?: photoUrl }

    val request = remember(absUrl, token) {
        ImageRequest.Builder(context)
            .data(absUrl)
            .apply {
                if (!token.isNullOrBlank()) {
                    addHeader("Authorization", "Bearer $token")
                }
            }
            .crossfade(true)
            .build()
    }

    AsyncImage(
        model = request,
        contentDescription = "photo",
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
    )
}
