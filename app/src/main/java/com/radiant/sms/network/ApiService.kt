package com.radiant.sms.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    // ---------- AUTH ----------
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("api/auth/me")
    suspend fun me(): MeResponse

    @POST("api/auth/logout")
    suspend fun logout(): MessageResponse


    // ---------- MEMBER ----------
    @GET("api/member/profile")
    suspend fun getMemberProfile(): MemberProfileResponse

    @GET("api/member/ledger")
    suspend fun getMemberLedger(@Query("year") year: Int? = null): MemberLedgerResponse

    @GET("api/member/due-summary")
    suspend fun getMemberDueSummary(@Query("year") year: Int? = null): MemberDueSummaryResponse

    @GET("api/member/share-details")
    suspend fun getMemberShareDetails(): MemberShareDetailsResponse
    
   // @GET("api/member/share-details")
    //suspend fun getMemberShareDetails(): MemberShareDetailsResponse


    // ---------- ADMIN ----------
    @GET("api/admin/members")
    suspend fun adminMembers(
        @Query("search") search: String? = null,
        @Query("perPage") perPage: Int? = null
    ): AnyJson

    @GET("api/admin/deposits")
    suspend fun adminDeposits(
        @Query("search") search: String? = null,
        @Query("perPage") perPage: Int? = null
    ): AnyJson

    @GET("api/admin/due-summary")
    suspend fun adminDueSummary(
        @Query("search") search: String? = null,
        @Query("perPage") perPage: Int? = null
    ): AnyJson
}
