package studio.hazeray.applimit.data.update

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.hazeray.applimit.BuildConfig

@Singleton
class UpdateRepository @Inject constructor(
    private val api: GitHubReleaseApi,
    private val downloader: ApkDownloader,
    private val installer: UpdateInstaller,
    private val settings: UpdateSettings
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    private var downloadJob: Job? = null

    suspend fun checkForUpdate(now: Long = System.currentTimeMillis()): UpdateState {
        if (_state.value is UpdateState.Checking) return _state.value
        if (_state.value is UpdateState.Downloading) return _state.value
        _state.value = UpdateState.Checking
        return runCatching { api.fetchLatestRelease() }
            .fold(
                onSuccess = { release ->
                    settings.recordCheck(now)
                    val newer = isNewerVersion(release.versionName, BuildConfig.VERSION_NAME)
                    val asset = release.apkAsset
                    val next = if (newer && asset != null) {
                        UpdateState.UpdateAvailable(
                            version = release.versionName,
                            downloadUrl = asset.downloadUrl,
                            sizeBytes = asset.sizeBytes
                        )
                    } else {
                        UpdateState.UpToDate
                    }
                    _state.value = next
                    next
                },
                onFailure = { e ->
                    val err = UpdateState.Error(e.message ?: e.javaClass.simpleName)
                    _state.value = err
                    err
                }
            )
    }

    fun startDownload() {
        val available = _state.value as? UpdateState.UpdateAvailable ?: return
        if (downloadJob?.isActive == true) return
        downloadJob = scope.launch {
            _state.value = UpdateState.Downloading(version = available.version)
            runCatching { downloader.download(available.downloadUrl, available.version) }
                .fold(
                    onSuccess = { uri ->
                        _state.value = UpdateState.ReadyToInstall(uri, available.version)
                    },
                    onFailure = { e ->
                        _state.value = UpdateState.Error(e.message ?: e.javaClass.simpleName)
                    }
                )
        }
    }

    fun launchInstaller() {
        val ready = _state.value as? UpdateState.ReadyToInstall ?: return
        installer.launchInstaller(ready.apkUri)
    }

    fun reset() {
        downloadJob?.cancel()
        downloadJob = null
        _state.value = UpdateState.Idle
    }
}
