package com.radiant.sms.data

import com.radiant.sms.network.*
import okhttp3.MultipartBody

class Repository(private val api: ApiService) {

    // ---------- AUTH ----------
    suspend fun login(request: LoginRequest): LoginResponse = api.login(request)
    suspend fun login(email: String, password: String): LoginResponse =
        login(LoginRequest(email = email, password = password))

    suspend fun me(): MeResponse = api.me()
    suspend fun logout(): MessageResponse = api.logout()

    suspend fun changePassword(currentPassword: String, newPassword: String): MessageResponse {
        val body = ChangePasswordRequest(currentPassword = currentPassword, newPassword = newPassword)
        return try {
            api.memberChangePassword(body)
        } catch (_: Exception) {
            api.authChangePassword(body)
        }
    }

    // ---------- MEMBER ----------
    suspend fun memberProfile(): MemberProfileResponse = api.getMemberProfile()
    suspend fun memberLedger(year: Int? = null): MemberLedgerResponse = api.getMemberLedger(year)
    suspend fun memberDueSummary(year: Int? = null): MemberDueSummaryResponse = api.getMemberDueSummary(year)
    suspend fun memberShareDetails(): MemberShareDetailsResponse = api.getMemberShareDetails()

    // ---------- ADMIN ----------
    suspend fun adminMembersAll(search: String? = null): List<AdminMemberDto> {
        val all = mutableListOf<AdminMemberDto>()
        val perPage = 100 // ✅ backend max (prevents HTTP 422)
        var page = 1

        while (true) {
            val resp = api.adminMembers(search = search, perPage = perPage, page = page)
            val chunk = resp.data
            all.addAll(chunk)
            if (chunk.size < perPage) break
            page++
        }

        return all
    }

    suspend fun adminMemberDetails(memberId: Long): AdminMemberDetailsResponse =
        api.adminMemberDetails(memberId)

    suspend fun adminCreateMember(parts: List<MultipartBody.Part>): MessageResponse =
        api.adminCreateMember(parts)

    suspend fun adminUpdateMember(memberId: Long, parts: List<MultipartBody.Part>): MessageResponse =
        api.adminUpdateMember(memberId, parts)

    // keep existing screens working
    suspend fun adminDeposits(search: String? = null, perPage: Int? = null): AnyJson =
        api.adminDeposits(search = search, perPage = perPage)

    suspend fun adminDueSummary(search: String? = null, perPage: Int? = null): AnyJson =
        api.adminDueSummary(search = search, perPage = perPage)
}
