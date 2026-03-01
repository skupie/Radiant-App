package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.network.*
import com.radiant.sms.network.NetworkModule
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberLedgerScreen(nav: NavController) {
    val context = LocalContext.current
    val api = remember { NetworkModule.api(context) }
    val scope = rememberCoroutineScope()

    val currentYear = remember { LocalDate.now().year }

    var profile by remember { mutableStateOf<MemberProfileResponse?>(null) }
    var ledger by remember { mutableStateOf<MemberLedgerResponse?>(null) }

    var selectedYear by remember { mutableStateOf<Int?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun load(year: Int?) {
        scope.launch {
            isLoading = true
            error = null
            try {
                // Keep profile stable while switching years
                if (profile == null) {
                    profile = api.getMemberProfile()
                }
                ledger = api.getMemberLedger(year)
                selectedYear = ledger?.year ?: year
            } catch (e: Exception) {
                error = e.message ?: "Failed to load ledger"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { load(currentYear) }

    ScreenScaffold(nav = nav) {

        if (isLoading && ledger == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@ScreenScaffold
        }

        val member = profile?.member
        val shareCount = member?.share ?: 0
        val displayYear = selectedYear ?: ledger?.year ?: currentYear

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            if (error != null) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }

            // =========================
            // Header (Name + Share Count)
            // =========================
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = member?.fullName ?: "",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Share Count: $shareCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // =========================
            // Year selector
            // =========================
            YearPicker(
                years = (ledger?.availableYears ?: emptyList()).sortedDescending(),
                selectedYear = displayYear,
                onYearSelected = { y -> load(y) },
                enabled = !isLoading
            )

            // =========================
            // All-time total deposits pill
            // =========================
            ledger?.let { led ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 2.dp
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        text = "All-time total deposits: ${formatMoney(led.lifetimeTotal)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // =========================
            // Due Summary
            // =========================
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Due Summary for $displayYear",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Outstanding amounts for months without deposits (share adjusted).",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))

                    val due = ledger?.dueSummary
                    val totalDue = due?.total ?: 0.0
                    Text(
                        text = "Total Due: ${formatMoney(totalDue)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(Modifier.height(14.dp))

                    DueSummaryTable(
                        months = due?.months ?: emptyList(),
                        shareCount = shareCount
                    )
                }
            }

            // =========================
            // Ledger
            // =========================
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ledger for $displayYear",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )

                        val yearTotal = ledger?.yearTotal ?: 0.0
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            tonalElevation = 2.dp
                        ) {
                            Text(
                                text = "Total: ${formatMoney(yearTotal)}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    val months = ledger?.monthlyData ?: emptyList()
                    if (isLoading && ledger != null) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(10.dp))
                    }

                    if (months.isEmpty()) {
                        Text(
                            text = "No ledger entries found for $displayYear.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        months.forEach { m ->
                            LedgerMonthBlock(month = m)
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YearPicker(
    years: List<Int>,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Select Year",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(end = 12.dp)
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = !expanded }
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor()
                    .widthIn(min = 140.dp),
                value = selectedYear.toString(),
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                val list = if (years.isNotEmpty()) years else listOf(selectedYear)
                list.forEach { y ->
                    DropdownMenuItem(
                        text = { Text(y.toString()) },
                        onClick = {
                            expanded = false
                            if (y != selectedYear) onYearSelected(y)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DueSummaryTable(
    months: List<DueMonth>,
    shareCount: Int
) {
    if (months.isEmpty()) {
        Text(
            text = "No due months for the selected year.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    // Header row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableHeaderCell(text = "Month", weight = 0.40f)
        TableHeaderCell(text = "Base\nAmount", weight = 0.22f)
        TableHeaderCell(text = "Share\nCount", weight = 0.18f)
        TableHeaderCell(text = "Due\nTotal", weight = 0.20f, alignEnd = true)
    }

    Divider()

    months.forEachIndexed { index, m ->
        if (index > 0) Divider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableBodyCell(
                text = "${monthName(m.month)}\n${m.year}",
                weight = 0.40f
            )
            TableBodyCell(
                text = formatMoney(m.baseAmount),
                weight = 0.22f
            )
            TableBodyCell(
                text = shareCount.toString(),
                weight = 0.18f
            )
            TableBodyCell(
                text = formatMoney(m.amount),
                weight = 0.20f,
                alignEnd = true,
                bold = true
            )
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, weight: Float, alignEnd: Boolean = false) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        textAlign = if (alignEnd) TextAlign.End else TextAlign.Start
    )
}

@Composable
private fun TableBodyCell(
    text: String,
    weight: Float,
    alignEnd: Boolean = false,
    bold: Boolean = false
) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal,
        textAlign = if (alignEnd) TextAlign.End else TextAlign.Start
    )
}

@Composable
private fun LedgerMonthBlock(month: LedgerMonth) {
    val entries = month.entries
    val monthLabel = month.label?.takeIf { it.isNotBlank() } ?: monthName(month.month)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = monthLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Entries: ${entries.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = formatMoney(month.total),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(10.dp))

        if (entries.isEmpty()) {
            Text(
                text = "No entries",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return
        }

        entries.forEachIndexed { idx, e ->
            if (idx > 0) Spacer(Modifier.height(10.dp))
            LedgerEntryCard(entry = e)
        }
    }
}

@Composable
private fun LedgerEntryCard(entry: LedgerEntry) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = formatMoney(entry.totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TagChip(label = "Base: ${formatMoney(entry.baseAmount)}")
                TagChip(label = "Total: ${formatMoney(entry.totalAmount)}")
                if (!entry.type.isNullOrBlank()) {
                    TagChip(label = entry.type!!.trim())
                }
            }

            if (!entry.depositedAtLocal.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Logged at: ${entry.depositedAtLocal}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!entry.notes.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = entry.notes!!.trim(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TagChip(label: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        tonalElevation = 0.dp,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatMoney(value: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return nf.format(value)
}

private fun monthName(month: Int): String {
    return runCatching {
        java.time.Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault())
    }.getOrDefault("Month $month")
}
