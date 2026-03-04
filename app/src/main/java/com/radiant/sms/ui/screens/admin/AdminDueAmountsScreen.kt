package com.radiant.sms.ui.screens.admin

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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.radiant.sms.data.Repository
import com.radiant.sms.network.AnyJson
import com.radiant.sms.network.NetworkModule
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.DecimalFormat
import java.util.Locale

private data class AdminDueRow(
    val memberId: Long? = null,
    val memberName: String = "-",
    val memberCode: String = "",
    val shareCount: Int = 0,
    val dueMonths: Int = 0,
    val totalDue: Double = 0.0
)

private fun Any?.asMap(): Map<String, Any?>? {
    return when (this) {
        is Map<*, *> -> this.entries.associate { (k, v) -> k.toString() to v }
        else -> null
    }
}

private fun Any?.asString(): String? = when (this) {
    null -> null
    is String -> this
    else -> this.toString()
}

private fun Any?.asLong(): Long? = when (this) {
    is Number -> this.toLong()
    is String -> this.trim().toLongOrNull()
    else -> null
}

private fun Any?.asInt(): Int? = when (this) {
    is Number -> this.toInt()
    is String -> this.trim().toIntOrNull()
    else -> null
}

private fun Any?.asDouble(): Double? = when (this) {
    is Number -> this.toDouble()
    is String -> this.trim().replace(",", "").toDoubleOrNull()
    else -> null
}

/** ✅ NEW: If backend sends array/list of months, use size as dueMonths */
private fun Any?.asListSizeInt(): Int? {
    return when (this) {
        is List<*> -> this.size
        is Array<*> -> this.size
        // Sometimes backend may send "1,2,3" string
        is String -> {
            val s = this.trim()
            if (s.contains(",")) s.split(",").map { it.trim() }.filter { it.isNotEmpty() }.size else null
        }
        else -> null
    }
}

private fun parseAdminDueSummary(json: AnyJson): Pair<List<AdminDueRow>, Double?> {
    val dataAny = json["data"]
    val list = (dataAny as? List<*>)?.filterNotNull()?.mapNotNull { it as? Map<*, *> }
        ?: (json["members"] as? List<*>)?.filterNotNull()?.mapNotNull { it as? Map<*, *> }
        ?: emptyList()

    val rows = list.map { raw ->
        val m = raw.entries.associate { (k, v) -> k.toString() to v }
        val memberMap = m["member"].asMap()

        val memberId =
            (m["member_id"].asLong()
                ?: memberMap?.get("id").asLong()
                ?: m["id"].asLong())

        val name = listOf(
            m["full_name"].asString(),
            m["member_full_name"].asString(),
            m["member_name"].asString(),
            m["name"].asString(),
            memberMap?.get("full_name").asString(),
            memberMap?.get("name").asString()
        ).firstOrNull { !it.isNullOrBlank() }?.trim().orEmpty().ifBlank { "Member" }

        val code = listOf(
            m["nid"].asString(),
            m["member_code"].asString(),
            m["member_no"].asString(),
            m["code"].asString(),
            m["account_no"].asString(),
            memberMap?.get("nid").asString()
        ).firstOrNull { !it.isNullOrBlank() }?.trim().orEmpty()

        val shareCount = listOf(
            m["share"].asInt(),
            m["shares"].asInt(),
            m["share_count"].asInt(),
            memberMap?.get("share").asInt(),
            memberMap?.get("shares").asInt(),
            memberMap?.get("share_count").asInt()
        ).firstOrNull { it != null } ?: 0

        // ✅ FIX: handle due months as Int OR list size
        val dueMonths = listOf(
            // integer keys
            m["due_months"].asInt(),
            m["due_month"].asInt(),
            m["due_count"].asInt(),
            m["months"].asInt(),
            m["due"].asInt(),

            // list/array keys (use size)
            m["due_months"].asListSizeInt(),
            m["due_month_list"].asListSizeInt(),
            m["due_month_list_local"].asListSizeInt(),
            m["months"].asListSizeInt(),
            m["due"].asListSizeInt(),

            // sometimes nested month list
            m["due_summary"].asMap()?.get("months").asListSizeInt(),
            m["due_summary"].asMap()?.get("due_months").asListSizeInt()
        ).firstOrNull { it != null } ?: 0

        val totalDue = listOf(
            m["total_due"].asDouble(),
            m["due_total"].asDouble(),
            m["amount"].asDouble(),
            m["total"].asDouble()
        ).firstOrNull { it != null } ?: 0.0

        AdminDueRow(
            memberId = memberId,
            memberName = name,
            memberCode = code,
            shareCount = shareCount,
            dueMonths = dueMonths,
            totalDue = totalDue
        )
    }

    val summary = json["summary"].asMap()
    val summaryTotal = listOf(
        summary?.get("total_due").asDouble(),
        summary?.get("total").asDouble(),
        json["total_due"].asDouble(),
        json["total"].asDouble()
    ).firstOrNull { it != null }

    return rows to summaryTotal
}

