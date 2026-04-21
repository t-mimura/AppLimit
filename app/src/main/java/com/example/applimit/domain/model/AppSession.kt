package com.example.applimit.domain.model

data class AppSession(
    val targetAppId: Long,
    val state: SessionState,
    val startedAt: Long,
    val expiresAt: Long,
    val cooldownUntil: Long? = null
)
