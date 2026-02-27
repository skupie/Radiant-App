package com.radiant.sms.data

import com.radiant.sms.network.*

class Repository(private val api: ApiService) {

    suspend fun login(email: String, password: String): LoginResponse {
        return api.login(LoginRequest(email = email, password = password, device_name = "android"))
    }

    suspend fun me(): MeResponse = api.me()

    suspend fun logout(): MessageResponse = api.logout()

    suspend fun memberProfile(): AnyJson = api.memberProfile()
    suspend fun memberLedger(year: Int?): AnyJson = api.memberLedger(year)
    suspend fun memberDueSummary(year: Int?): AnyJson = api.memberDueSummary(year)
    suspend fun memberShareDetails(): AnyJson = api.memberShareDetails()

    suspend fun adminMembers(search: String?, perPage: Int?): AnyJson = api.adminMembers(search, perPage)
    suspend fun adminDeposits(search: String?, perPage: Int?): AnyJson = api.adminDeposits(search, perPage)
    suspend fun adminDueSummary(search: String?, perPage: Int?): AnyJson = api.adminDueSummary(search, perPage)
}
