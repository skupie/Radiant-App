// app/src/main/java/com/radiant/sms/network/models/AuthModels.kt
package com.radiant.sms.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

typealias AnyJson = Map<String, Any?>

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val password: String,
    val device_name: String? = "android"
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val token: String,
    val user: User
)

@JsonClass(generateAdapter = true)
data class User(
    val id: Long? = null,
    val name: String? = null,
    val email: String? = null,
    val role: String? = null
)

@JsonClass(generateAdapter = true)
data class MeResponse(
    val user: User
)

@JsonClass(generateAdapter = true)
data class MessageResponse(
    val message: String? = null
)

@JsonClass(generateAdapter = true)
data class ChangePasswordRequest(
    @Json(name = "current_password") val currentPassword: String,
    @Json(name = "new_password") val newPassword: String
)
