package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.radiant.sms.data.Repository
import com.radiant.sms.network.NetworkModule
import androidx.compose.ui.platform.LocalContext

@Composable
fun MemberShareDetailsScreen(
    nav: NavController
) {

    val context = LocalContext.current
    val api = remember { NetworkModule.api(context) }
    val repo = remember { Repository(api) }

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var memberName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var nid by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    var shareCount by remember { mutableStateOf(0) }
    var totalDeposit by remember { mutableStateOf(0.0) }

    LaunchedEffect(Unit) {

        try {

            val profile = repo.me()

            memberName = profile.user.name ?: ""
            email = profile.user.email ?: ""
            nid = profile.user.nid ?: ""
            imageUrl = profile.user.image ?: ""

            val share = repo.memberShares()

            shareCount = share.totalShares
            totalDeposit = share.totalDeposits

        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }

    }

    ScreenScaffold(
        nav = nav,
        title = "Member Details",
        hideTitle = false
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(10.dp))

            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = memberName,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "NID: $nid",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEEF4FF)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            "Total Shares",
                            style = MaterialTheme.typography.labelMedium
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            shareCount.toString(),
                            style = MaterialTheme.typography.headlineSmall
                        )

                    }

                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE9F8EF)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            "Total Deposit",
                            style = MaterialTheme.typography.labelMedium
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            "৳ $totalDeposit",
                            style = MaterialTheme.typography.headlineSmall
                        )

                    }

                }

            }

        }

    }

}
