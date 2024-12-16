package com.example.myapplication

data class ApiResponse(
    val message: String,
    val user: User
)

data class User(
    val iss: String,
    val aud: String,
    val auth_time: Long,
    val user_id: String,
    val sub: String,
    val iat: Long,
    val exp: Long,
    val email: String,
    val email_verified: Boolean,
    val firebase: Firebase,
    val uid: String
)

data class Firebase(
    val identities: Identities,
    val sign_in_provider: String
)

data class Identities(
    val email: List<String>
)