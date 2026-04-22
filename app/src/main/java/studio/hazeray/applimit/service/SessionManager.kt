package studio.hazeray.applimit.service

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import studio.hazeray.applimit.domain.model.AppSession
import studio.hazeray.applimit.domain.model.SessionState
import studio.hazeray.applimit.domain.model.TargetApp

@Singleton
class SessionManager @Inject constructor() {

    private val _sessions = MutableStateFlow<Map<Long, AppSession>>(emptyMap())
    val sessions: StateFlow<Map<Long, AppSession>> = _sessions.asStateFlow()

    fun getSession(targetAppId: Long): AppSession? = _sessions.value[targetAppId]

    fun onTargetAppDetected(targetApp: TargetApp, currentTimeMillis: Long) {
        val session = _sessions.value[targetApp.id]

        when (session?.state) {
            null, SessionState.IDLE -> {
                updateSession(
                    targetApp.id,
                    AppSession(
                        targetAppId = targetApp.id,
                        state = SessionState.ACTIVE,
                        startedAt = currentTimeMillis,
                        expiresAt = currentTimeMillis + targetApp.limitMinutes * 60 * 1000L
                    )
                )
            }
            SessionState.ACTIVE,
            SessionState.WARNING,
            SessionState.COOLDOWN -> {
                // Keep existing session for this app as-is
            }
        }
    }

    fun onTargetAppLeft(@Suppress("UNUSED_PARAMETER") currentTimeMillis: Long) {
        // Timer does not stop when the target app leaves foreground (spec: simple implementation)
    }

    fun extend(targetApp: TargetApp, currentTimeMillis: Long) {
        val session = _sessions.value[targetApp.id] ?: return
        if (session.state != SessionState.WARNING && session.state != SessionState.COOLDOWN) return

        updateSession(
            targetApp.id,
            session.copy(
                state = SessionState.ACTIVE,
                expiresAt = currentTimeMillis + targetApp.extensionMinutes * 60 * 1000L,
                cooldownUntil = null
            )
        )
    }

    fun dismiss(targetApp: TargetApp, currentTimeMillis: Long) {
        val session = _sessions.value[targetApp.id] ?: return
        if (session.state != SessionState.WARNING) return

        updateSession(
            targetApp.id,
            session.copy(
                state = SessionState.COOLDOWN,
                cooldownUntil = currentTimeMillis + targetApp.cooldownMinutes * 60 * 1000L
            )
        )
    }

    fun checkState(currentTimeMillis: Long) {
        val current = _sessions.value
        var next: MutableMap<Long, AppSession>? = null

        for ((id, session) in current) {
            when (session.state) {
                SessionState.ACTIVE -> {
                    if (currentTimeMillis >= session.expiresAt) {
                        val updated = session.copy(state = SessionState.WARNING)
                        if (next == null) next = current.toMutableMap()
                        next[id] = updated
                    }
                }
                SessionState.COOLDOWN -> {
                    val cooldownUntil = session.cooldownUntil
                    if (cooldownUntil != null && currentTimeMillis >= cooldownUntil) {
                        if (next == null) next = current.toMutableMap()
                        next.remove(id)
                    }
                }
                else -> { /* no automatic transition */ }
            }
        }

        if (next != null) {
            _sessions.value = next
        }
    }

    fun reset() {
        _sessions.value = emptyMap()
    }

    private fun updateSession(id: Long, session: AppSession) {
        _sessions.value = _sessions.value + (id to session)
    }
}
