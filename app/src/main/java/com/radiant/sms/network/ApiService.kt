package com.radiant.sms.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    // ---------- AUTH ----------
    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("api/auth/change-password")
    suspend fun authChangePassword(@Body body: ChangePasswordRequest): MessageResponse

    @Headers("Accept: application/json")
    @GET("api/auth/me")
    suspend fun me(): MeResponse

    @Headers("Accept: application/json")
    @POST("api/auth/logout")
    suspend fun logout(): MessageResponse


    // ---------- MEMBER ----------
    @Headers("Accept: application/json")
    @GET("api/member/profile")
    suspend fun getMemberProfile(): MemberProfileResponse

    @Headers(
        "Accept: application/json",
        "Content-Type: application/json"
    )
    @POST("api/member/change-password")
    suspend fun memberChangePassword(@Body body: ChangePasswordRequest): MessageResponse

    @Headers("Accept: application/json")
    @GET("api/member/ledger")
    suspend fun getMemberLedger(@Query("year") year: Int? = null): MemberLedgerResponse

    @Headers("Accept: application/json")
    @GET("api/member/due-summary")
    suspend fun getMemberDueSummary(@Query("year") year: Int? = null): MemberDueSummaryResponse

    @Headers("Accept: application/json")
    @GET("api/member/share-details")
    suspend fun getMemberShareDetails(): MemberShareDetailsResponse


    // ---------- ADMIN ----------
    @Headers("Accept: application/json")
    @GET("api/admin/members")
    suspend fun adminMembers(
        @Query("search") search: String? = null,
        @Query("perPage") perPage: Int? = null
    ): AnyJson

    @Headers("Accept: application/json")
    @GET("api/admin/deposits")
    suspend fun adminDeposits(
        @Query("search") search: String? = null,
        @Query("perPage") perPage: Int? = null
    ): AnyJson

    @Headers("Accept: application/json")
    @GET("api/admin/due-summary")
    suspend fun adminDueSummary(
        @Query("search") search: String? = null,
        @Query("perPage") perPage: Int? = null
    ): AnyJson
}
