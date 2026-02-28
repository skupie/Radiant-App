package com.radiant.sms.network

import com.radiant.sms.network.*
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/member/profile")
    suspend fun getMemberProfile(): MemberProfileResponse

    @GET("api/member/ledger")
    suspend fun getMemberLedger(@Query("year") year: Int? = null): MemberLedgerResponse

    @GET("api/member/due-summary")
    suspend fun getMemberDueSummary(@Query("year") year: Int? = null): MemberDueSummaryResponse

    @GET("api/member/share-details")
    suspend fun getMemberShareDetails(): MemberShareDetailsResponse
}
