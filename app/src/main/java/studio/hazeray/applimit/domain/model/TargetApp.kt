package studio.hazeray.applimit.domain.model

data class TargetApp(
    val id: Long = 0L,
    val packageName: String,
    val appName: String,
    val limitMinutes: Int = DEFAULT_LIMIT_MINUTES,
    val cooldownMinutes: Int = DEFAULT_COOLDOWN_MINUTES,
    val extensionMinutes: Int = DEFAULT_EXTENSION_MINUTES,
    val isEnabled: Boolean = true
) {
    companion object {
        const val DEFAULT_LIMIT_MINUTES = 15
        const val DEFAULT_COOLDOWN_MINUTES = 120
        const val DEFAULT_EXTENSION_MINUTES = 5
    }
}
