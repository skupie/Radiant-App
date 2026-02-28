package com.radiant.sms.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MemberShareDetailsResponse(
    val member: MemberInfo? = null,
    val share: ShareInfo? = null,
    val nominee: NomineeInfo? = null
)

@JsonClass(generateAdapter = true)
data class MemberInfo(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val member_id: String? = null
)

@JsonClass(generateAdapter = true)
data class ShareInfo(
    val share_no: String? = null,
    val share_amount: String? = null,
    val total_deposit: String? = null,
    val created_at: String? = null
)

@JsonClass(generateAdapter = true)
data class NomineeInfo(
    val name: String? = null,
    val phone: String? = null,
    val relation: String? = null,
    val nid: String? = null,
    val address: String? = null
)
