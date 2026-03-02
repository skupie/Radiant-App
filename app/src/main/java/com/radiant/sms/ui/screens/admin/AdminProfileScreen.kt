package com.radiant.sms.ui.screens.admin

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.radiant.sms.ui.viewmodel.AuthViewModel

@Composable
fun AdminProfileScreen(nav: NavController, authVm: AuthViewModel = viewModel()) {
    val s by authVm.state.collectAsState()
    AdminScaffold(nav = nav) {
        Text("Admin Profile")
        Text("Name: ${s.userName ?: "-"}")
        Text("Email: ${s.userEmail ?: "-"}")
        Text("Role: ${s.role ?: "-"}")
    }
}
