package com.example.applimit.service.fake

import com.example.applimit.overlay.OverlayController

class FakeOverlayController : OverlayController {
    var showing = false
        private set

    var lastAppName: String? = null
        private set

    var lastOnExtend: (() -> Unit)? = null
        private set

    var lastOnDismiss: (() -> Unit)? = null
        private set

    override fun showWarningOverlay(
        appName: String,
        usedMinutes: Int,
        onExtend: () -> Unit,
        onDismiss: () -> Unit
    ) {
        showing = true
        lastAppName = appName
        lastOnExtend = onExtend
        lastOnDismiss = onDismiss
    }

    override fun showCooldownOverlay(
        appName: String,
        remainingMinutes: Int,
        onExtend: () -> Unit,
        onDismiss: () -> Unit
    ) {
        showing = true
        lastAppName = appName
        lastOnExtend = onExtend
        lastOnDismiss = onDismiss
    }

    override fun hideOverlay() {
        showing = false
        lastAppName = null
        lastOnExtend = null
        lastOnDismiss = null
    }

    override fun isShowing(): Boolean = showing
}
