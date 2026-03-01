package com.radiant.sms.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MemberShareDetailsResponse(
    // Common nested variants
    @Json(name = "member") val member: MemberInfo? = null,

    @Json(name = "share") val share: ShareInfo? = null,
    @Json(name = "share_info") val shareInfo: ShareInfo? = null,
    @Json(name = "share_details") val shareDetails: ShareInfo? = null,

    @Json(name = "nominee") val nominee: NomineeInfo? = null,
    @Json(name = "nominee_info") val nomineeInfo: NomineeInfo? = null,
    @Json(name = "nominee_details") val nomineeDetails: NomineeInfo? = null,

    // Flat fallback variants (some APIs return these at top-level)
    @Json(name = "share_no") val shareNoFlat: String? = null,
    @Json(name = "share_amount") val shareAmountFlat: String? = null,
    @Json(name = "total_deposit") val totalDepositFlat: String? = null,
    @Json(name = "created_at") val createdAtFlat: String? = null,

    @Json(name = "nominee_name") val nomineeNameFlat: String? = null,
    @Json(name = "nominee_phone") val nomineePhoneFlat: String? = null,
    @Json(name = "nominee_relation") val nomineeRelationFlat: String? = null,
    @Json(name = "nominee_nid") val nomineeNidFlat: String? = null,
    @Json(name = "nominee_address") val nomineeAddressFlat: String? = null,
    @Json(name = "nominee_photo") val nomineePhotoFlat: String? = null
) {

    // ✅ For UI: always pick the best available share object
    val resolvedShare: ShareInfo?
        get() = share ?: shareInfo ?: shareDetails ?: run {
            if (
                shareNoFlat == null &&
                shareAmountFlat == null &&
                totalDepositFlat == null &&
                createdAtFlat == null
            ) null
            else ShareInfo(
                shareNoSnake = shareNoFlat,
                shareAmountSnake = shareAmountFlat,
                totalDepositSnake = totalDepositFlat,
                createdAtSnake = createdAtFlat
            )
        }

    // ✅ For UI: always pick the best available nominee object
    val resolvedNominee: NomineeInfo?
        get() = nominee ?: nomineeInfo ?: nomineeDetails ?: run {
            if (
                nomineeNameFlat == null &&
                nomineePhoneFlat == null &&
                nomineeRelationFlat == null &&
                nomineeNidFlat == null &&
                nomineeAddressFlat == null &&
                nomineePhotoFlat == null
            ) null
            else NomineeInfo(
                name = nomineeNameFlat,
                phone = nomineePhoneFlat,
                relation = nomineeRelationFlat,
                nid = nomineeNidFlat,
                address = nomineeAddressFlat,
                photo = nomineePhotoFlat
            )
        }
}

@JsonClass(generateAdapter = true)
data class MemberInfo(
    @Json(name = "name") val name: String? = null,
    @Json(name = "full_name") val fullName: String? = null,

    @Json(name = "email") val email: String? = null,

    @Json(name = "phone") val phone: String? = null,
    @Json(name = "mobile_number") val mobileNumber: String? = null,

    @Json(name = "member_id") val memberIdSnake: String? = null,
    @Json(name = "id") val id: String? = null,

    // photo variants
    @Json(name = "photo") val photo: String? = null,
    @Json(name = "image") val image: String? = null,
    @Json(name = "avatar") val avatar: String? = null,
    @Json(name = "profile_photo") val profilePhoto: String? = null,
    @Json(name = "profile_photo_url") val profilePhotoUrl: String? = null
) {
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
