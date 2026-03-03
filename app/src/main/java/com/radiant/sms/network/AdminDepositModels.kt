package com.radiant.sms.network

import com.squareup.moshi.Json

data class AdminDepositsResponse(
    val data: List<AdminDepositItem> = emptyList(),
    val meta: AdminDepositMeta? = null,
    val summary: AdminDepositSummary? = null
)

data class AdminDepositItem(
    val id: Long = 0L,

    // ✅ Some APIs send member_id flat
    @Json(name = "member_id")
    val memberId: Long? = null,

    // ✅ Some APIs send nested member object
    val member: AdminDepositMember? = null,

    // ✅ Some APIs send member name flat
    @Json(name = "member_name")
    val memberName: String? = null,

    @Json(name = "member_full_name")
    val memberFullName: String? = null,

    // Some APIs might use `name` key
    val name: String? = null,

    val year: Int? = null,
    val month: String? = null,

    @Json(name = "base_amount")
    val baseAmount: Double? = null,

    @Json(name = "total_amount")
    val totalAmount: Double? = null,

    val type: String? = null,

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
