package com.radiant.sms.network.models

data class MemberProfileResponse(val member: Member)

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

data class MemberShareDetailsResponse(
    val member: Member,
    val total_deposited: Double = 0.0,
    val total_due: Double = 0.0
)

data class Member(
    val id: Int,
    val user_id: Int? = null,
    val full_name: String? = null,
    val nid: String? = null,
    val email: String? = null,
    val mobile_number: String? = null,
    val nominee_name: String? = null,
    val nominee_nid: String? = null,
    val share: Int? = null,
    val image_url: String? = null,
    val nominee_photo_url: String? = null,
    val total_deposited: Double? = null,
    val due_total: Double? = null,
    val due_months_count: Int? = null
)

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
