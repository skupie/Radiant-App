package com.radiant.sms.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.radiant.sms.AppConfig
import com.radiant.sms.data.Repository
import com.radiant.sms.data.TokenStore
import com.radiant.sms.network.NetworkModule
import com.radiant.sms.util.DownloadHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(nav: NavController, vm: AdminMembersViewModel = viewModel()) {
    val s by vm.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    val token: String? = remember { TokenStore(context).getTokenSync() }
    val repo = remember { Repository(NetworkModule.api(context)) }

    val screenBg = Color(0xFFF6F7FB)
    val cardBg = Color.White
    val subtleText = Color(0xFF6B7280)
    val cardShape = RoundedCornerShape(22.dp)
    val chipShape = RoundedCornerShape(999.dp)

    var confirmDeleteId by remember { mutableStateOf<Long?>(null) }
    var confirmDeleteName by remember { mutableStateOf("") }

    // ✅ auto-search (no search button)
    LaunchedEffect(s.query) {
        delay(300)
        vm.loadMembers()
    }

    // ✅ Title beside hamburger (TopAppBar)
    AdminScaffold(
        nav = nav,
        title = "Dashboard",
        hideTitle = false,
        showHamburger = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBg)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val t = token ?: return@Button
                                val url = AppConfig.BASE_URL.trimEnd('/') + "/api/admin/members/export/pdf"
                                DownloadHelper.downloadWithAuth(
                                    context = context,
                                    url = url,
                                    token = t,
                                    fileName = "members-summary.pdf",
                                    mimeType = "application/pdf"
                                )
                                Toast.makeText(context, "Downloading PDF...", Toast.LENGTH_SHORT).show()
                            }
                        ) { Text("All Members\nPDF") }

                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val t = token ?: return@Button
                                val url = AppConfig.BASE_URL.trimEnd('/') + "/api/admin/members/export/excel"
                                DownloadHelper.downloadWithAuth(
                                    context = context,
                                    url = url,
                                    token = t,
                                    fileName = "members-summary.xlsx",
                                    mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                )
                                Toast.makeText(context, "Downloading Excel...", Toast.LENGTH_SHORT).show()
                            }
                        ) { Text("All Members\nExcel") }
                    }

                    Spacer(Modifier.height(10.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { nav.navigate("admin_create_member") }
                    ) { Text("Create Member") }
                }
            }

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = s.query,
                        onValueChange = { vm.setQuery(it) },
                        label = { Text("Search members") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("Updates automatically", style = MaterialTheme.typography.bodySmall, color = subtleText)
                }
            }

            Spacer(Modifier.height(12.dp))

            if (s.isLoading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
            }

            s.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(s.members) { m ->
                    val memberId = m.id ?: 0L
                    val name = m.fullName ?: "Member"
                    val email = m.email ?: "-"
                    val share = m.share ?: 0
                    val deposits = m.depositsCount ?: 0
                    val totalDeposited = m.totalDeposited ?: 0.0

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = cardShape,
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            Modifier
                                .clickable { nav.navigate("admin_member_details/$memberId") }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = subtleText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(Modifier.width(8.dp))

                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                                            shape = chipShape
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "BDT ${formatMoney(totalDeposited)}",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        confirmDeleteId = memberId
                                        confirmDeleteName = name
                                    }
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Share", style = MaterialTheme.typography.labelSmall, color = subtleText)
                                    Text(share.toString(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Deposits", style = MaterialTheme.typography.labelSmall, color = subtleText)
                                    Text(deposits.toString(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }

            if (confirmDeleteId != null) {
                AlertDialog(
                    onDismissRequest = { confirmDeleteId = null },
                    title = { Text("Delete member?") },
                    text = { Text("This will permanently delete “$confirmDeleteName”.") },
                    confirmButton = {
                        TextButton(onClick = {
                            val id = confirmDeleteId ?: return@TextButton
                            confirmDeleteId = null
                            scope.launch {
                                try {
                                    repo.adminDeleteMember(id)
                                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                                    vm.loadMembers()
                                } catch (e: Exception) {
                                    Toast.makeText(context, e.message ?: "Delete failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmDeleteId = null }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}

private fun formatMoney(value: Double): String {
    return String.format(Locale.getDefault(), "%.2f", value)
}
