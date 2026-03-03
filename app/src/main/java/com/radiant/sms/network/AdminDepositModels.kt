package com.radiant.sms.network

import com.squareup.moshi.Json

data class AdminDepositsResponse(
    val data: List<AdminDepositItem> = emptyList(),
    val meta: AdminDepositMeta? = null,
    val summary: AdminDepositSummary? = null
)

data class AdminDepositItem(
    val id: Long = 0L,
    val member: AdminDepositMember? = null,
    val year: Int? = null,
    val month: String? = null,

    @Json(name = "base_amount")
    val baseAmount: Double? = null,

    @Json(name = "total_amount")
    val totalAmount: Double? = null,

    val type: String? = null,

    @Json(name = "logged_at")
    val loggedAt: String? = null
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
