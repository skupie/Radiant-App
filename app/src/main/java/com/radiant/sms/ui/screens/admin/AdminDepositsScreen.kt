package com.radiant.sms.ui.screens.admin

import android.widget.Toast
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.data.Repository
import com.radiant.sms.network.AdminDepositItem
import com.radiant.sms.network.NetworkModule
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDepositsScreen(nav: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val repo = remember { Repository(NetworkModule.api(context)) }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Filters
    var search by remember { mutableStateOf("") }
    var selectedMemberId by remember { mutableStateOf<Long?>(null) }
    var selectedMemberName by remember { mutableStateOf("All members") }
    var selectedYear by remember { mutableStateOf<Int?>(null) }

    // Dropdown data
    var members by remember { mutableStateOf<List<Pair<Long, String>>>(emptyList()) }
    var availableYears by remember { mutableStateOf<List<Int>>(emptyList()) }

    // List + pagination + total
    var items by remember { mutableStateOf<List<AdminDepositItem>>(emptyList()) }
    var totalDeposits by remember { mutableStateOf(0.0) }
    var page by remember { mutableLongStateOf(1L) }
    var lastPage by remember { mutableLongStateOf(1L) }

    // Delete dialog
    var deleteId by remember { mutableStateOf<Long?>(null) }

    // Edit sheet placeholder
    var editItem by remember { mutableStateOf<AdminDepositItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun load(pageToLoad: Int = page.toInt()) {
        scope.launch {
            loading = true
            error = null
            try {
                val resp = repo.adminDepositsList(
                    search = search.takeIf { it.isNotBlank() },
                    memberId = selectedMemberId,
                    year = selectedYear,
                    perPage = 10,
                    page = pageToLoad
                )
                items = resp.data
                totalDeposits = resp.summary?.filteredTotal ?: 0.0
                availableYears = resp.summary?.availableYears ?: emptyList()
                val cp = resp.meta?.currentPage ?: pageToLoad
                val lp = resp.meta?.lastPage ?: 1
                page = cp.toLong()
                lastPage = lp.toLong()
            } catch (e: Exception) {
                error = e.message ?: "Failed to load deposits"
            } finally {
                loading = false
            }
        }
    }

    // Load members once + first load
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val all = repo.adminMembersAll(search = null)
                members = listOf(0L to "All members") + all.mapNotNull {
                    val id = it.id ?: return@mapNotNull null
                    val name = it.fullName ?: "Member $id"
                    id to name
                }
            } catch (_: Exception) {
                // keep screen working even if members fail
            }
        }
        load(1)
    }

    AdminScaffold(nav = nav, hideTitle = true, showHamburger = true) {

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Deposits",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text("Total Deposits: BDT %.2f".format(totalDeposits))
        }

        Spacer(Modifier.height(12.dp))
        Divider()
        Spacer(Modifier.height(12.dp))

        // Filters card (mobile)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("Search") },
                    singleLine = true
                )

                Spacer(Modifier.height(10.dp))

                Text("Member / Year", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))

                // Simple “cycle” selectors = functional + stable across compose versions
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val current = selectedMemberId ?: 0L
                            val idx = members.indexOfFirst { it.first == current }
                            val nextIndex = if (idx == -1 || idx == members.lastIndex) 0 else idx + 1
                            val pair = members.getOrNull(nextIndex) ?: (0L to "All members")
                            selectedMemberId = if (pair.first == 0L) null else pair.first
                            selectedMemberName = pair.second
                        }
                    ) {
                        Text(selectedMemberName)
                    }

                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (availableYears.isEmpty()) return@OutlinedButton
                            val idx = availableYears.indexOf(selectedYear)
                            selectedYear = if (idx == -1) availableYears.first()
                            else availableYears.getOrNull(idx + 1)
                        }
                    ) {
                        Text(selectedYear?.toString() ?: "All years")
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            search = ""
                            selectedMemberId = null
                            selectedMemberName = "All members"
                            selectedYear = null
                            load(1)
                        }
                    ) { Text("Reset") }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { load(1) }
                    ) { Text("Apply") }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
        }

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(10.dp))
        }

        // List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items) { d ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(d.member?.name ?: "-", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text("${d.month ?: "-"} ${d.year ?: ""}", style = MaterialTheme.typography.bodySmall)

                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Base: ${d.baseAmount ?: 0.0}")
                            Text("Total: ${d.totalAmount ?: 0.0}")
                        }

                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Type: ${d.type ?: "-"}", style = MaterialTheme.typography.bodySmall)
                            Text(d.loggedAt ?: "-", style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(Modifier.height(10.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = { editItem = d }
                            ) { Text("Edit") }

                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = { deleteId = d.id }
                            ) { Text("Delete") }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        enabled = page > 1,
                        onClick = { load((page - 1).toInt()) }
                    ) { Text("Prev") }

                    Text("Page $page of $lastPage")

                    OutlinedButton(
                        enabled = page < lastPage,
                        onClick = { load((page + 1).toInt()) }
                    ) { Text("Next") }
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        // Delete confirm
        if (deleteId != null) {
            AlertDialog(
                onDismissRequest = { deleteId = null },
                title = { Text("Delete deposit?") },
                text = { Text("This cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        val id = deleteId ?: return@TextButton
                        deleteId = null
                        scope.launch {
                            try {
                                repo.adminDeleteDeposit(id)
                                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                                load(page.toInt())
                            } catch (e: Exception) {
                                Toast.makeText(context, e.message ?: "Delete failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { deleteId = null }) { Text("Cancel") }
                }
            )
        }

        // Edit sheet (placeholder — safe)
        if (editItem != null) {
            ModalBottomSheet(
                onDismissRequest = { editItem = null },
                sheetState = sheetState
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Edit Deposit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    Text("Edit UI can be expanded next (month/year/type/base/notes).")
                    Spacer(Modifier.height(16.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { editItem = null }
                    ) { Text("Close") }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
