package com.radiant.sms.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AdminActivityResponse(
    val data: List<AdminActivityDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AdminActivityDto(
    val id: Long? = null,
    val action: String? = null,
    val description: String? = null,
    @Json(name = "actor_name") val actorName: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminTeamMembersResponse(
    val data: List<AdminTeamMemberDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AdminTeamMemberDto(
    val id: Long? = null,
    val name: String? = null,
    val email: String? = null,
    val role: String? = null
)

@JsonClass(generateAdapter = true)
data class AdminTeamMemberUpsertRequest(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,
    val role: String? = null
)
