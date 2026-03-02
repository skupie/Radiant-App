package com.radiant.sms.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AdminMembersResponse(
    val data: List<AdminMemberDto> = emptyList(),
    val meta: PageMeta? = null
)

@JsonClass(generateAdapter = true)
data class PageMeta(
    @Json(name = "current_page") val currentPage: Int? = null,
    @Json(name = "last_page") val lastPage: Int? = null,
    @Json(name = "per_page") val perPage: Int? = null,
    val total: Int? = null
)

@JsonClass(generateAdapter = true)
data class AdminMemberDto(
    val id: Long? = null,
    @Json(name = "full_name") val fullName: String? = null,
    val email: String? = null,
    val nid: String? = null,
    val share: Int? = null,
    @Json(name = "mobile_number") val mobileNumber: String? = null,
    @Json(name = "deposits_count") val depositsCount: Int? = null,
    @Json(name = "total_deposited") val totalDeposited: Double? = null,
    @Json(name = "image_url") val imageUrl: String? = null
)
