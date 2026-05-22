package studio.hazeray.applimit.data.update

import android.net.Uri

sealed class UpdateState {
    data object Idle : UpdateState()

    data object Checking : UpdateState()

    data object UpToDate : UpdateState()

    data class UpdateAvailable(
        val version: String,
        val downloadUrl: String,
        val sizeBytes: Long
    ) : UpdateState()

    data class Downloading(val version: String) : UpdateState()

    data class ReadyToInstall(val apkUri: Uri, val version: String) : UpdateState()

    data class Error(val message: String) : UpdateState()
}
