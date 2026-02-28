package com.radiant.sms.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MemberProfileResponse(
    @Json(name = "member") val member: MemberDto?,
    @Json(name = "total_deposited") val totalDeposited: Double? = 0.0,
    @Json(name = "total_due") val totalDue: Double? = 0.0
)

@JsonClass(generateAdapter = true)
data class MemberDto(
    @Json(name = "full_name") val fullName: String? = null,
    @Json(name = "nid") val nid: String? = null,
    @Json(name = "email") val email: String? = null,

    // change these names if API differs
    @Json(name = "mobile_number") val mobileNumber: String? = null,
    @Json(name = "share") val share: Int? = 0,
    @Json(name = "created_at") val createdAt: String? = null,

    @Json(name = "nominee_name") val nomineeName: String? = null,
    @Json(name = "nominee_nid") val nomineeNid: String? = null
)
