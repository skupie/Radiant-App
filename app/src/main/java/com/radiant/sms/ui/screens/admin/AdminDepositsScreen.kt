package com.radiant.sms.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.data.Repository
import com.radiant.sms.network.AdminDepositItem
import com.radiant.sms.network.AdminDepositUpsertRequest
import com.radiant.sms.network.NetworkModule
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDepositsScreen(nav: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { Repository(NetworkModule.api(context)) }

    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }

    // ---- Financial-app style colors ----
    val screenBg = Color(0xFFF6F7FB)
    val cardBg = Color.White
    val subtleText = Color(0xFF6B7280)

    // ---- UI State ----
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Filters
    var selectedMemberId by remember { mutableStateOf<Long?>(null) }
    var selectedMemberName by remember { mutableStateOf("All members") }
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var showMemberPicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }

    // Members + years
    var members by remember { mutableStateOf<List<Pair<Long, String>>>(emptyList()) }
    var availableYears by remember { mutableStateOf<List<Int>>(emptyList()) }
    val memberNameMap = remember(members) {
        members.filter { it.first != 0L }.associate { it.first to it.second }
    }

    // List + pagination + totals
    var items by remember { mutableStateOf<List<AdminDepositItem>>(emptyList()) }
    var totalDeposits by remember { mutableStateOf(0.0) }
    var page by remember { mutableLongStateOf(1L) }
    var lastPage by remember { mutableLongStateOf(1L) }

    // Delete dialog
    var deleteId by remember { mutableStateOf<Long?>(null) }

    // Add/Edit sheet
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Edit mode
    var editingDepositId by remember { mutableStateOf<Long?>(null) }

    // Form
    var formMemberId by remember { mutableStateOf<Long?>(null) }
    var formMemberName by remember { mutableStateOf("Select member") }
    var formYear by remember { mutableStateOf<Int?>(null) }
    var formMonth by remember { mutableStateOf("Feb") } // UI label
    var formBaseAmount by remember { mutableStateOf("") }
    var formType by remember { mutableStateOf("Cash") } // UI label
    var formNotes by remember { mutableStateOf("") }
    var formDepositedAt by remember { mutableStateOf("") }

    var showFormMemberPicker by remember { mutableStateOf(false) }
    var showFormYearPicker by remember { mutableStateOf(false) }
    var showFormMonthPicker by remember { mutableStateOf(false) }
    var showFormTypePicker by remember { mutableStateOf(false) }

    fun nowTimestamp(): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return fmt.format(Date())
    }

    fun monthShortNameFromNumber(num: Int): String {
        return when (num) {
            1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"; 5 -> "May"; 6 -> "Jun"
            7 -> "Jul"; 8 -> "Aug"; 9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
            else -> "Feb"
        }
    }

    fun monthNumberFromLabel(label: String): Int {
        return when (label.lowercase(Locale.getDefault()).take(3)) {
            "jan" -> 1; "feb" -> 2; "mar" -> 3; "apr" -> 4; "may" -> 5; "jun" -> 6
            "jul" -> 7; "aug" -> 8; "sep" -> 9; "oct" -> 10; "nov" -> 11; "dec" -> 12
            else -> 2
        }
    }

    fun displayMonthYear(monthRaw: String?, year: Int?): String {
        val y = year?.toString()?.trim().orEmpty()
        val m = monthRaw?.trim().orEmpty()
        val asInt = m.toIntOrNull()
        val monthLabel = when {
            asInt != null -> monthShortNameFromNumber(asInt)
            m.length >= 3 -> m.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
            else -> "—"
        }
        return if (y.isNotBlank()) "$monthLabel $y" else monthLabel
    }

    fun bestMemberName(d: AdminDepositItem): String {
        val nested = d.member?.name?.trim()
        if (!nested.isNullOrEmpty()) return nested

        val full = d.memberFullName?.trim()
        if (!full.isNullOrEmpty()) return full

        val flat = d.memberName?.trim()
        if (!flat.isNullOrEmpty()) return flat

        val name = d.name?.trim()
        if (!name.isNullOrEmpty()) return name

        val id = d.memberId ?: d.member?.id
        if (id != null) {
            val mapped = memberNameMap[id]
            if (!mapped.isNullOrBlank()) return mapped
        }
        return "Member"
    }

    fun bestDepositedAtRaw(d: AdminDepositItem): String {
        return listOf(d.depositedAt, d.loggedAt, d.createdAt)
            .firstOrNull { !it.isNullOrBlank() }
            ?: "-"
    }

    fun formatDepositedAt(raw: String): String {
        if (raw.isBlank() || raw == "-") return "-"
        val outFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault())

        try {
            val odt = OffsetDateTime.parse(raw)
            return odt.toLocalDateTime().format(outFmt)
        } catch (_: Exception) {}

        try {
            val ldt = LocalDateTime.parse(raw)
            return ldt.format(outFmt)
        } catch (_: Exception) {}

        return raw
    }

    fun normalizeTypeLabel(raw: String?): String {
        val t = raw?.trim().orEmpty()
        if (t.isBlank()) return "-"
        return t.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    fun typeUiLabel(raw: String?): String {
        val t = raw?.trim()?.lowercase(Locale.getDefault()).orEmpty()
        return when (t) {
            "cash" -> "Cash"
            "bkash" -> "Bkash"
            "bank" -> "Bank"
            else -> if (t.isNotBlank()) t.replaceFirstChar { it.titlecase(Locale.getDefault()) } else "Cash"
        }
    }

    fun load(pageToLoad: Int = page.toInt()) {
        scope.launch {
            loading = true
            error = null
            try {
                val resp = repo.adminDepositsList(
                    search = null,
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

    fun openCreate() {
        editingDepositId = null
        showSheet = true
        formMemberId = null
        formMemberName = "Select member"
        formYear = selectedYear ?: currentYear
        formMonth = "Feb"
        formBaseAmount = ""
        formType = "Cash"
        formNotes = ""
        formDepositedAt = nowTimestamp()
    }

    fun openEdit(d: AdminDepositItem) {
        val id = d.id ?: return
        editingDepositId = id
        showSheet = true

        val mid = d.memberId ?: d.member?.id
        formMemberId = mid
        formMemberName = if (mid != null) (memberNameMap[mid] ?: bestMemberName(d)) else bestMemberName(d)

        formYear = d.year ?: selectedYear ?: currentYear

        // month could be "2" or "Feb" etc
        val mRaw = d.month?.trim()
        val mInt = mRaw?.toIntOrNull()
        formMonth = when {
            mInt != null -> monthShortNameFromNumber(mInt)
            !mRaw.isNullOrBlank() -> mRaw.take(3).replaceFirstChar { it.titlecase(Locale.getDefault()) }
            else -> "Feb"
        }

        formBaseAmount = (d.baseAmount ?: 0.0).toString()
        formType = typeUiLabel(d.type)

        // Notes might not exist on model — keep safe
        formNotes = ""

        val dep = bestDepositedAtRaw(d)
        formDepositedAt = if (dep == "-" || dep.isBlank()) nowTimestamp() else dep
    }

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

    // year options: include current + next year always
    val yearOptions = remember(availableYears, currentYear) {
        (availableYears + currentYear + (currentYear + 1)).distinct().sorted()
    }

    val cardShape = RoundedCornerShape(22.dp)
    val chipShape = RoundedCornerShape(999.dp)

    @Composable
    fun Chip(
        text: String,
        selected: Boolean,
        enabled: Boolean = true,
        onClick: () -> Unit
    ) {
        val bg = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
        val content = if (selected) Color.White else MaterialTheme.colorScheme.primary
        val border = if (selected) Color.Transparent else MaterialTheme.colorScheme.primary

        Box(
            modifier = Modifier
                .border(1.dp, if (enabled) border else Color(0xFFCBD5E1), chipShape)
                .background(if (enabled) bg else Color(0xFFF1F5F9), chipShape)
                .clickable(enabled = enabled) { onClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = text,
                color = if (enabled) content else Color(0xFF94A3B8),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    fun pageWindow(current: Int, last: Int): List<Int> {
        if (last <= 1) return listOf(1)
        val start = (current - 2).coerceAtLeast(1)
        val end = (current + 2).coerceAtMost(last)
        return (start..end).toList()
    }

    @Composable
    fun TypeBadge(type: String) {
        val label = normalizeTypeLabel(type)
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    shape = chipShape
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }

    @Composable
    fun DeletePill(onClick: () -> Unit) {
        Box(
            modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f), chipShape)
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.06f), chipShape)
                .clickable { onClick() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Delete",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }

    AdminScaffold(
    nav = nav,
    title = "Deposits",
    hideTitle = false,
    showHamburger = true
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
            .padding(horizontal = 16.dp) // ✅ no statusBarsPadding here
    ) {
            Spacer(Modifier.height(12.dp))

            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Deposits",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Total Deposit: BDT %.2f".format(totalDeposits),
                                style = MaterialTheme.typography.bodyMedium,
                                color = subtleText
                            )
                        }

                        Button(onClick = { openCreate() }) { Text("Add Deposit") }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Filters
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = cardShape,
                colors = CardDefaults.cardColors(containerColor = cardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = { showMemberPicker = true }
                        ) {
                            Text(
                                selectedMemberName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
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
                Spacer(Modifier.height(10.dp))
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            Divider(color = Color(0xFFE6E8EF))
            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!loading && items.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = cardShape,
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(Modifier.padding(18.dp)) {
                                Text(
                                    "No deposits found",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Try changing member/year filter or add a new deposit.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = subtleText
                                )
                            }
                        }
                    }
                }

                items(items) { d ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = d.id != null) { openEdit(d) },
                        shape = cardShape,
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {

                            // Top: name (tap works) + delete trailing
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = bestMemberName(d),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                Spacer(Modifier.width(10.dp))

                                DeletePill(onClick = { deleteId = d.id })
                            }

                            Spacer(Modifier.height(10.dp))

                            // Month + Type badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = displayMonthYear(d.month, d.year),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = subtleText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                TypeBadge(type = d.type ?: "-")
                            }

                            Spacer(Modifier.height(12.dp))

                            // Base + Total
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Base", style = MaterialTheme.typography.labelSmall, color = subtleText)
                                    Text(
                                        "${d.baseAmount ?: 0.0}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Column {
                                    Text("Total", style = MaterialTheme.typography.labelSmall, color = subtleText)
                                    Text(
                                        "${d.totalAmount ?: 0.0}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            val depositedRaw = bestDepositedAtRaw(d)
                            Text(
                                text = "Deposited: ${formatDepositedAt(depositedRaw)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = subtleText
                            )
                        }
                    }
                }

                // Pagination chips
                if (lastPage > 1) {
                    item {
                        Spacer(Modifier.height(4.dp))

                        val current = page.toInt()
                        val last = lastPage.toInt()
                        val window = pageWindow(current, last)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = cardShape,
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Chip(
                                    text = "Prev",
                                    selected = false,
                                    enabled = current > 1,
                                    onClick = { load(current - 1) }
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (window.first() > 1) {
                                        Chip(text = "1", selected = current == 1) { load(1) }
                                        if (window.first() > 2) {
                                            Text("…", color = subtleText, modifier = Modifier.padding(top = 6.dp))
                                        }
                                    }

                                    window.forEach { p ->
                                        Chip(
                                            text = p.toString(),
                                            selected = p == current,
                                            onClick = { load(p) }
                                        )
                                    }

                                    if (window.last() < last) {
                                        if (window.last() < last - 1) {
                                            Text("…", color = subtleText, modifier = Modifier.padding(top = 6.dp))
                                        }
                                        Chip(text = last.toString(), selected = current == last) { load(last) }
                                    }
                                }

                                Chip(
                                    text = "Next",
                                    selected = false,
                                    enabled = current < last,
                                    onClick = { load(current + 1) }
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }

        // Member picker (filter)
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
                                        load(1)
                                    }
                                    .padding(vertical = 10.dp)
                            )
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showMemberPicker = false }) { Text("Close") } }
            )
        }

        // Year picker (filter)
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
                                        load(1)
                                    }
                                    .padding(vertical = 10.dp)
                            )
                        }
                        items(yearOptions) { y ->
                            Text(
                                y.toString(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedYear = y
                                        showYearPicker = false
                                        load(1)
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
                    }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = { TextButton(onClick = { deleteId = null }) { Text("Cancel") } }
            )
        }

        // Add/Edit sheet
        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState
            ) {
                val isEdit = editingDepositId != null

                Column(Modifier.padding(16.dp)) {
                    Text(
                        if (isEdit) "Edit Deposit" else "Add Deposit",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { showFormMemberPicker = true }) {
                        Text(formMemberName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(modifier = Modifier.weight(1f), onClick = { showFormMonthPicker = true }) { Text(formMonth) }
                        OutlinedButton(modifier = Modifier.weight(1f), onClick = { showFormYearPicker = true }) { Text(formYear?.toString() ?: "Year") }
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = { showFormTypePicker = true }) {
                        Text("Type: $formType")
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
                        value = formDepositedAt,
                        onValueChange = { formDepositedAt = it },
                        label = { Text("Deposited at (yyyy-MM-dd HH:mm:ss)") },
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

                            if (memberId == null) { Toast.makeText(context, "Select member", Toast.LENGTH_SHORT).show(); return@Button }
                            if (year == null) { Toast.makeText(context, "Select year", Toast.LENGTH_SHORT).show(); return@Button }
                            if (base == null) { Toast.makeText(context, "Enter valid base amount", Toast.LENGTH_SHORT).show(); return@Button }
                            if (formDepositedAt.isBlank()) { Toast.makeText(context, "Deposited at required", Toast.LENGTH_SHORT).show(); return@Button }

                            val monthNumber = monthNumberFromLabel(formMonth)
                            val normalizedType = formType.trim().lowercase(Locale.getDefault()) // cash/bkash/bank

                            val req = AdminDepositUpsertRequest(
                                memberId = memberId,
                                year = year,
                                month = monthNumber,
                                baseAmount = base,
                                type = normalizedType,
                                notes = formNotes.takeIf { it.isNotBlank() },
                                depositedAt = formDepositedAt
                            )

                            scope.launch {
                                try {
                                    val id = editingDepositId
                                    if (id == null) {
                                        repo.adminCreateDeposit(req)
                                        Toast.makeText(context, "Deposit added", Toast.LENGTH_SHORT).show()
                                    } else {
                                        repo.adminUpdateDeposit(id, req)
                                        Toast.makeText(context, "Deposit updated", Toast.LENGTH_SHORT).show()
                                    }
                                    showSheet = false
                                    load(1)
                                } catch (e: Exception) {
                                    Toast.makeText(context, e.message ?: "Failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    ) { Text(if (editingDepositId != null) "Update" else "Save") }

                    Spacer(Modifier.height(24.dp))
                }

                // Member picker (form)
                if (showFormMemberPicker) {
                    AlertDialog(
                        onDismissRequest = { showFormMemberPicker = false },
                        title = { Text("Select member") },
                        text = {
                            LazyColumn {
                                items(members.filter { it.first != 0L }) { m ->
                                    Text(
                                        m.second,
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
                        confirmButton = { TextButton(onClick = { showFormMemberPicker = false }) { Text("Close") } }
                    )
                }

                // Year picker (form)
                if (showFormYearPicker) {
                    AlertDialog(
                        onDismissRequest = { showFormYearPicker = false },
                        title = { Text("Select year") },
                        text = {
                            LazyColumn {
                                items(yearOptions) { y ->
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
                        confirmButton = { TextButton(onClick = { showFormYearPicker = false }) { Text("Close") } }
                    )
                }

                // Month picker (form)
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
                        confirmButton = { TextButton(onClick = { showFormMonthPicker = false }) { Text("Close") } }
                    )
                }

                // Type picker (form)
                if (showFormTypePicker) {
                    val types = listOf("Cash", "Bkash", "Bank")
                    AlertDialog(
                        onDismissRequest = { showFormTypePicker = false },
                        title = { Text("Select type") },
                        text = {
                            LazyColumn {
                                items(types) { t ->
                                    Text(
                                        t,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                formType = t
                                                showFormTypePicker = false
                                            }
                                            .padding(vertical = 10.dp)
                                    )
                                }
                            }
                        },
                        confirmButton = { TextButton(onClick = { showFormTypePicker = false }) { Text("Close") } }
                    )
                }
            }
        }
    }
}
