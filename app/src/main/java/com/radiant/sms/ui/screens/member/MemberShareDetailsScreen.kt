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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
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

                // Load due summary safely
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

    // ✅ New Scaffold (no padding lambda)
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

                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(member.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Member Image",
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = member.displayName ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = member.email ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "NID: ${member.displayNid}",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // =========================
                // 💰 SHARE INFO
                // =========================

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {

                        Text(
                            "Share Information",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        InfoRow("Share No", member.share.toString())
                        InfoRow("Share Amount", member.shareAmount.toString())
                        InfoRow("Total Deposit", res.totalDeposited.toString())
                        InfoRow("Total Due", totalDue ?: "0")

                        val formattedDate = try {
                            val parsed = OffsetDateTime.parse(member.createdAt)
                            parsed.format(
                                DateTimeFormatter.ofPattern(
                                    "dd MMMM yyyy",
                                    Locale.getDefault()
                                )
                            )
                        } catch (e: Exception) {
                            member.createdAt
                        }

                        InfoRow("Created At", formattedDate)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // =========================
                // 👩 NOMINEE INFO
                // =========================

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {

                        Text(
                            "Nominee Information",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(member.nomineeImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Nominee Image",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        InfoRow("Name", member.nomineeName ?: "")
                        InfoRow("NID", member.nomineeNid ?: "")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
