package com.radiant.sms.network

import com.squareup.moshi.Json

data class AdminDepositUpsertRequest(
    @Json(name = "member_id")
    val memberId: Long,

    val year: Int,

    // send month as number string: "1".."12"
    val month: String,

    @Json(name = "base_amount")
    val baseAmount: Double,

    val type: String,

    val notes: String? = null,

    @Json(name = "deposited_at")
    val depositedAt: String
)
