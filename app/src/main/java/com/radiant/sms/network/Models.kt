package com.radiant.sms.network

typealias AnyJson = Map<String, Any?>

data class MemberLedgerResponse(
    val year: Int,
    val available_years: List<Int> = emptyList(),
    val monthly_data: List<LedgerMonth> = emptyList(),
    val year_total: Double = 0.0,
    val lifetime_total: Double = 0.0,
    val due_summary: DueSummary? = null
)

data class MemberDueSummaryResponse(
    val selected_year: Int? = null,
    val available_years: List<Int> = emptyList(),
    val summary: DueSummary
)

// ✅ NOTE:
// Do NOT define MemberProfileResponse here (you already have it in MemberProfileResponse.kt)
// Do NOT define MemberShareDetailsResponse here (it exists in its own file)

data class LedgerMonth(
    val month: Int,
    val label: String? = null,
    val total: Double = 0.0,
    val entries: List<LedgerEntry> = emptyList()
)

data class LedgerEntry(
    val id: Int,
    val base_amount: Double = 0.0,
    val total_amount: Double = 0.0,
    val type: String? = null,
    val deposited_at_local: String? = null,
    val notes: String? = null
)

data class DueSummary(
    val total: Double = 0.0,
    val months: List<DueMonth> = emptyList()
)

data class DueMonth(
    val year: Int,
    val month: Int,
    val base_amount: Double = 0.0,
    val amount: Double = 0.0
)
