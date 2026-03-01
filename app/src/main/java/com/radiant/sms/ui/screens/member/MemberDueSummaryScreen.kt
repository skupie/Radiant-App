package com.radiant.sms.ui.screens.member

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.data.Repository
import com.radiant.sms.network.DueMonth
import com.radiant.sms.network.MemberDueSummaryResponse
import com.radiant.sms.network.MemberProfileResponse
import com.radiant.sms.network.NetworkModule
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MemberDueSummaryScreen(navController: NavController) {
    val context = LocalContext.current
    val api = remember { NetworkModule.api(context) }
    val repo = remember { Repository(api) }
    val scope = rememberCoroutineScope()

    // Default MUST be All Years
    var selectedYear by remember { mutableStateOf<Int?>(null) }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var data by remember { mutableStateOf<MemberDueSummaryResponse?>(null) }
    var profile by remember { mutableStateOf<MemberProfileResponse?>(null) }

    fun load(year: Int?) {
        scope.launch {
            loading = true
            error = null
            try {
                if (profile == null) {
                    profile = repo.memberProfile()
                }
                data = repo.memberDueSummary(year = year)
                selectedYear = data?.selectedYear ?: year
            } catch (e: Exception) {
                error = e.message ?: "Unknown error"
            } finally {
                loading = false
            }
        }
    }

    // Load All Years by default
    LaunchedEffect(Unit) { load(null) }

    ScreenScaffold(
        nav = navController,
        title = "",
        hideTitle = true,
        showHamburger = true
    ) {
        val shareCount = profile?.member?.share ?: 0
        val availableYears = data?.availableYears?.sortedDescending() ?: emptyList()
        val totalDue = data?.summary?.total ?: 0.0

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // Title added
            Text(
                text = "Due Summary",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Outstanding Due Amounts",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (error != null) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }

            DueYearFilter(
                years = availableYears,
                selectedYear = selectedYear,
                enabled = !loading,
                onYearSelected = { y -> load(y) }
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                tonalElevation = 1.dp
            ) {
                Text(
                    text = "Total Due: ${formatMoney(totalDue)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 12.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            Spacer(Modifier.height(4.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    if (loading && data == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                        return@ElevatedCard
                    }

                    if (loading && data != null) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(12.dp))
                    }

                    val months = data?.summary?.months ?: emptyList()
                    if (months.isEmpty()) {
                        Text(
                            text = "No due records found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        return@ElevatedCard
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableHeaderCell(text = "MONTH", weight = 0.35f)
                        TableHeaderCell(text = "BASE\nAMOUNT", weight = 0.23f)
                        TableHeaderCell(text = "SHARE\nCOUNT", weight = 0.20f)
                        TableHeaderCell(text = "DUE\nAMOUNT", weight = 0.22f, alignEnd = true)
                    }

                    Divider()

                    months.forEachIndexed { index, m ->
                        if (index > 0) Divider()
                        DueRow(month = m, shareCount = shareCount)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueYearFilter(
    years: List<Int>,
    selectedYear: Int?,
    enabled: Boolean,
    onYearSelected: (Int?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Filter by Year",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = !expanded }
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                value = selectedYear?.toString() ?: "All Years",
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
                DropdownMenuItem(
                    text = { Text("All Years") },
                    onClick = {
                        expanded = false
                        if (selectedYear != null) onYearSelected(null)
                    }
                )

                years.forEach { y ->
                    DropdownMenuItem(
                        text = { Text(y.toString()) },
                        onClick = {
                            expanded = false
                            if (selectedYear != y) onYearSelected(y)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DueRow(month: DueMonth, shareCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableBodyCell(
            text = "${monthName(month.month)}\n${month.year}",
            weight = 0.35f
        )
        TableBodyCell(
            text = formatMoney(month.baseAmount),
            weight = 0.23f
        )
        TableBodyCell(
            text = shareCount.toString(),
            weight = 0.20f
        )
        TableBodyCell(
            text = formatMoney(month.amount),
            weight = 0.22f,
            alignEnd = true,
            bold = true
        )
    }
}

@Composable
private fun RowScope.TableHeaderCell(text: String, weight: Float, alignEnd: Boolean = false) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        textAlign = if (alignEnd) TextAlign.End else TextAlign.Start
    )
}

@Composable
private fun RowScope.TableBodyCell(
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

private fun formatMoney(value: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return nf.format(value)
}

private fun monthName(month: Int): String {
    return runCatching {
        val months = java.text.DateFormatSymbols(Locale.getDefault()).months
        if (month in 1..12) months[month - 1] else "Month $month"
    }.getOrDefault("Month $month")
}
