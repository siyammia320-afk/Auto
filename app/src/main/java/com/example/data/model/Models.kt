package com.example.data.model

data class LiveService(
    val sid: String,
    val ranges: List<String>
)

data class ActiveNumber(
    val phone: String,
    val service: String,
    val range: String,
    val timestamp: String
)

data class OtpItem(
    val phone: String,
    val otp: String,
    val message: String,
    val service: String,
    val timestamp: String = ""
)

data class ConnectionInfo(
    val timezone: String,
    val region: String,
    val proxyCode: String
)

data class CreatedAccount(
    val success: Boolean,
    val name: String = "",
    val phone: String = "",
    val uid: String = "",
    val password: String = "",
    val birthday: String = "",
    val time: String = "",
    val cookie: String = "",
    val timezone: String = "",
    val region: String = "",
    val proxyCode: String = "",
    val error: String? = null
)
