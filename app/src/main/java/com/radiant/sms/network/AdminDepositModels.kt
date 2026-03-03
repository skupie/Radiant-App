package com.radiant.sms.network

import com.google.gson.annotations.SerializedName

data class AdminDepositsResponse(
    val data: List<AdminDepositItem>,
    val meta: AdminDepositMeta?,
    val summary: AdminDepositSummary?
)

data class AdminDepositItem(
    val id: Int,
    val member: AdminDepositMember?,
    val year: Int?,
    val month: String?,
    @SerializedName("base_amount")
    val baseAmount: Double?,
    @SerializedName("total_amount")
    val totalAmount: Double?,
    val type: String?,
    @SerializedName("logged_at")
    val loggedAt: String?
)

data class AdminDepositMember(
    val id: Int,
    val name: String?
)

data class AdminDepositMeta(
    @SerializedName("current_page")
    val currentPage: Int?,
    @SerializedName("last_page")
    val lastPage: Int?
)

data class AdminDepositSummary(
    @SerializedName("filtered_total")
    val filteredTotal: Double?,
    @SerializedName("available_years")
    val availableYears: List<Int>?
)
