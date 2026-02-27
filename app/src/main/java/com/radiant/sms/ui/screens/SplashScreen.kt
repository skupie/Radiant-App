package com.radiant.sms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.radiant.sms.ui.Routes
import com.radiant.sms.ui.viewmodel.AuthViewModel

@Composable
fun SplashScreen(nav: NavController, vm: AuthViewModel = viewModel()) {
    val state by vm.state.collectAsState()

    LaunchedEffect(state.isLoading, state.tokenPresent, state.role) {
        if (!state.isLoading) {
            if (!state.tokenPresent) {
                nav.navigate(Routes.LOGIN) { popUpTo(Routes.SPLASH) { inclusive = true } }
            } else {
                val role = (state.role ?: "").lowercase()
                if (role == "admin") {
                    nav.navigate(Routes.ADMIN_HOME) { popUpTo(Routes.SPLASH) { inclusive = true } }
                } else {
                    nav.navigate(Routes.MEMBER_HOME) { popUpTo(Routes.SPLASH) { inclusive = true } }
                }
            }
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text("Loading…")
        }
    }
}
