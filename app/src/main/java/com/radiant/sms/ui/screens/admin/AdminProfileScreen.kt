package com.radiant.sms.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.radiant.sms.data.Repository
import com.radiant.sms.network.NetworkModule

@Composable
fun AdminProfileScreen(
    modifier: Modifier = Modifier,
    onLogout: (() -> Unit)? = null
) {

    val context = LocalContext.current
    val api = remember { NetworkModule.api(context) }
    val repo = remember { Repository(api) }

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(loading) {
        if (!loading) return@LaunchedEffect
        try {
            val me = repo.me()
            name = me.user.name
            email = me.user.email
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text("Admin Profile", style = MaterialTheme.typography.headlineSmall)

        when {
            loading -> {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Text(error ?: "", color = MaterialTheme.colorScheme.error)
            }

            else -> {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            name?.takeIf { it.isNotBlank() } ?: "Admin",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            email?.takeIf { it.isNotBlank() } ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(onClick = { loading = true }) {
                Text("Reload")
            }

            if (onLogout != null) {
                Button(onClick = onLogout) {
                    Text("Logout")
                }
            }
        }
    }
}
