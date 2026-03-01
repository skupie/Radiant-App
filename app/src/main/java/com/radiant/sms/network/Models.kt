package com.radiant.sms.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MemberLedgerResponse(
    @Json(name = "year") val year: Int,
    @Json(name = "available_years") val availableYears: List<Int> = emptyList(),
    @Json(name = "monthly_data") val monthlyData: List<LedgerMonth> = emptyList(),
    @Json(name = "year_total") val yearTotal: Double = 0.0,
    @Json(name = "lifetime_total") val lifetimeTotal: Double = 0.0,
    @Json(name = "due_summary") val dueSummary: DueSummary? = null
)

@JsonClass(generateAdapter = true)
data class MemberDueSummaryResponse(
    @Json(name = "selected_year") val selectedYear: Int? = null,
    @Json(name = "available_years") val availableYears: List<Int> = emptyList(),
    @Json(name = "summary") val summary: DueSummary
)

@JsonClass(generateAdapter = true)
data class LedgerMonth(
    @Json(name = "month") val month: Int,
    @Json(name = "label") val label: String? = null,
    @Json(name = "total") val total: Double = 0.0,
    @Json(name = "entries") val entries: List<LedgerEntry> = emptyList()
)

@JsonClass(generateAdapter = true)
data class LedgerEntry(
    @Json(name = "id") val id: Int,
    @Json(name = "base_amount") val baseAmount: Double = 0.0,
    @Json(name = "total_amount") val totalAmount: Double = 0.0,
    @Json(name = "type") val type: String? = null,
    @Json(name = "deposited_at_local") val depositedAtLocal: String? = null,
    @Json(name = "notes") val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class DueSummary(
    @Json(name = "total") val total: Double = 0.0,
    @Json(name = "months") val months: List<DueMonth> = emptyList()
)

@JsonClass(generateAdapter = true)
data class DueMonth(
    @Json(name = "year") val year: Int,
    @Json(name = "month") val month: Int,
    @Json(name = "base_amount") val baseAmount: Double = 0.0,
    @Json(name = "amount") val amount: Double = 0.0
)
