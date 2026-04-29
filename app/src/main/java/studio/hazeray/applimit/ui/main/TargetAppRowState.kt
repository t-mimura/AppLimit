package studio.hazeray.applimit.ui.main

import studio.hazeray.applimit.domain.model.TargetApp

data class TargetAppRowState(
    val app: TargetApp,
    val status: SessionStatus
)

sealed class SessionStatus {
    object NotStarted : SessionStatus()
    data class Active(val remainingMinutes: Int, val isExtended: Boolean) : SessionStatus()
    data class Cooldown(val remainingMinutes: Int) : SessionStatus()
}
