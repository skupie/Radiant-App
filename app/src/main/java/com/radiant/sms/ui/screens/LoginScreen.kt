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
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val s by vm.state.collectAsState()

    // Navigate away once login succeeds (token persisted)
    LaunchedEffect(s.isLoading, s.tokenPresent, s.role) {
        if (!s.isLoading && s.tokenPresent) {
            val destination = when (s.role?.lowercase()) {
                "admin" -> Routes.ADMIN_HOME
                else -> Routes.MEMBER_SHARE_DETAILS
            }

            nav.navigate(destination) {
                popUpTo(Routes.LOGIN) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        if (!s.error.isNullOrBlank()) {
            Text(
                text = s.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = {
                // ✅ Use AuthViewModel so token is saved to TokenStore.
                vm.login(email.trim(), password)
            },
            enabled = !s.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (s.isLoading) "Logging in..." else "Login")
        }
    }
}
