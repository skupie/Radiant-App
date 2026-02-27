package com.radiant.sms.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("api/auth/me")
    suspend fun me(): MeResponse

    @POST("api/auth/logout")
    suspend fun logout(): MessageResponse

    // Member
    @GET("api/member/profile")
    suspend fun memberProfile(): AnyJson

    @GET("api/member/ledger")
    suspend fun memberLedger(@Query("year") year: Int? = null): AnyJson

    @GET("api/member/due-summary")
    suspend fun memberDueSummary(@Query("year") year: Int? = null): AnyJson

    @GET("api/member/share-details")
    suspend fun memberShareDetails(): AnyJson

    // Admin
    @GET("api/admin/members")
    suspend fun adminMembers(
        @Query("search") search: String? = null,
        @Query("per_page") perPage: Int? = null
    ): AnyJson

    @GET("api/admin/deposits")
    suspend fun adminDeposits(
        @Query("search") search: String? = null,
        @Query("per_page") perPage: Int? = null
    ): AnyJson

    @GET("api/admin/due-summary")
    suspend fun adminDueSummary(
        @Query("search") search: String? = null,
        @Query("per_page") perPage: Int? = null
    ): AnyJson
}
