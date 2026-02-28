// app/src/main/java/com/radiant/sms/data/Repository.kt
package com.radiant.sms.data

import com.radiant.sms.network.ApiService
import com.radiant.sms.network.*

class Repository(private val api: ApiService) {

    // AUTH
    suspend fun login(email: String, password: String): LoginResponse {
        return api.login(LoginRequest(email = email, password = password, device_name = "android"))
    }

    suspend fun me(): MeResponse = api.me()

    suspend fun logout(): MessageResponse = api.logout()


    // MEMBER
    suspend fun memberProfile(): MemberProfileResponse = api.getMemberProfile()

    suspend fun memberLedger(year: Int?): MemberLedgerResponse = api.getMemberLedger(year)

    suspend fun memberDueSummary(year: Int?): MemberDueSummaryResponse = api.getMemberDueSummary(year)

    suspend fun memberShareDetails(): MemberShareDetailsResponse = api.getMemberShareDetails()


    // ADMIN
    suspend fun adminMembers(search: String?, perPage: Int?): AnyJson = api.adminMembers(search, perPage)

    suspend fun adminDeposits(search: String?, perPage: Int?): AnyJson = api.adminDeposits(search, perPage)

    suspend fun adminDueSummary(search: String?, perPage: Int?): AnyJson = api.adminDueSummary(search, perPage)
}
