package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.radiant.sms.data.Repository
import com.radiant.sms.data.TokenStore
import com.radiant.sms.network.MemberShareDetailsResponse
import com.radiant.sms.network.NetworkModule
import kotlinx.coroutines.launch

@Composable
fun MemberShareDetailsScreen(nav: NavController) {

    val context = LocalContext.current
    val repo = remember { Repository(NetworkModule.api(context)) }
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var data by remember { mutableStateOf<MemberShareDetailsResponse?>(null) }

    val tokenStore = remember { TokenStore(context) }

    fun load() {
        scope.launch {
            try {
                loading = true
                error = null
                data = repo.memberShareDetails()
            } catch (e: Exception) {
                error = e.message ?: "Failed to load share details"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    ScreenScaffold(title = "Share Details", nav = nav) {

        if (loading) {
            CircularProgressIndicator()
            return@ScreenScaffold
        }

        if (error != null) {
            Text("Error: ${error!!}", color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(12.dp))
            Button(onClick = { load() }) { Text("Retry") }
            return@ScreenScaffold
        }

        val member = data?.member
        val share = data?.share
        val nominee = data?.nominee

        Button(
            onClick = { load() },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Refresh") }

        Spacer(Modifier.height(12.dp))

        InfoCardWithPhoto(
            title = "Member Information",
            photoUrl = member?.displayPhotoUrl,
            tokenStore = tokenStore
        ) {
            InfoRow("Name", member?.displayName)
            InfoRow("Email", member?.email)
            InfoRow("Phone", member?.displayPhone)
            InfoRow("Member ID", member?.displayMemberId)
        }

        InfoCard(title = "Share Information") {
            InfoRow("Share No", share?.displayShareNo)
            InfoRow("Share Amount", share?.displayShareAmount)
            InfoRow("Total Deposit", share?.displayTotalDeposit)
            InfoRow("Created At", share?.displayCreatedAt)
        }

        InfoCardWithPhoto(
            title = "Nominee Information",
            photoUrl = nominee?.displayPhotoUrl,
            tokenStore = tokenStore
        ) {
            InfoRow("Name", nominee?.name)
            InfoRow("Phone", nominee?.displayPhone)
            InfoRow("Relation", nominee?.relation)
            InfoRow("NID", nominee?.nid)
            InfoRow("Address", nominee?.address)
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
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
            Text(title, style = MaterialTheme.typography.titleMedium)
            Divider()
            content()
        }
    }
    Spacer(Modifier.height(12.dp))
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

                MemberCirclePhoto(
                    photoUrl = photoUrl,
                    tokenStore = tokenStore
                )
            }

            Divider()
            content()
        }
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun MemberCirclePhoto(
    photoUrl: String?,
    tokenStore: TokenStore
) {
    val context = LocalContext.current

    // ✅ Correct TokenStore API
    val token = remember { tokenStore.getTokenSync() }

    // ✅ Explicit type prevents type inference issues
    val model: ImageRequest? = remember(photoUrl, token) {
        if (photoUrl.isNullOrBlank()) null
        else {
            ImageRequest.Builder(context)
                .data(photoUrl)
                .apply {
                    // If your photo endpoint is protected, send auth header
                    if (!token.isNullOrBlank()) {
                        addHeader("Authorization", "Bearer $token")
                    }
                }
                .crossfade(true)
                .build()
        }
    }

    if (model == null) {
        Surface(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {}
        return
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
