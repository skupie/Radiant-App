package com.radiant.sms.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MemberShareDetailsResponse(
    // Some APIs wrap data differently; keep these names matching your endpoint output.
    @Json(name = "member") val member: MemberInfo? = null,
    @Json(name = "share") val share: ShareInfo? = null,
    @Json(name = "nominee") val nominee: NomineeInfo? = null
)

@JsonClass(generateAdapter = true)
data class MemberInfo(
    // Name can come as "name" or "full_name"
    @Json(name = "name") val name: String? = null,
    @Json(name = "full_name") val fullName: String? = null,

    @Json(name = "email") val email: String? = null,

    // Phone can come as "phone" or "mobile_number"
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "mobile_number") val mobileNumber: String? = null,

    // Member id can come as "member_id" or "id"
    @Json(name = "member_id") val memberIdSnake: String? = null,
    @Json(name = "id") val id: String? = null,

    // Photo can come as many different names depending on backend
    @Json(name = "photo") val photo: String? = null,
    @Json(name = "image") val image: String? = null,
    @Json(name = "avatar") val avatar: String? = null,
    @Json(name = "profile_photo") val profilePhoto: String? = null,
    @Json(name = "profile_photo_url") val profilePhotoUrl: String? = null
) {
    // Normalized fields for UI
    val displayName: String?
        get() = name ?: fullName

    val displayPhone: String?
        get() = phone ?: mobileNumber

    val displayMemberId: String?
        get() = memberIdSnake ?: id

    val displayPhotoUrl: String?
        get() = profilePhotoUrl ?: profilePhoto ?: avatar ?: image ?: photo
}

@JsonClass(generateAdapter = true)
data class ShareInfo(
    @Json(name = "share_no") val shareNoSnake: String? = null,
    @Json(name = "shareNo") val shareNoCamel: String? = null,

    @Json(name = "share_amount") val shareAmountSnake: String? = null,
    @Json(name = "shareAmount") val shareAmountCamel: String? = null,

    @Json(name = "total_deposit") val totalDepositSnake: String? = null,
    @Json(name = "totalDeposit") val totalDepositCamel: String? = null,

    @Json(name = "created_at") val createdAtSnake: String? = null,
    @Json(name = "createdAt") val createdAtCamel: String? = null
) {
    val displayShareNo: String?
        get() = shareNoSnake ?: shareNoCamel

    val displayShareAmount: String?
        get() = shareAmountSnake ?: shareAmountCamel

    val displayTotalDeposit: String?
        get() = totalDepositSnake ?: totalDepositCamel

    val displayCreatedAt: String?
        get() = createdAtSnake ?: createdAtCamel
}

@JsonClass(generateAdapter = true)
data class NomineeInfo(
    @Json(name = "name") val name: String? = null,

    @Json(name = "phone") val phone: String? = null,
    @Json(name = "mobile_number") val mobileNumber: String? = null,

    @Json(name = "relation") val relation: String? = null,

    @Json(name = "nid") val nid: String? = null,

    @Json(name = "address") val address: String? = null,

    // Photo variants
    @Json(name = "photo") val photo: String? = null,
    @Json(name = "image") val image: String? = null,
    @Json(name = "avatar") val avatar: String? = null,
    @Json(name = "profile_photo") val profilePhoto: String? = null,
    @Json(name = "profile_photo_url") val profilePhotoUrl: String? = null
) {
    val displayPhone: String?
        get() = phone ?: mobileNumber

    val displayPhotoUrl: String?
        get() = profilePhotoUrl ?: profilePhoto ?: avatar ?: image ?: photo
}
