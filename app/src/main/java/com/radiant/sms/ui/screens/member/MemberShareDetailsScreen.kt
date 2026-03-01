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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.radiant.sms.data.TokenStore
import com.radiant.sms.network.MemberShareDetailsResponse
import com.radiant.sms.network.NetworkModule
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * File: MemberShareDetailsScreen.kt
 * Path: app/src/main/java/com/radiant/sms/ui/screens/member/MemberShareDetailsScreen.kt
 *
 * Stable version:
 * - No PullToRefreshContainer
 * - No nestedScrollConnection
 * - No isRefreshing / endRefresh
 * - No .data usage
 * - Shows loading spinner
 * - Date formatted properly
 */

@Composable
fun MemberShareDetailsScreen(nav: NavController) {

    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }

    val api = remember {
        NetworkModule.createApiService {
            tokenStore.getTokenSync()
        }
    }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var response by remember { mutableStateOf<MemberShareDetailsResponse?>(null) }
    var totalDue by remember { mutableStateOf<String?>(null) }

    suspend fun load() {
        loading = true
        error = null
        try {
            response = api.getMemberShareDetails()

            val year = LocalDate.now().year
            val dueRes = api.getMemberDueSummary(year)
            totalDue = dueRes.summary.total.toString()

        } catch (t: Throwable) {
            error = t.message ?: "Unknown error"
        } finally {
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        load()
    }

    ScreenScaffold(
        title = "Share Details",
        nav = nav,
        showBack = false
    ) {

        Box(modifier = Modifier.fillMaxSize()) {

            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                if (!error.isNullOrBlank()) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                val member = response?.member
                val share = response?.share
                val nominee = response?.nominee

                // MEMBER INFORMATION
                InfoCardWithPhoto(
                    title = "Member Information",
                    photoUrl = member?.displayPhotoUrl,
                    tokenStore = tokenStore
                ) {
                    InfoRow("Name", member?.displayName)
                    InfoRow("Email", member?.email)
                    InfoRow("NID", member?.displayNid)
                }

                // SHARE INFORMATION
                InfoCardWithPhoto(
                    title = "Share Information",
                    photoUrl = null,
                    tokenStore = tokenStore
                ) {
                    InfoRow("Share No", share?.displayShareNo)
                    InfoRow("Share Amount", share?.displayShareAmount)
                    InfoRow("Total Deposit", share?.displayTotalDeposit)
                    InfoRow("Total Due", totalDue)
                    InfoRow("Created At", formatIsoDate(share?.displayCreatedAt))
                }

                // NOMINEE INFORMATION
                InfoCardWithPhoto(
                    title = "Nominee Information",
                    photoUrl = nominee?.displayPhotoUrl,
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
    return try {
        val instant = Instant.parse(value)
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
            .withZone(ZoneId.systemDefault())
        formatter.format(instant)
    } catch (_: Throwable) {
        value
    }
}

@Composable
private fun InfoCardWithPhoto(
    title: String,
    photoUrl: String?,
    tokenStore: TokenStore,
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                if (!photoUrl.isNullOrBlank()) {
                    MemberCirclePhoto(photoUrl, tokenStore)
                }
            }

            HorizontalDivider()
            content()
        }
    }

    Spacer(Modifier.height(12.dp))
}

@Composable
private fun MemberCirclePhoto(
    photoUrl: String,
    tokenStore: TokenStore
) {
    val context = LocalContext.current
    val token = remember { tokenStore.getTokenSync() }

    val request = remember(photoUrl, token) {
        ImageRequest.Builder(context)
            .data(photoUrl)
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
            .size(46.dp)
            .clip(CircleShape)
    )
}

@Composable
private fun InfoRow(label: String, value: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label)
        Text(text = value ?: "-")
    }
}
