package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MemberShareDetailsScreen(nav: NavController, tokenStore: TokenStore) {
    val api = remember { NetworkModule.api(tokenStore) }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var data by remember { mutableStateOf<MemberShareDetailsResponse?>(null) }
    var totalDue by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    suspend fun load() {
        loading = true
        error = null
        try {
            // Share + nominee + member
            data = api.getMemberShareDetails()

            // Total due (use current year)
            val year = LocalDate.now().year
            totalDue = api.getMemberDueSummary(year).data?.summary?.total?.toString()
        } catch (t: Throwable) {
            error = t.message ?: "Unknown error"
        } finally {
            loading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = loading,
        onRefresh = { scope.launch { load() } }
    )

    ScreenScaffold(
        title = "Share Details",
        nav = nav,
        showBack = false // ✅ remove back button for this screen
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
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

                val member = data?.data?.member
                val share = data?.data?.share
                val nominee = data?.data?.nominee

                // ---------------- MEMBER ----------------
                InfoCardWithPhoto(
                    title = "Member Information",
                    photoUrl = member?.displayPhotoUrl,
                    tokenStore = tokenStore
                ) {
                    InfoRow("Name", member?.displayName)
                    InfoRow("Email", member?.email)

                    // ✅ Requirement: remove Member ID row, show Member NID
                    InfoRow("NID", member?.displayNid)
                }

                // ---------------- SHARE ----------------
                InfoCardWithPhoto(
                    title = "Share Information",
                    photoUrl = null,
                    tokenStore = tokenStore
                ) {
                    InfoRow("Share No", share?.displayShareNo)
                    InfoRow("Share Amount", share?.displayShareAmount)
                    InfoRow("Total Deposit", share?.displayTotalDeposit)

                    // ✅ Requirement: add Total Due below Total Deposit
                    InfoRow("Total Due", totalDue)

                    // ✅ Requirement: date only (19 October 2025)
                    InfoRow("Created At", formatIsoDate(share?.displayCreatedAt))
                }

                // ---------------- NOMINEE ----------------
                InfoCardWithPhoto(
                    title = "Nominee Information",
                    photoUrl = nominee?.displayPhotoUrl,
                    tokenStore = tokenStore
                ) {
                    InfoRow("Name", nominee?.name)

                    // ✅ Requirement: remove Phone/Relation/Address; keep only NID
                    InfoRow("NID", nominee?.displayNid)
                }
            }

            // ✅ Swipe-to-refresh indicator (replaces Refresh button)
            PullRefreshIndicator(
                refreshing = loading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

private fun formatIsoDate(value: String?): String? {
    if (value.isNullOrBlank()) return null
    return try {
        val instant = Instant.parse(value)
        val fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
            .withZone(ZoneId.systemDefault())
        fmt.format(instant)
    } catch (_: Throwable) {
        value // fallback if parsing fails
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
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                if (!photoUrl.isNullOrBlank()) {
                    MemberCirclePhoto(
                        photoUrl = photoUrl,
                        tokenStore = tokenStore
                    )
                }
            }

            Divider()
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

    val model = remember(photoUrl, token) {
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
        model = model,
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
        Text(label)
        Text(value ?: "-")
    }
}
