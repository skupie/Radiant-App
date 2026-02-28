package com.radiant.sms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.radiant.sms.ui.Routes
import com.radiant.sms.ui.viewmodel.AuthViewModel

@Composable
fun SplashScreen(nav: NavController, vm: AuthViewModel = viewModel()) {
    val s by vm.state.collectAsState()

    // Decide where to go
    LaunchedEffect(s.isLoading, s.tokenPresent, s.role) {
        if (!s.isLoading) {
            val destination = if (s.tokenPresent) {
                when (s.role?.lowercase()) {
                    "admin" -> Routes.ADMIN_HOME
                    else -> Routes.MEMBER_SHARE_DETAILS // ✅ member goes here now
                }
            } else {
                Routes.LOGIN
            }

            nav.navigate(destination) {
                popUpTo(Routes.SPLASH) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
