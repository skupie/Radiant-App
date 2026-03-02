package com.radiant.sms.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.radiant.sms.data.Repository
import com.radiant.sms.network.AdminMemberDto
import com.radiant.sms.network.NetworkModule

@Composable
fun AdminPanelScreen(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val api = remember { NetworkModule.api(context) }
    val repo = remember { Repository(api) }

    var search by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var members by remember { mutableStateOf<List<AdminMemberDto>>(emptyList()) }

    LaunchedEffect(Unit) {
        loading = true
        try {
            val res = repo.adminMembers(perPage = 50)
            members = res.data
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

        Text("Admin Panel", style = MaterialTheme.typography.headlineSmall)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.weight(1f),
                label = { Text("Search members") },
                singleLine = true
            )

            Button(
                enabled = !loading,
                onClick = {
                    loading = true
                }
            ) {
                Text("Search")
            }
        }

        LaunchedEffect(loading) {
            if (!loading) return@LaunchedEffect
            try {
                val res = repo.adminMembers(
                    search = search.takeIf { it.isNotBlank() },
                    perPage = 50
                )
                members = res.data
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }

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
                Text(
                    "Members (${members.size})",
                    style = MaterialTheme.typography.titleMedium
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(members) { m ->
                        MemberRow(m)
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberRow(m: AdminMemberDto) {
    val name = m.fullName?.takeIf { it.isNotBlank() } ?: "Unnamed member"
    val email = m.email?.takeIf { it.isNotBlank() } ?: "No email"

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Text(name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(email, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
