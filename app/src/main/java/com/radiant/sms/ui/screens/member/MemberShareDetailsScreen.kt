package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Divider
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MemberShareDetailsScreen(nav: NavController) {
    val context = LocalContext.current
    val tokenStore = remember { TokenStore(context) }
    val api = remember { NetworkModule.api(context) }

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
            // NOTE: this assumes your ApiService has this endpoint/method already.
            val dueResp = api.getMemberDueSummary(year)
            totalDue = dueResp.data?.summary?.total?.toString()
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
        showBack = false // ✅ remove back button from top-left
        // ✅ refresh button removed (we only use swipe-to-refresh)
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

                val member = data?.member
                val share = data?.share
                val nominee = data?.nominee

                // ---------------- MEMBER ----------------
                InfoCardWithPhoto(
                    title = "Member Information",
                    photoUrl = member?.displayPhotoUrl,
                    tokenStore = tokenStore
                ) {
                    InfoRow("Name", member?.displayName)
                    InfoRow("Email", member?.email)

                    // ✅ Remove Member ID row, show Member NID
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

                    // ✅ Add Total Due below Total Deposit
                    InfoRow("Total Due", totalDue)

                    // ✅ Show only date like "19 October 2025"
                    InfoRow("Created At", formatToReadableDate(share?.displayCreatedAt))
                }

                // ---------------- NOMINEE ----------------
                InfoCardWithPhoto(
                    title = "Nominee Information",
                    photoUrl = nominee?.displayPhotoUrl,
                    tokenStore = tokenStore
                ) {
                    InfoRow("Name", nominee?.name)

                    // ✅ Remove Phone/Relation/Address; keep only NID
                    InfoRow("NID", nominee?.displayNid)
                }
            }

            // ✅ Swipe-to-refresh indicator
            PullRefreshIndicator(
                refreshing = loading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

private fun formatToReadableDate(value: String?): String? {
    if (value.isNullOrBlank()) return null

    val outFmt = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
        .withZone(ZoneId.systemDefault())

    // Try a few common timestamp formats safely
    return runCatching {
        // Example: 2025-10-19T12:34:56Z
        outFmt.format(Instant.parse(value))
    }.getOrElse {
        runCatching {
            // Example: 2025-10-19T12:34:56+06:00
            outFmt.format(OffsetDateTime.parse(value).toInstant())
        }.getOrElse {
            runCatching {
                // Example: 2025-10-19T12:34:56
                val ldt = LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ldt.toLocalDate().format(outFmt.withZone(ZoneId.systemDefault()))
            }.getOrElse {
                runCatching {
                    // Example: 2025-10-19 12:34:56
                    val dt = LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    dt.toLocalDate().format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH))
                }.getOrElse {
                    // If nothing matches, show raw value
                    value
                }
            }
        }
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
        Text(text = label)
        Text(text = value ?: "-")
    }
}
