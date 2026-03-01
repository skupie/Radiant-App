package com.radiant.sms.network

import com.squareup.moshi.Json

// ------------------------------------------------------------
// ROOT RESPONSE
// ------------------------------------------------------------

data class MemberShareDetailsResponse(
    @Json(name = "member") val member: MemberInfo?,
    @Json(name = "share") val share: ShareInfo?,
    @Json(name = "nominee") val nominee: NomineeInfo?
)

// ------------------------------------------------------------
// MEMBER
// ------------------------------------------------------------

data class MemberInfo(
    @Json(name = "id") val id: Int?,
    @Json(name = "name") val name: String?,
    @Json(name = "email") val email: String?,
    @Json(name = "phone") val phone: String?,

    // Some APIs use "nid", some use "national_id"
    @Json(name = "nid") val nid: String? = null,
    @Json(name = "national_id") val nationalId: String? = null,

    @Json(name = "photo") val photo: String?
) {
    val displayName: String
        get() = name ?: "-"

    val displayPhotoUrl: String?
        get() = photo?.takeIf { it.isNotBlank() }

    val displayNid: String
        get() = (nid ?: nationalId).takeIf { !it.isNullOrBlank() } ?: "-"
}

// ------------------------------------------------------------
// SHARE
// ------------------------------------------------------------

data class ShareInfo(
    @Json(name = "share_no") val shareNo: Int?,
    @Json(name = "share_amount") val shareAmount: Double?,
    @Json(name = "total_deposit") val totalDeposit: Double?,
    @Json(name = "created_at") val createdAt: String?
) {
    val displayShareNo: String
        get() = shareNo?.toString() ?: "-"

    val displayShareAmount: String
        get() = shareAmount?.toString() ?: "-"

    val displayTotalDeposit: String
        get() = totalDeposit?.toString() ?: "-"

    val displayCreatedAt: String?
        get() = createdAt
}

// ------------------------------------------------------------
// NOMINEE
// ------------------------------------------------------------

data class NomineeInfo(
    @Json(name = "name") val name: String?,
    @Json(name = "nid") val nid: String?,
    @Json(name = "photo") val photo: String?
) {
    val displayName: String
        get() = name ?: "-"

    val displayNid: String
        get() = nid?.takeIf { it.isNotBlank() } ?: "-"

    val displayPhotoUrl: String?
        get() = photo?.takeIf { it.isNotBlank() }
}
