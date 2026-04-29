package studio.hazeray.applimit.debug

data class DebugTickRecord(
    val timestamp: Long,
    val foregroundPackage: String?,
    val isTarget: Boolean,
    val targetAppName: String?,
    val sessionState: String?,
    val remainingMs: Long?,
    val isExtended: Boolean
)
