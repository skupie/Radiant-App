package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.PullToRefreshBox
import androidx.compose.material3.Text
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * File: MemberShareDetailsScreen.kt
 * Path: app/src/main/java/com/radiant/sms/ui/screens/member/MemberShareDetailsScreen.kt
 *
 * Fixes included:
 * ✅ Material3-only modern pull refresh (PullToRefreshBox)
 * ✅ No refresh button (swipe to refresh only)
 * ✅ No back button on top-left for this screen (showBack = false)
 * ✅ Share "Created At" shows date only like: 19 October 2025
 * ✅ Added "Total Due" below "Total Deposit"
 * ✅ Member Information: remove Member ID -> show NID
 * ✅ Nominee Information: remove Phone/Relation/Address (keep only Name + NID)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberShareDetailsScreen(
    nav: NavController,
    tokenStore: TokenStore
) {
    val api = remember { NetworkModule.api(tokenStore) }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var response by remember { mutableStateOf<MemberShareDetailsResponse?>(null) }
    var totalDue by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    suspend fun load() {
        loading = true
        error = null
        try {
            // share + nominee + member (new endpoint already working)
            response = api.getMemberShareDetails()

            // Total due (current year)
            val year = LocalDate.now().year
            val dueRes = api.getMemberDueSummary(year)

            // Models.kt shows: dueRes.summary.total
            totalDue = dueRes.summary.total.toString()
        } catch (t: Throwable) {
            error = t.message ?: "Unknown error"
        } finally {
            loading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    val pullState = rememberPullToRefreshState()

    ScreenScaffold(
        title = "Share Details",
        nav = nav,
        showBack = false // ✅ remove back button from top-left
    ) {
        PullToRefreshBox(
            isRefreshing = loading,
            onRefresh = { scope.launch { load() } },
            state = pullState,
            modifier = Modifier.fillMaxSize()
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

                val member = response?.member
                val share = response?.resolvedShare
                val nominee = response?.resolvedNominee

                // ---------------- MEMBER ----------------
                InfoCardWithPhoto(
                    title = "Member Information",
                    photoUrl = member?.displayPhotoUrl,
                    tokenStore = tokenStore
                ) {
                    InfoRow("Name", member?.displayName)
                    InfoRow("Email", member?.email)

                    // ✅ Requirement: remove Member ID, replace with NID
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

                    // ✅ Requirement: show only date like "19 October 2025"
                    // displayCreatedAt is a String from API; we format it safely.
                    InfoRow("Created At", formatCreatedAtToDateOnly(share?.displayCreatedAt))
                }

                // ---------------- NOMINEE ----------------
                InfoCardWithPhoto(
                    title = "Nominee Information",
                    photoUrl = nominee?.displayPhotoUrl,
                    tokenStore = tokenStore
                ) {
                    InfoRow("Name", nominee?.name)

                    // ✅ Requirement: remove Phone, Relation, Address
                    InfoRow("NID", nominee?.nid)
                }
            }
        }
    }
}

/**
 * Converts API timestamp to "d MMMM yyyy" (e.g., "19 October 2025")
 * Handles:
 * - ISO strings: 2025-10-19T10:15:30Z
 * - Common DB strings: 2025-10-19 10:15:30
 * - If parsing fails, returns original string
 */
private fun formatCreatedAtToDateOnly(value: String?): String? {
    if (value.isNullOrBlank()) return null

    val outFmt = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
    val zone = ZoneId.systemDefault()

    // 1) Try ISO instant
    runCatching {
        val instant = Instant.parse(value)
        return outFmt.withZone(zone).format(instant)
    }

    // 2) Try "yyyy-MM-dd HH:mm:ss" (Laravel often returns this)
    runCatching {
        val inFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val ldt = LocalDateTime.parse(value, inFmt)
        return ldt.toLocalDate().format(outFmt)
    }

    // 3) Try "yyyy-MM-dd" only
    runCatching {
        val inFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        val d = LocalDate.parse(value, inFmt)
        return d.format(outFmt)
    }

    return value
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
