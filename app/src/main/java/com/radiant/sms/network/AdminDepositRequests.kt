package com.radiant.sms.network

import com.squareup.moshi.Json

data class AdminDepositUpsertRequest(
    @Json(name = "member_id")
    val memberId: Long,

    val year: Int,

    // ✅ Laravel usually validates month as integer 1..12
    val month: Int,

    @Json(name = "base_amount")
    val baseAmount: Double,

    // ✅ must be: cash | bkash | bank (lowercase)
    val type: String,

    val notes: String? = null,

    @Json(name = "deposited_at")
    val depositedAt: String
)
