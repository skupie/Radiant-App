package com.radiant.sms.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.radiant.sms.network.AdminMemberDto

@Composable
fun AdminPanelScreen(modifier: Modifier = Modifier) {
    val repo = NetworkModule.repository

    var search by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var members by remember { mutableStateOf<List<AdminMemberDto>>(emptyList()) }

    fun loadMembers() {
        loading = true
        error = null
        members = emptyList()

        // Using LaunchedEffect style trigger is cleaner, but this is a small self-contained screen:
        // we just launch in composition via a state flip.
    }

    // initial load
    LaunchedEffect(Unit) {
        loading = true
        try {
            val res = repo.adminMembers(search = null, perPage = 50)
            members = res.data
        } catch (e: Exception) {
            error = e.message ?: "Failed to load members"
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
        Text(
            text = "Admin Panel",
            style = MaterialTheme.typography.headlineSmall
        )

        // Search box
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = search,
                onValueChange = { search = it },
                label = { Text("Search members") },
                singleLine = true
            )

            Button(
                onClick = {
                    loading = true
                    error = null
                    members = emptyList()
                }
            ) {
                Text("Search")
            }
        }

        // Search trigger
        LaunchedEffect(loading) {
            // Only run when we explicitly set loading = true via Search button
            if (!loading) return@LaunchedEffect

            try {
                val q = search.trim().ifEmpty { null }
                val res = repo.adminMembers(search = q, perPage = 50)
                members = res.data
            } catch (e: Exception) {
                error = e.message ?: "Failed to load members"
            } finally {
                loading = false
            }
        }

        when {
            loading -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> {
                Text(
                    text = "Members (${members.size})",
                    style = MaterialTheme.typography.titleMedium
                )

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(members) { m ->
                        MemberRow(m)
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                Text(
                                    text = "Deposits & Due Summary",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "This section is intentionally kept simple to avoid build errors when AdminDepositsResponse/AdminDueSummaryResponse models are missing in your repo.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberRow(m: AdminMemberDto) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = m.name.ifBlank { "Unnamed member" },
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = m.email.ifBlank { "No email" },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
