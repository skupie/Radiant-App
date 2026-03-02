package com.radiant.sms.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.radiant.sms.data.NetworkModule
import com.radiant.sms.network.MeResponse

@Composable
fun AdminProfileScreen(
    modifier: Modifier = Modifier,
    onLogout: (() -> Unit)? = null
) {
    val repo = NetworkModule.repository

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var me by remember { mutableStateOf<MeResponse?>(null) }

    fun load() {
        loading = true
        error = null
        me = null
    }

    LaunchedEffect(Unit) {
        try {
            me = repo.me()
        } catch (e: Exception) {
            error = e.message ?: "Failed to load profile"
        } finally {
            loading = false
        }
    }

    // manual reload trigger
    LaunchedEffect(loading) {
        if (!loading) return@LaunchedEffect
        try {
            me = repo.me()
        } catch (e: Exception) {
            error = e.message ?: "Failed to load profile"
        } finally {
            loading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
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
                Button(onClick = { load() }) { Text("Retry") }
            }

            else -> {
                val user = me?.user
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Name: ${user?.name ?: "-"}")
                        Text(text = "Email: ${user?.email ?: "-"}")
                        Text(text = "Role: ${user?.role ?: "-"}")
                    }
                }

                if (onLogout != null) {
                    Button(onClick = onLogout) { Text("Logout") }
                }
            }
        }
    }
}
