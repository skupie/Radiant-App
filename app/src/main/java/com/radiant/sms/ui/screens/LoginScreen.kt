package com.radiant.sms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.radiant.sms.ui.Routes
import com.radiant.sms.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(nav: NavController, vm: AuthViewModel = viewModel()) {
    val s by vm.state.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // When login succeeds -> route based on role
    LaunchedEffect(s.tokenPresent, s.role) {
        if (s.tokenPresent) {
            val destination = when (s.role?.lowercase()) {
                "admin" -> Routes.ADMIN_HOME
                else -> Routes.MEMBER_SHARE_DETAILS // ✅ member goes here now
            }

            nav.navigate(destination) {
                popUpTo(Routes.LOGIN) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    ScreenScaffold(title = "Login", nav = nav) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            if (!s.error.isNullOrBlank()) {
                Text(
                    text = s.error ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = { vm.login(username, password) },
                enabled = !s.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (s.isLoading) "Logging in..." else "Login")
            }
        }
    }
}
