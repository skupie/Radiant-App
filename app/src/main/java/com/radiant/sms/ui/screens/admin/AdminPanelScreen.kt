package com.radiant.sms.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.radiant.sms.data.Repository
import com.radiant.sms.network.AdminMemberDto
import com.radiant.sms.network.NetworkModule

@Composable
fun AdminPanelScreen(
    modifier: Modifier = Modifier,
    onMemberClick: (Int) -> Unit,
    onCreateMemberClick: () -> Unit,
    onExportPdfClick: () -> Unit,
    onExportExcelClick: () -> Unit
) {

    val context = LocalContext.current
    val api = remember { NetworkModule.api(context) }
    val repo = remember { Repository(api) }

    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var members by remember { mutableStateOf<List<AdminMemberDto>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val res = repo.adminMembers(perPage = 1000)
            members = res.data
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Admin Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = onCreateMemberClick
            ) {
                Text("Create Member")
            }

            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onExportPdfClick
            ) {
                Text("PDF")
            }

            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onExportExcelClick
            ) {
                Text("Excel")
            }
        }

        Spacer(Modifier.height(20.dp))

        when {
            loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
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
                    text = "Total Members: ${members.size}",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(members) { member ->
                        MemberCard(
                            member = member,
                            onClick = {
                                member.id?.let { id ->
                                    onMemberClick(id.toInt()) // ✅ FIX HERE
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberCard(
    member: AdminMemberDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(14.dp)) {

            Text(
                text = member.fullName ?: "Unnamed Member",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = member.email ?: "",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "NID: ${member.nid ?: "-"}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