private fun formatMoney(amount: Double): String = DecimalFormat("#,##0.00").format(amount)

@Composable
private fun NumberBadge(
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .wrapContentWidth(Alignment.CenterHorizontally)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDueAmountsScreen(nav: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { Repository(NetworkModule.api(context)) }

    val screenBg = Color(0xFFF6F7FB)
    val cardBg = Color.White
    val subtleText = Color(0xFF6B7280)

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var search by remember { mutableStateOf("") }
    var all by remember { mutableStateOf<List<AdminDueRow>>(emptyList()) }
    var serverTotalDue by remember { mutableStateOf<Double?>(null) }

    val filtered = remember(search, all) {
        val q = search.trim().lowercase(Locale.getDefault())
        if (q.isBlank()) all
        else all.filter { r ->
            listOf(r.memberName, r.memberCode)
                .any { it.lowercase(Locale.getDefault()).contains(q) }
        }
    }
    val filteredTotal = remember(filtered) { filtered.sumOf { it.totalDue } }

    fun load() {
        scope.launch {
            loading = true
            error = null
            try {
                val json = repo.adminDueSummary(search = null, perPage = null)
                val (rows, total) = parseAdminDueSummary(json)
                all = rows
                serverTotalDue = total
            } catch (e: Exception) {
                error = when (e) {
                    is HttpException -> "HTTP ${e.code()}"
                    else -> (e.message ?: "Failed to load due amounts")
                }
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    AdminScaffold(nav = nav) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBg)
                .padding(16.dp)
        ) {
            Text(
                text = "Due Amounts",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                placeholder = { Text("Search members...") },
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Members", style = MaterialTheme.typography.labelMedium, color = subtleText)
                            Text(
                                filtered.size.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Due", style = MaterialTheme.typography.labelMedium, color = subtleText)
                            Text(
                                formatMoney(serverTotalDue ?: filteredTotal),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (loading) {
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    if (!error.isNullOrBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = error ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { load() }) { Text("Retry") }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column {
                    // header (more spacing + centered columns)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "MEMBER",
                            modifier = Modifier.weight(2.2f),
                            color = subtleText,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            "SHARE",
                            modifier = Modifier.weight(0.85f),
                            color = subtleText,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "DUE",
                            modifier = Modifier.weight(0.85f),
                            color = subtleText,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "TOTAL",
                            modifier = Modifier.weight(1.1f),
                            color = subtleText,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.End
                        )
                    }
                    Divider()

                    if (!loading && filtered.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text("No members found", color = subtleText)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(filtered) { row ->
                                DueRowItem(
                                    row = row,
                                    subtleText = subtleText,
                                    onClick = {
                                        row.memberId?.let { nav.navigate("admin_member_details/$it") }
                                    }
                                )
                                Divider()
                            }
                            item { Spacer(Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DueRowItem(
    row: AdminDueRow,
    subtleText: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = row.memberId != null) { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(2.2f)) {
            Text(
                text = row.memberName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (row.memberCode.isNotBlank()) {
                Text(
                    text = row.memberCode,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtleText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // ✅ badge style + spacing
        Box(modifier = Modifier.weight(0.85f), contentAlignment = Alignment.Center) {
            NumberBadge(value = row.shareCount.toString())
        }

        Box(modifier = Modifier.weight(0.85f), contentAlignment = Alignment.Center) {
            NumberBadge(value = row.dueMonths.toString())
        }

        Text(
            formatMoney(row.totalDue),
            modifier = Modifier
                .weight(1.1f)
                .padding(start = 6.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.width(2.dp))
    }
}
