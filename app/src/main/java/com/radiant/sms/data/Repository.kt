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
        val perPage = 100
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

    // ✅ NEW: Delete member
    suspend fun adminDeleteMember(memberId: Long): MessageResponse =
        api.adminDeleteMember(memberId)

    // keep existing screens working
    suspend fun adminDeposits(search: String? = null, perPage: Int? = null): AnyJson =
        api.adminDeposits(search = search, perPage = perPage)

    suspend fun adminDueSummary(search: String? = null, perPage: Int? = null): AnyJson =
        api.adminDueSummary(search = search, perPage = perPage)

    // typed deposits list + filters + pagination + summary total
    suspend fun adminDepositsList(
        search: String? = null,
        memberId: Long? = null,
        year: Int? = null,
        perPage: Int = 10,
        page: Int = 1
    ): AdminDepositsResponse =
        api.adminDepositsList(
            search = search,
            memberId = memberId,
            year = year,
            perPage = perPage,
            page = page
        )

    suspend fun adminCreateDeposit(body: AdminDepositUpsertRequest): AnyJson =
        api.adminCreateDeposit(body)

    suspend fun adminUpdateDeposit(id: Long, body: AdminDepositUpsertRequest): AnyJson =
        api.adminUpdateDeposit(id, body)

    suspend fun adminDeleteDeposit(id: Long): AnyJson =
        api.adminDeleteDeposit(id)


        // ---------- ADMIN PANEL (Activity + Team Members) ----------
suspend fun adminActivity(perPage: Int? = 50, page: Int? = 1): List<AdminActivityDto> =
    api.adminActivity(perPage = perPage, page = page).data

suspend fun adminTeamMembers(): List<AdminTeamMemberDto> =
    api.adminTeamMembers().data

suspend fun adminCreateTeamMember(body: AdminTeamMemberUpsertRequest): MessageResponse =
    api.adminCreateTeamMember(body)

suspend fun adminUpdateTeamMember(id: Long, body: AdminTeamMemberUpsertRequest): MessageResponse =
    api.adminUpdateTeamMember(id, body)

suspend fun adminDeleteTeamMember(id: Long): MessageResponse =
    api.adminDeleteTeamMember(id)
}
