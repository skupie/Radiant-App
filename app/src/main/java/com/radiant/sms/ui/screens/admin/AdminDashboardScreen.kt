package com.radiant.sms.ui.screens.admin

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.radiant.sms.AppConfig
import com.radiant.sms.data.TokenStore
import com.radiant.sms.network.AdminMemberDto
import com.radiant.sms.util.DownloadHelper

private fun adminMemberDetailsRoute(m: AdminMemberDto): String {
    return "admin_member_details" +
        "?id=${m.id ?: 0}" +
        "&name=${Uri.encode(m.fullName ?: "-")}" +
        "&email=${Uri.encode(m.email ?: "-")}" +
        "&nid=${Uri.encode(m.nid ?: "-")}" +
        "&share=${m.share ?: 0}" +
        "&deposits=${m.depositsCount ?: 0}" +
        "&total=${m.totalDeposited ?: 0.0}"
}

@Composable
fun AdminDashboardScreen(nav: NavController, vm: AdminMembersViewModel = viewModel()) {
    val s by vm.state.collectAsState()
    val context = LocalContext.current
    val token = remember { TokenStore(context).getTokenSync() }

    AdminScaffold(nav = nav, hideTitle = true, showHamburger = true) {

        Spacer(Modifier.height(12.dp))
        Text("Dashboard", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    if (token.isNullOrBlank()) return@Button
                    val url = AppConfig.BASE_URL.trimEnd('/') + "/api/admin/members/export/pdf"
                    DownloadHelper.downloadWithAuth(
                        context = context,
                        url = url,
                        token = token,
                        fileName = "members-summary.pdf",
                        mimeType = "application/pdf"
                    )
                    Toast.makeText(context, "Downloading PDF...", Toast.LENGTH_SHORT).show()
                }
            ) { Text("All Members PDF") }

            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    if (token.isNullOrBlank()) return@Button
                    val url = AppConfig.BASE_URL.trimEnd('/') + "/api/admin/members/export/excel"
                    DownloadHelper.downloadWithAuth(
                        context = context,
                        url = url,
                        token = token,
                        fileName = "members-summary.xlsx",
                        mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    )
                    Toast.makeText(context, "Downloading Excel...", Toast.LENGTH_SHORT).show()
                }
            ) { Text("All Members Excel") }
        }

        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = s.query,
                onValueChange = { vm.setQuery(it) },
                label = { Text("Search members") },
                singleLine = true
            )
            Button(
                modifier = Modifier.height(56.dp),
                onClick = { vm.loadMembers() }
            ) { Text("Search") }
        }

        Spacer(Modifier.height(12.dp))

        if (s.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
        }

        s.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Member", fontWeight = FontWeight.Bold)
                Text("Share", fontWeight = FontWeight.Bold)
                Text("Deposits", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(s.members) { m ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {

                        // ✅ Clicking on member NAME opens details
                        Text(
                            text = m.fullName ?: "-",
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { nav.navigate(adminMemberDetailsRoute(m)) }
                        )

                        Spacer(Modifier.height(2.dp))
                        Text(m.email ?: "-", style = MaterialTheme.typography.bodySmall)

                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Share: ${m.share ?: 0}")
                            Text("Deposits: ${m.depositsCount ?: 0}")
                            Text("৳ ${m.totalDeposited ?: 0.0}")
                        }
                    }
                }
            }
        }
    }
}
