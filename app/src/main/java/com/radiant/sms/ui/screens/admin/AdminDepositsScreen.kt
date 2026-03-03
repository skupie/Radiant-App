package com.radiant.sms.ui.screens.admin

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
import androidx.navigation.NavController
import com.radiant.sms.data.Repository
import com.radiant.sms.network.AdminDepositItem
import com.radiant.sms.network.NetworkModule
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDepositsScreen(nav: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { Repository(NetworkModule.api(context)) }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var selectedMemberId by remember { mutableStateOf<Long?>(null) }
    var selectedMemberName by remember { mutableStateOf("All members") }
    var selectedYear by remember { mutableStateOf<Int?>(null) }

    var showMemberPicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }

    var members by remember { mutableStateOf<List<Pair<Long, String>>>(emptyList()) }
    var availableYears by remember { mutableStateOf<List<Int>>(emptyList()) }

    var items by remember { mutableStateOf<List<AdminDepositItem>>(emptyList()) }
    var totalDeposits by remember { mutableStateOf(0.0) }

    fun formatDepositedAt(raw: String?): String {
        if (raw.isNullOrBlank()) return "-"
        val outFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        return try {
            OffsetDateTime.parse(raw).toLocalDateTime().format(outFmt)
        } catch (_: Exception) {
            try {
                LocalDateTime.parse(raw).format(outFmt)
            } catch (_: Exception) {
                raw
            }
        }
    }

    fun load() {
        scope.launch {
            loading = true
            try {
                val resp = repo.adminDepositsList(
                    search = null,
                    memberId = selectedMemberId,
                    year = selectedYear,
                    perPage = 20,
                    page = 1
                )
                items = resp.data
                totalDeposits = resp.summary?.filteredTotal ?: 0.0
                availableYears = resp.summary?.availableYears ?: emptyList()
            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val all = repo.adminMembersAll(null)
                members = listOf(0L to "All members") + all.mapNotNull {
                    val id = it.id ?: return@mapNotNull null
                    id to (it.fullName ?: "Member $id")
                }
            } catch (_: Exception) {}
        }
        load()
    }

    AdminScaffold(
        nav = nav,
        hideTitle = true, // we handle our own title
        showHamburger = true
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {

            Spacer(Modifier.height(12.dp))

            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Column(Modifier.padding(16.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Deposits",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Total Deposit: BDT %.2f".format(totalDeposits),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Button(onClick = { /* existing add sheet logic */ }) {
                            Text("Add Deposit")
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Filter Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Column(Modifier.padding(16.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = { showMemberPicker = true }
                        ) {
                            Text(selectedMemberName)
                        }

                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = { showYearPicker = true }
                        ) {
                            Text(selectedYear?.toString() ?: "All years")
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            if (loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items) { d ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(Modifier.padding(16.dp)) {

                            Text(
                                d.member?.name ?: d.memberName ?: "Member",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                "${d.month} ${d.year}",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Base: ${d.baseAmount}")
                                Text("Total: ${d.totalAmount}")
                            }

                            Spacer(Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Type: ${d.type}")
                                Text("Deposited: ${formatDepositedAt(d.depositedAt)}")
                            }

                            Spacer(Modifier.height(12.dp))

                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { /* existing delete logic */ }
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }

    // Member Picker
    if (showMemberPicker) {
        AlertDialog(
            onDismissRequest = { showMemberPicker = false },
            title = { Text("Select member") },
            text = {
                LazyColumn {
                    items(members) { m ->
                        Text(
                            text = m.second,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showMemberPicker = false
                                    selectedMemberId =
                                        if (m.first == 0L) null else m.first
                                    selectedMemberName = m.second
                                    load()
                                }
                                .padding(vertical = 10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMemberPicker = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Year Picker
    if (showYearPicker) {
        AlertDialog(
            onDismissRequest = { showYearPicker = false },
            title = { Text("Select year") },
            text = {
                LazyColumn {
                    item {
                        Text(
                            "All years",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedYear = null
                                    showYearPicker = false
                                    load()
                                }
                                .padding(vertical = 10.dp)
                        )
                    }
                    items(availableYears) { y ->
                        Text(
                            y.toString(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedYear = y
                                    showYearPicker = false
                                    load()
                                }
                                .padding(vertical = 10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showYearPicker = false }) {
                    Text("Close")
                }
            }
        )
    }
}
