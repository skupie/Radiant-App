package com.radiant.sms.network

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ---------- AUTH ----------
    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @Headers("Accept: application/json", "Content-Type: application/json")
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

    @Headers("Accept: application/json", "Content-Type: application/json")
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
        @Query("per_page") perPage: Int? = null,
        @Query("page") page: Int? = null
    ): AdminMembersResponse

    @Headers("Accept: application/json")
    @GET("api/admin/members/{member}")
    suspend fun adminMemberDetails(
        @Path("member") memberId: Long
    ): AdminMemberDetailsResponse

    @Multipart
    @Headers("Accept: application/json")
    @POST("api/admin/members")
    suspend fun adminCreateMember(
        @Part parts: List<MultipartBody.Part>
    ): MessageResponse

    @Multipart
    @Headers("Accept: application/json")
    @POST("api/admin/members/{member}")
    suspend fun adminUpdateMember(
        @Path("member") memberId: Long,
        @Part parts: List<MultipartBody.Part>
    ): MessageResponse

    // ✅ Existing (keep — used by older screens)
    @Headers("Accept: application/json")
    @GET("api/admin/deposits")
    suspend fun adminDeposits(
        @Query("search") search: String? = null,
        @Query("per_page") perPage: Int? = null
    ): AnyJson

    @Headers("Accept: application/json")
    @GET("api/admin/due-summary")
    suspend fun adminDueSummary(
        @Query("search") search: String? = null,
        @Query("per_page") perPage: Int? = null
    ): AnyJson

    // ✅ New (typed + filters + pagination + summary total)
    @Headers("Accept: application/json")
    @GET("api/admin/deposits")
    suspend fun adminDepositsList(
        @Query("search") search: String? = null,
        @Query("member_id") memberId: Long? = null,
        @Query("year") year: Int? = null,
        @Query("per_page") perPage: Int = 10,
        @Query("page") page: Int = 1
    ): AdminDepositsResponse

    // ✅ Create deposit
    @Headers("Accept: application/json")
    @POST("api/admin/deposits")
    suspend fun adminCreateDeposit(
        @Body body: AnyJson
    ): AnyJson

    // ✅ Update deposit
    @Headers("Accept: application/json")
    @PUT("api/admin/deposits/{id}")
    suspend fun adminUpdateDeposit(
        @Path("id") id: Long,
        @Body body: AnyJson
    ): AnyJson

    // ✅ Delete deposit
    @Headers("Accept: application/json")
    @DELETE("api/admin/deposits/{id}")
    suspend fun adminDeleteDeposit(
        @Path("id") id: Long
    ): AnyJson

    // Exports
    @GET("api/admin/members/export/pdf")
    suspend fun adminMembersExportPdf(): Response<ResponseBody>

    @GET("api/admin/members/export/excel")
    suspend fun adminMembersExportExcel(): Response<ResponseBody>
}
