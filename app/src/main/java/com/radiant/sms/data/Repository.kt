package com.radiant.sms.data

import com.radiant.sms.network.ApiService
import com.radiant.sms.network.LoginRequest
import com.radiant.sms.network.LoginResponse
import com.radiant.sms.network.MemberDueSummaryResponse
import com.radiant.sms.network.MemberLedgerResponse
import com.radiant.sms.network.MemberProfileResponse
import com.radiant.sms.network.MemberShareDetailsResponse
import com.radiant.sms.network.MembersResponse
import retrofit2.Response

class Repository(private val api: ApiService) {

    suspend fun login(request: LoginRequest): Response<LoginResponse> =
        api.login(request)

    suspend fun members(): Response<MembersResponse> =
        api.members()

    suspend fun memberProfile(memberId: Int): Response<MemberProfileResponse> =
        api.memberProfile(memberId)

    suspend fun memberLedger(memberId: Int, year: Int, month: Int): Response<MemberLedgerResponse> =
        api.memberLedger(memberId, year, month)

    suspend fun memberDueSummary(memberId: Int, year: Int): Response<MemberDueSummaryResponse> =
        api.memberDueSummary(memberId, year)

    suspend fun memberShareDetails(memberId: Int): Response<MemberShareDetailsResponse> =
        api.memberShareDetails(memberId)

    // ✅ RESTORED aliases (your screens are calling these)
    suspend fun getMemberLedger(memberId: Int, year: Int, month: Int) =
        memberLedger(memberId, year, month)

    suspend fun getMemberDueSummary(memberId: Int, year: Int) =
        memberDueSummary(memberId, year)

    suspend fun getMemberProfile(memberId: Int) =
        memberProfile(memberId)

    suspend fun getMemberShareDetails(memberId: Int) =
        memberShareDetails(memberId)
}
