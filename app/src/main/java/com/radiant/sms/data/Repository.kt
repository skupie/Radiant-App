package com.radiant.sms.data

import com.radiant.sms.network.*

class Repository(private val api: ApiService) {

    // ---------------- AUTH ----------------

    suspend fun login(email: String, password: String): LoginResponse {
        return api.login(
            LoginRequest(
                email = email,
                password = password,
                device_name = "android"
            )
        )
    }

    suspend fun me(): MeResponse = api.me()

    suspend fun logout(): MessageResponse = api.logout()


    // ---------------- MEMBER ----------------

    suspend fun memberProfile(): MemberProfileResponse {
        return api.getMemberProfile()
    }

    suspend fun memberLedger(year: Int?): MemberLedgerResponse {
        return api.getMemberLedger(year)
    }

    suspend fun memberDueSummary(year: Int?): MemberDueSummaryResponse {
        return api.getMemberDueSummary(year)
    }

    suspend fun memberShareDetails(): MemberShareDetailsResponse {
        return api.getMemberShareDetails()
    }


    // ---------------- ADMIN ----------------

    suspend fun adminMembers(search: String?, perPage: Int?): AnyJson {
        return api.adminMembers(search, perPage)
    }

    suspend fun adminDeposits(search: String?, perPage: Int?): AnyJson {
        return api.adminDeposits(search, perPage)
    }

    suspend fun adminDueSummary(search: String?, perPage: Int?): AnyJson {
        return api.adminDueSummary(search, perPage)
    }
}
