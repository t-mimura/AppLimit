package studio.hazeray.applimit.overlay

interface DebugOverlayController {
    fun update(info: DebugOverlayInfo)
    fun hide()
}

data class DebugOverlayInfo(
    val foregroundLabel: String,
    val foregroundPackage: String?,
    val isTarget: Boolean,
    val phase: Phase?,
    val remainingMs: Long?
) {
    enum class Phase { BEFORE_LIMIT, EXTENDED, COOLDOWN }
}
