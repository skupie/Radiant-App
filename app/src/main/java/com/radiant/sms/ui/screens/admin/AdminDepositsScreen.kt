package com.radiant.sms.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import com.radiant.sms.network.AnyJson
import com.radiant.sms.network.NetworkModule
import kotlinx.coroutines.launch
import java.util.Locale

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

    // Pickers
    var showMemberPicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }

    // Data for pickers
    var members by remember { mutableStateOf<List<Pair<Long, String>>>(emptyList()) }
    var availableYears by remember { mutableStateOf<List<Int>>(emptyList()) }

    // List + pagination + total
    var items by remember { mutableStateOf<List<AdminDepositItem>>(emptyList()) }
    var totalDeposits by remember { mutableStateOf(0.0) }
    var page by remember { mutableLongStateOf(1L) }
    var lastPage by remember { mutableLongStateOf(1L) }

    // Delete dialog
    var deleteId by remember { mutableStateOf<Long?>(null) }

    // Add/Edit Sheet
    var showAddSheet by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<AdminDepositItem?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Add/Edit form state
    var formMemberId by remember { mutableStateOf<Long?>(null) }
    var formMemberName by remember { mutableStateOf("Select member") }
    var formYear by remember { mutableStateOf<Int?>(null) }
    var formMonth by remember { mutableStateOf("Feb") } // default
    var formBaseAmount by remember { mutableStateOf("") }
    var formType by remember { mutableStateOf("cash") }
    var formNotes by remember { mutableStateOf("") }
    var showFormMemberPicker by remember { mutableStateOf(false) }
    var showFormYearPicker by remember { mutableStateOf(false) }
    var showFormMonthPicker by remember { mutableStateOf(false) }

    fun monthShortNameFromNumber(num: Int): String {
        return when (num) {
            1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"; 5 -> "May"; 6 -> "Jun"
            7 -> "Jul"; 8 -> "Aug"; 9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
            else -> "—"
        }
    }

    fun displayMonthYear(monthRaw: String?, year: Int?): String {
        val y = year?.toString()?.trim().orEmpty()
        val m = monthRaw?.trim().orEmpty()

        // If backend sends "2" or "02"
        val asInt = m.toIntOrNull()
        val monthLabel = when {
            asInt != null -> monthShortNameFromNumber(asInt)
            m.length >= 3 -> m.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            else -> "—"
        }

        return if (y.isNotBlank()) "$monthLabel $y" else monthLabel
    }

    fun safeMemberName(d: AdminDepositItem): String {
        val name = d.member?.name?.trim()
        if (!name.isNullOrEmpty()) return name
        val id = d.member?.id
        return if (id != null && id > 0) "Member #$id" else "Member"
    }

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

    fun resetFiltersAndReload() {
        search = ""
        selectedMemberId = null
        selectedMemberName = "All members"
        selectedYear = null
        load(1)
    }

    // Load members + initial page
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
                members = listOf(0L to "All members")
            }
        }
        load(1)
    }

    // IMPORTANT: show scaffold title so your own header isn't hidden
    AdminScaffold(
        nav = nav,
        hideTitle = false,
        showHamburger = true
    ) {
        // Give spacing so content doesn't sit under the top bar
        Spacer(Modifier.height(8.dp))

        // Header card (always visible)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Deposits", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Total Deposit: BDT %.2f".format(totalDeposits),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(onClick = {
                        // open add sheet with defaults
                        showAddSheet = true
                        editItem = null
                        formMemberId = null
                        formMemberName = "Select member"
                        formYear = selectedYear
                        formMonth = "Feb"
                        formBaseAmount = ""
                        formType = "cash"
                        formNotes = ""
                    }) {
                        Text("Add Deposit")
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Filters card
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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

                Spacer(Modifier.height(10.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { resetFiltersAndReload() }
                    ) { Text("Reset") }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { load(1) }
                    ) { Text("Apply") }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Divider()
        Spacer(Modifier.height(12.dp))

        if (loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
        }

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(10.dp))
        }

        // Deposit list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items) { d ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            safeMemberName(d),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            displayMonthYear(d.month, d.year),
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(Modifier.height(10.dp))

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
                                onClick = {
                                    // Pre-fill edit form
                                    editItem = d
                                    showAddSheet = false
                                    formMemberId = d.member?.id
                                    formMemberName = d.member?.name ?: "Member"
                                    formYear = d.year
                                    formMonth = displayMonthYear(d.month, null).substringBefore(" ").ifBlank { "Feb" }
                                    formBaseAmount = (d.baseAmount ?: 0.0).toString()
                                    formType = d.type ?: "cash"
                                    formNotes = ""
                                }
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

        // Member picker (filters)
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
                                        if (m.first == 0L) {
                                            selectedMemberId = null
                                            selectedMemberName = "All members"
                                        } else {
                                            selectedMemberId = m.first
                                            selectedMemberName = m.second
                                        }
                                    }
                                    .padding(vertical = 10.dp)
                            )
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showMemberPicker = false }) { Text("Close") } }
            )
        }

        // Year picker (filters)
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
                                    }
                                    .padding(vertical = 10.dp)
                            )
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showYearPicker = false }) { Text("Close") } }
            )
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

        // Add sheet
        if (showAddSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddSheet = false },
                sheetState = sheetState
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Add Deposit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showFormMemberPicker = true }
                    ) { Text(formMemberName) }

                    Spacer(Modifier.height(10.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = { showFormMonthPicker = true }
                        ) { Text(formMonth) }

                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = { showFormYearPicker = true }
                        ) { Text(formYear?.toString() ?: "Year") }
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = formBaseAmount,
                        onValueChange = { formBaseAmount = it },
                        label = { Text("Base amount") },
                        singleLine = true
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = formType,
                        onValueChange = { formType = it },
                        label = { Text("Type (cash/bank/etc)") },
                        singleLine = true
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = formNotes,
                        onValueChange = { formNotes = it },
                        label = { Text("Notes (optional)") }
                    )

                    Spacer(Modifier.height(14.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val memberId = formMemberId
                            val year = formYear
                            val base = formBaseAmount.toDoubleOrNull()

                            if (memberId == null) {
                                Toast.makeText(context, "Select member", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (year == null) {
                                Toast.makeText(context, "Select year", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (base == null) {
                                Toast.makeText(context, "Enter valid base amount", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val monthNumber = when (formMonth.lowercase(Locale.getDefault())) {
                                "jan" -> 1; "feb" -> 2; "mar" -> 3; "apr" -> 4; "may" -> 5; "jun" -> 6
                                "jul" -> 7; "aug" -> 8; "sep" -> 9; "oct" -> 10; "nov" -> 11; "dec" -> 12
                                else -> 2
                            }

                            // Backend often accepts month as number or name.
                            // Use number string to avoid "2 2026" display issues.
                            val body: AnyJson = mapOf(
                                "member_id" to memberId,
                                "year" to year,
                                "month" to monthNumber.toString(),
                                "base_amount" to base,
                                "type" to formType,
                                "notes" to formNotes.takeIf { it.isNotBlank() }
                            )

                            scope.launch {
                                try {
                                    repo.adminCreateDeposit(body)
                                    Toast.makeText(context, "Deposit added", Toast.LENGTH_SHORT).show()
                                    showAddSheet = false
                                    load(1)
                                } catch (e: Exception) {
                                    Toast.makeText(context, e.message ?: "Failed to add", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) { Text("Save") }

                    Spacer(Modifier.height(24.dp))
                }

                // Form member picker
                if (showFormMemberPicker) {
                    AlertDialog(
                        onDismissRequest = { showFormMemberPicker = false },
                        title = { Text("Select member") },
                        text = {
                            LazyColumn {
                                items(members.filter { it.first != 0L }) { m ->
                                    Text(
                                        text = m.second,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                formMemberId = m.first
                                                formMemberName = m.second
                                                showFormMemberPicker = false
                                            }
                                            .padding(vertical = 10.dp)
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showFormMemberPicker = false }) { Text("Close") }
                        }
                    )
                }

                // Form year picker
                if (showFormYearPicker) {
                    AlertDialog(
                        onDismissRequest = { showFormYearPicker = false },
                        title = { Text("Select year") },
                        text = {
                            LazyColumn {
                                items(availableYears.ifEmpty { listOf(2026) }) { y ->
                                    Text(
                                        y.toString(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                formYear = y
                                                showFormYearPicker = false
                                            }
                                            .padding(vertical = 10.dp)
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showFormYearPicker = false }) { Text("Close") }
                        }
                    )
                }

                // Form month picker
                if (showFormMonthPicker) {
                    val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
                    AlertDialog(
                        onDismissRequest = { showFormMonthPicker = false },
                        title = { Text("Select month") },
                        text = {
                            LazyColumn {
                                items(months) { m ->
                                    Text(
                                        m,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                formMonth = m
                                                showFormMonthPicker = false
                                            }
                                            .padding(vertical = 10.dp)
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showFormMonthPicker = false }) { Text("Close") }
                        }
                    )
                }
            }
        }

        // Edit sheet placeholder (you said add option missing; edit can be expanded next)
        if (editItem != null) {
            ModalBottomSheet(
                onDismissRequest = { editItem = null },
                sheetState = sheetState
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Edit Deposit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    Text("Edit form can be enabled next (same as Add).")
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
