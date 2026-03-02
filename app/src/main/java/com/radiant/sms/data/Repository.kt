package com.radiant.sms.data

import com.radiant.sms.network.AdminDepositsResponse
import com.radiant.sms.network.AdminDueSummaryResponse
import com.radiant.sms.network.AdminMembersResponse
import com.radiant.sms.network.ApiService
import com.radiant.sms.network.ChangePasswordRequest
import com.radiant.sms.network.LoginRequest
import com.radiant.sms.network.LoginResponse
import com.radiant.sms.network.MeResponse
import com.radiant.sms.network.MemberDueSummaryResponse
import com.radiant.sms.network.MemberLedgerResponse
import com.radiant.sms.network.MemberProfileResponse
import com.radiant.sms.network.MemberShareDetailsResponse
import com.radiant.sms.network.MessageResponse

/**
 * Single source of truth for API calls used by the app.
 *
 * NOTE:
 * - ApiService methods in this project return the *parsed models* directly (not retrofit2.Response<T>).
 * - So Repository also returns the models directly.
 */
class Repository(private val api: ApiService) {

    // ---------- AUTH ----------
    suspend fun login(request: LoginRequest): LoginResponse =
        api.login(request)

    /** Convenience overload used by ViewModels */
    suspend fun login(email: String, password: String): LoginResponse =
        login(LoginRequest(email = email, password = password))

    suspend fun me(): MeResponse =
        api.me()

    suspend fun logout(): MessageResponse =
        api.logout()

    /**
     * Change password (member). Some backends expose this under different paths.
     * We try member endpoint first, then fall back to auth endpoint.
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): MessageResponse {
        val body = ChangePasswordRequest(
            currentPassword = currentPassword,
            newPassword = newPassword
        )
        return try {
            api.memberChangePassword(body)
        } catch (_: Exception) {
            api.authChangePassword(body)
        }
    }

    // ---------- MEMBER ----------
    suspend fun memberProfile(): MemberProfileResponse =
        api.getMemberProfile()

    suspend fun memberLedger(year: Int? = null): MemberLedgerResponse =
        api.getMemberLedger(year)

    suspend fun memberDueSummary(year: Int? = null): MemberDueSummaryResponse =
        api.getMemberDueSummary(year)

    suspend fun memberShareDetails(): MemberShareDetailsResponse =
        api.getMemberShareDetails()

    // ---------- ADMIN ----------
    /**
     * IMPORTANT:
     * These must return typed models (NOT AnyJson), otherwise your Admin UI code will fail to compile.
     *
     * Make sure ApiService admin methods also return these same models:
     * - adminMembers(...) : AdminMembersResponse
     * - adminDeposits(...) : AdminDepositsResponse
     * - adminDueSummary(...) : AdminDueSummaryResponse
     */
    suspend fun adminMembers(search: String? = null, perPage: Int? = null): AdminMembersResponse =
        api.adminMembers(search = search, perPage = perPage)

    suspend fun adminDeposits(search: String? = null, perPage: Int? = null): AdminDepositsResponse =
        api.adminDeposits(search = search, perPage = perPage)

    suspend fun adminDueSummary(search: String? = null, perPage: Int? = null): AdminDueSummaryResponse =
        api.adminDueSummary(search = search, perPage = perPage)
}
