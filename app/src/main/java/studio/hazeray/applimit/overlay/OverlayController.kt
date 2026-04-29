package studio.hazeray.applimit.overlay

interface OverlayController {
    fun showCooldownOverlay(
        appName: String,
        remainingMinutes: Int,
        onExtend: () -> Unit,
        onDismiss: () -> Unit
    )

    fun hideOverlay()

    fun isShowing(): Boolean
}
