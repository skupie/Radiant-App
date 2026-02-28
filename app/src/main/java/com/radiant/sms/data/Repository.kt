// app/src/main/java/com/radiant/sms/network/ApiService.kt
package com.radiant.sms.network

import com.radiant.sms.network.models.*
import retrofit2.http.*

interface ApiService {

    // ---------- AUTH ----------
    @POST("api/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("api/me")
    suspend fun me(): MeResponse

    @POST("api/logout")
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


    // ---------- ADMIN (optional / safe placeholders) ----------
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
