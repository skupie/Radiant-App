package com.radiant.sms.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.radiant.sms.storage.TokenStore
import com.radiant.sms.ui.Routes
import com.radiant.sms.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(nav: NavController, vm: AuthViewModel = viewModel()) {

    val state by vm.state.collectAsState()
    val ctx = LocalContext.current
    val tokenStore = remember { TokenStore(ctx) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    /**
     * ✅ Updated LaunchedEffect
     * - Saves token when available
     * - Navigates based on role
     */
    LaunchedEffect(state.tokenPresent, state.role, state.token) {

        // Save token if available
        val token = state.token
        if (!token.isNullOrBlank()) {
            tokenStore.saveToken(token)
        }

        // Navigate when authenticated
        if (state.tokenPresent) {
            val role = (state.role ?: "").lowercase()

            if (role == "admin") {
                nav.navigate(Routes.ADMIN_HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                nav.navigate(Routes.MEMBER_HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("RadiantSMS Login") })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = { vm.login(email.trim(), password) },
                enabled = !state.isLoading && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Signing in…")
                } else {
                    Text("Login")
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                "If you see 419/401, your host may be stripping Authorization headers. Tell me the exact error response and I’ll help fix it.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
