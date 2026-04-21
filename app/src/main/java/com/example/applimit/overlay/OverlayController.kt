package com.example.applimit.overlay

interface OverlayController {
    fun showWarningOverlay(
        appName: String,
        usedMinutes: Int,
        onExtend: () -> Unit,
        onDismiss: () -> Unit
    )

    fun showCooldownOverlay(
        appName: String,
        remainingMinutes: Int,
        onExtend: () -> Unit,
        onDismiss: () -> Unit
    )

    fun hideOverlay()

    fun isShowing(): Boolean
}
