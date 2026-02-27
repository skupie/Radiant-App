package com.radiant.sms.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val password: String,
    val device_name: String? = "android"
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val token: String,
    val token_type: String,
    val user: UserDto
)

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    val role: String
)

@JsonClass(generateAdapter = true)
data class MessageResponse(
    val message: String? = null,
    val success: Boolean? = null
)

// Minimal "me" response wrapper - adjust if your API returns different fields
@JsonClass(generateAdapter = true)
data class MeResponse(
    val user: UserDto? = null
)

// For demo screens - keep as raw maps so the app compiles even if you change schema
@JsonClass(generateAdapter = true)
data class AnyJson(val data: Map<String, Any?> = emptyMap())
