// app/src/main/java/com/radiant/sms/ui/screens/MemberHomeScreen.kt
package com.radiant.sms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.radiant.sms.ui.Routes
import com.radiant.sms.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberHomeScreen(nav: NavController, authVm: AuthViewModel = viewModel()) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Member Dashboard") },
                actions = {
                    TextButton(onClick = {
                        authVm.logout()
                        nav.navigate(Routes.LOGIN) { popUpTo(0) }
                    }) { Text("Logout") }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text("Member features wired to API:", style = MaterialTheme.typography.titleMedium)

            FeatureButton("Profile", "GET /api/member/profile") {
                nav.navigate(Routes.MEMBER_PROFILE)
            }

            FeatureButton("Ledger", "GET /api/member/ledger?year=YYYY") {
                nav.navigate(Routes.MEMBER_LEDGER)
            }

            FeatureButton("Due Summary", "GET /api/member/due-summary?year=YYYY") {
                nav.navigate(Routes.MEMBER_DUE_SUMMARY)
            }

            FeatureButton("Share Details", "GET /api/member/share-details") {
                nav.navigate(Routes.MEMBER_SHARE_DETAILS)
            }
        }
    }
}

@Composable
private fun FeatureButton(title: String, subtitle: String, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
