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
fun AdminHomeScreen(nav: NavController, vm: AuthViewModel = viewModel()) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    TextButton(onClick = {
                        vm.logout()
                        nav.navigate(Routes.LOGIN) { popUpTo(Routes.ADMIN_HOME) { inclusive = true } }
                    }) { Text("Logout") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Admin features wired to API:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Bullet("Members: GET /api/admin/members?search=&per_page=")
            Bullet("Deposits: GET /api/admin/deposits?search=&per_page=")
            Bullet("Due Summary: GET /api/admin/due-summary?search=&per_page=")
            Spacer(Modifier.height(14.dp))
            Text("Next: I can generate list screens for Members/Deposits using your real response JSON.")
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
