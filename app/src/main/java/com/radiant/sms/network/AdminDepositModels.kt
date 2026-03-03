package com.radiant.sms.network

import com.squareup.moshi.Json

data class AdminDepositsResponse(
    val data: List<AdminDepositItem> = emptyList(),
    val meta: AdminDepositMeta? = null,
    val summary: AdminDepositSummary? = null
)

data class AdminDepositItem(
    val id: Long = 0L,

    // Sometimes API returns nested member, sometimes it doesn't
    val member: AdminDepositMember? = null,

    // Common flat name fields from Laravel resources
    @Json(name = "member_name")
    val memberName: String? = null,

    @Json(name = "member_full_name")
    val memberFullName: String? = null,

    // Some APIs return `name` directly in the deposit item
    val name: String? = null,

    val year: Int? = null,

    // backend can send "2" / "02" / "Feb"
    val month: String? = null,

    @Json(name = "base_amount")
    val baseAmount: Double? = null,

    @Json(name = "total_amount")
    val totalAmount: Double? = null,

    // "Cash" / "Bkash" / "Bank"
    val type: String? = null,

    // Timestamp keys (we support multiple)
    @Json(name = "deposited_at")
    val depositedAt: String? = null,

    @Json(name = "logged_at")
    val loggedAt: String? = null,

    @Json(name = "created_at")
    val createdAt: String? = null
)

data class AdminDepositMember(
    val id: Long = 0L,
    val name: String? = null
)

data class AdminDepositMeta(
    @Json(name = "current_page")
    val currentPage: Int? = null,

    @Json(name = "last_page")
    val lastPage: Int? = null
)

data class AdminDepositSummary(
    @Json(name = "filtered_total")
    val filteredTotal: Double? = null,

    @Json(name = "available_years")
    val availableYears: List<Int>? = null
)
