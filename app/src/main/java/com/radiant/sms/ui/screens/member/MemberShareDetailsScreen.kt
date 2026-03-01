package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
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

/**
 * File: MemberShareDetailsScreen.kt
 * Path: app/src/main/java/com/radiant/sms/ui/screens/member/MemberShareDetailsScreen.kt
 *
 * Fixes:
 * ✅ Material3-only pull-to-refresh (PullToRefreshContainer + rememberPullToRefreshState)
 * ✅ No PullToRefreshBox (your Compose version doesn't have it)
 * ✅ NetworkModule.api(context) mismatch fixed by using NetworkModule.createApiService { token }
 * ✅ Removed tokenStore param so AppNavHost.kt doesn't need updating
 *
 * UI requirements kept:
 * ✅ Remove Member ID row, show Member NID
 * ✅ Add Total Due below Total Deposit
 * ✅ Created At shows date only (e.g., 19 October 2025)
 * ✅ Nominee: keep only Name + NID (remove phone/relation/address)
 * ✅ showBack = false
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberShareDetailsScreen(nav: NavController) {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }

    // NetworkModule.api(...) in your project expects Context, but we need auth token support.
    // So we use createApiService with token provider.
    val api = remember {
        NetworkModule.createApiService(
            tokenProvider = { tokenStore.getTokenSync() }
        )
    }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var response by remember { mutableStateOf<MemberShareDetailsResponse?>(null) }
    var totalDue by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    suspend fun load() {
        loading = true
        error = null
        try {
            // Member share details has a "data" wrapper in your model
            response = api.getMemberShareDetails()

            // Due summary returns MemberDueSummaryResponse directly (no .data wrapper)
            val year = LocalDate.now().year
            val dueRes = api.getMemberDueSummary(year)
            totalDue = dueRes.summary.total.toString()
        } catch (t: Throwable) {
            error = t.message ?: "Unknown error"
        } finally {
            loading = false
        }
    }

    // initial load
    LaunchedEffect(Unit) { load() }

    // Material3 pull-to-refresh state (works without PullToRefreshBox)
    val pullState = rememberPullToRefreshState()

    // When user triggers refresh, run load() and end refresh when done
    LaunchedEffect(pullState.isRefreshing) {
        if (pullState.isRefreshing) {
            load()
            pullState.endRefresh()
        }
    }

    // Also end refresh if we finish loading by other means
    LaunchedEffect(loading) {
        if (!loading && pullState.isRefreshing) {
            pullState.endRefresh()
        }
    }

    ScreenScaffold(
        title = "Share Details",
        nav = nav,
        showBack = false
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(pullState.nestedScrollConnection)
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

                val member = response?.data?.resolvedMember
                val share = response?.data?.resolvedShare
                val nominee = response?.data?.resolvedNominee

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

            // Material3 indicator
            PullToRefreshContainer(
                state = pullState,
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
                    MemberCirclePhoto(
                        photoUrl = photoUrl,
                        tokenStore = tokenStore
                    )
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
        Text(text = label)
        Text(text = value ?: "-")
    }
}
