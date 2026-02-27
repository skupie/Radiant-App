package com.radiant.sms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.radiant.sms.ui.Routes
import com.radiant.sms.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberHomeScreen(nav: NavController, vm: AuthViewModel = viewModel()) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Member Dashboard") },
                actions = {
                    TextButton(onClick = {
                        vm.logout()
                        nav.navigate(Routes.LOGIN) { popUpTo(Routes.MEMBER_HOME) { inclusive = true } }
                    }) { Text("Logout") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Member features wired to API:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Bullet("Profile: GET /api/member/profile")
            Bullet("Ledger: GET /api/member/ledger?year=YYYY")
            Bullet("Due Summary: GET /api/member/due-summary?year=YYYY")
            Bullet("Share Details: GET /api/member/share-details")
            Spacer(Modifier.height(14.dp))
            Text("Next: I can add real screens (lists/tables) based on your actual response JSON.")
        }
    }
}

@Composable private fun Bullet(text: String) {
    Row(Modifier.fillMaxWidth()) {
        Text("• ")
        Text(text)
    }
    Spacer(Modifier.height(6.dp))
}
