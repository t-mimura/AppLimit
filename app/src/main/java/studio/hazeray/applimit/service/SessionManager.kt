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

    private val _currentSession = MutableStateFlow<AppSession?>(null)
    val currentSession: StateFlow<AppSession?> = _currentSession.asStateFlow()

    fun onTargetAppDetected(targetApp: TargetApp, currentTimeMillis: Long) {
        val session = _currentSession.value

        when (session?.state) {
            null, SessionState.IDLE -> {
                _currentSession.value = AppSession(
                    targetAppId = targetApp.id,
                    state = SessionState.ACTIVE,
                    startedAt = currentTimeMillis,
                    expiresAt = currentTimeMillis + targetApp.limitMinutes * 60 * 1000L
                )
            }
            SessionState.ACTIVE -> {
                // Already active for this app, do nothing
            }
            SessionState.WARNING -> {
                // Already warning, do nothing
            }
            SessionState.COOLDOWN -> {
                // Stay in COOLDOWN; user must explicitly extend to resume
            }
        }
    }

    fun onTargetAppLeft(@Suppress("UNUSED_PARAMETER") currentTimeMillis: Long) {
        // Timer does not stop when the target app leaves foreground (spec: simple implementation)
    }

    fun extend(targetApp: TargetApp, currentTimeMillis: Long) {
        val session = _currentSession.value ?: return
        if (session.state != SessionState.WARNING && session.state != SessionState.COOLDOWN) return

        _currentSession.value = session.copy(
            state = SessionState.ACTIVE,
            expiresAt = currentTimeMillis + targetApp.extensionMinutes * 60 * 1000L,
            cooldownUntil = null
        )
    }

    fun dismiss(targetApp: TargetApp, currentTimeMillis: Long) {
        val session = _currentSession.value ?: return
        if (session.state != SessionState.WARNING) return

        _currentSession.value = session.copy(
            state = SessionState.COOLDOWN,
            cooldownUntil = currentTimeMillis + targetApp.cooldownMinutes * 60 * 1000L
        )
    }

    fun checkState(currentTimeMillis: Long): AppSession? {
        val session = _currentSession.value ?: return null

        when (session.state) {
            SessionState.ACTIVE -> {
                if (currentTimeMillis >= session.expiresAt) {
                    _currentSession.value = session.copy(state = SessionState.WARNING)
                }
            }
            SessionState.COOLDOWN -> {
                val cooldownUntil = session.cooldownUntil
                if (cooldownUntil != null && currentTimeMillis >= cooldownUntil) {
                    _currentSession.value = null
                    return null
                }
            }
            else -> { /* no automatic transition */ }
        }

        return _currentSession.value
    }

    fun reset() {
        _currentSession.value = null
    }
}
