package studio.hazeray.applimit.data.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class ApkDownloader @Inject constructor(@ApplicationContext private val context: Context) {

    suspend fun download(url: String, version: String): Uri {
        val downloadManager =
            context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val filename = "AppLimit-v$version.apk"
        val targetDir = context.getExternalFilesDir(DOWNLOAD_DIR)
            ?: throw IOException("External files dir unavailable")
        targetDir.mkdirs()
        val targetFile = File(targetDir, filename)
        if (targetFile.exists()) targetFile.delete()

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("AppLimit v$version")
            .setDestinationUri(Uri.fromFile(targetFile))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadId = downloadManager.enqueue(request)
        awaitCompletion(downloadId, downloadManager)
        return providerUriFor(targetFile)
    }

    private suspend fun awaitCompletion(id: Long, dm: DownloadManager) {
        suspendCancellableCoroutine<Unit> { continuation ->
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(c: Context, intent: Intent) {
                    val completed = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID,
                        -1L
                    )
                    if (completed != id) return
                    val status = queryStatus(dm, id)
                    runCatching { context.unregisterReceiver(this) }
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        continuation.resume(Unit)
                    } else {
                        continuation.resumeWithException(
                            IOException("Download failed with status $status")
                        )
                    }
                }
            }
            ContextCompat.registerReceiver(
                context,
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                ContextCompat.RECEIVER_EXPORTED
            )
            continuation.invokeOnCancellation {
                runCatching { context.unregisterReceiver(receiver) }
                dm.remove(id)
            }
        }
    }

    private fun queryStatus(dm: DownloadManager, id: Long): Int {
        val cursor = dm.query(DownloadManager.Query().setFilterById(id)) ?: return -1
        return cursor.use {
            if (!it.moveToFirst()) return -1
            val column = it.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (column < 0) -1 else it.getInt(column)
        }
    }

    private fun providerUriFor(file: File): Uri {
        val authority = "${context.packageName}.fileprovider"
        return FileProvider.getUriForFile(context, authority, file)
    }

    companion object {
        private const val DOWNLOAD_DIR = "updates"
    }
}
