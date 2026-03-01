package com.radiant.sms.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MemberShareDetailsResponse(

    // ✅ Some APIs wrap everything inside { "data": { ... } }
    @Json(name = "data") val data: MemberShareDetailsResponse? = null,

    // Common nested variants
    @Json(name = "member") val member: MemberInfo? = null,

    @Json(name = "share") val share: ShareInfo? = null,
    @Json(name = "share_info") val shareInfo: ShareInfo? = null,
    @Json(name = "share_details") val shareDetails: ShareInfo? = null,
    @Json(name = "share_information") val shareInformation: ShareInfo? = null,
    @Json(name = "shareInformation") val shareInformationCamel: ShareInfo? = null,

    @Json(name = "nominee") val nominee: NomineeInfo? = null,
    @Json(name = "nominee_info") val nomineeInfo: NomineeInfo? = null,
    @Json(name = "nominee_details") val nomineeDetails: NomineeInfo? = null,
    @Json(name = "nominee_information") val nomineeInformation: NomineeInfo? = null,
    @Json(name = "nomineeInformation") val nomineeInformationCamel: NomineeInfo? = null,

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

    /**
     * ✅ Always use the "real payload" if wrapped in data
     */
    val effective: MemberShareDetailsResponse
        get() = data ?: this

    // ✅ For UI: always pick the best available share object
    val resolvedShare: ShareInfo?
        get() = effective.share
            ?: effective.shareInfo
            ?: effective.shareDetails
            ?: effective.shareInformation
            ?: effective.shareInformationCamel
            ?: run {
                if (
                    effective.shareNoFlat == null &&
                    effective.shareAmountFlat == null &&
                    effective.totalDepositFlat == null &&
                    effective.createdAtFlat == null
                ) null
                else ShareInfo(
                    shareNoSnake = effective.shareNoFlat,
                    shareAmountSnake = effective.shareAmountFlat,
                    totalDepositSnake = effective.totalDepositFlat,
                    createdAtSnake = effective.createdAtFlat
                )
            }

    // ✅ For UI: always pick the best available nominee object
    val resolvedNominee: NomineeInfo?
        get() = effective.nominee
            ?: effective.nomineeInfo
            ?: effective.nomineeDetails
            ?: effective.nomineeInformation
            ?: effective.nomineeInformationCamel
            ?: run {
                if (
                    effective.nomineeNameFlat == null &&
                    effective.nomineePhoneFlat == null &&
                    effective.nomineeRelationFlat == null &&
                    effective.nomineeNidFlat == null &&
                    effective.nomineeAddressFlat == null &&
                    effective.nomineePhotoFlat == null
                ) null
                else NomineeInfo(
                    name = effective.nomineeNameFlat,
                    phone = effective.nomineePhoneFlat,
                    relation = effective.nomineeRelationFlat,
                    nid = effective.nomineeNidFlat,
                    address = effective.nomineeAddressFlat,
                    photo = effective.nomineePhotoFlat
                )
            }

    val resolvedMember: MemberInfo?
        get() = effective.member
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
