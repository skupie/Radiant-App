package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.radiant.sms.data.Repository
import com.radiant.sms.network.NetworkModule

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

    var shareCount by remember { mutableStateOf(0) }
    var totalDeposit by remember { mutableStateOf(0.0) }

    LaunchedEffect(Unit) {

        try {

            val me = repo.me()

            memberName = me.user.name ?: ""
            email = me.user.email ?: ""

            // This endpoint already exists in your repository
            val shares = repo.memberShareDetails()

            shareCount = shares.totalShares
            totalDeposit = shares.totalDeposits

        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }

    }

    ScreenScaffold(
        nav = nav,
        title = "Share Information",
        hideTitle = false
    ) {

        if (loading) {

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }

            return@ScreenScaffold
        }

        if (error != null) {

            Text(
                text = error ?: "",
                color = MaterialTheme.colorScheme.error
            )

            return@ScreenScaffold
        }

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        memberName,
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        email,
                        style = MaterialTheme.typography.bodyMedium
                    )

                }

            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text("Total Shares")

                        Spacer(Modifier.height(6.dp))

                        Text(
                            shareCount.toString(),
                            style = MaterialTheme.typography.headlineMedium
                        )

                    }

                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text("Total Deposit")

                        Spacer(Modifier.height(6.dp))

                        Text(
                            "৳ $totalDeposit",
                            style = MaterialTheme.typography.headlineMedium
                        )

                    }

                }

            }

        }

    }

}
