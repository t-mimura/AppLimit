package studio.hazeray.applimit.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.hazeray.applimit.data.repository.TargetAppRepository
import studio.hazeray.applimit.domain.model.AppSession
import studio.hazeray.applimit.domain.model.SessionState
import studio.hazeray.applimit.service.SessionManager

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: TargetAppRepository,
    sessionManager: SessionManager
) : ViewModel() {

    private val ticker: Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(TICKER_INTERVAL_MS)
        }
    }

    val rows: StateFlow<List<TargetAppRowState>> =
        combine(
            repository.getAllTargetApps(),
            sessionManager.sessions,
            ticker
        ) { apps, sessions, _ ->
            val now = System.currentTimeMillis()
            apps.map { app ->
                TargetAppRowState(app, computeStatus(sessions[app.id], now))
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleEnabled(appId: Long) {
        viewModelScope.launch {
            val app = repository.getTargetAppById(appId) ?: return@launch
            repository.updateTargetApp(app.copy(isEnabled = !app.isEnabled))
        }
    }

    fun removeApp(appId: Long) {
        viewModelScope.launch {
            val app = repository.getTargetAppById(appId) ?: return@launch
            repository.removeTargetApp(app)
        }
    }

    private fun computeStatus(session: AppSession?, now: Long): SessionStatus {
        if (session == null) return SessionStatus.NotStarted
        return when (session.state) {
            SessionState.ACTIVE -> SessionStatus.Active(
                remainingMinutes = ceilMinutes(session.expiresAt - now),
                isExtended = session.isExtended
            )
            SessionState.COOLDOWN -> {
                val until = session.cooldownUntil ?: return SessionStatus.NotStarted
                SessionStatus.Cooldown(remainingMinutes = ceilMinutes(until - now))
            }
            SessionState.IDLE -> SessionStatus.NotStarted
        }
    }

    private fun ceilMinutes(ms: Long): Int = if (ms <= 0) 0 else ((ms + 59_999) / 60_000).toInt()

    companion object {
        private const val TICKER_INTERVAL_MS = 30_000L
    }
}
