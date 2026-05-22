package studio.hazeray.applimit.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import studio.hazeray.applimit.data.update.UpdateRepository
import studio.hazeray.applimit.data.update.UpdateSettings
import studio.hazeray.applimit.data.update.UpdateState

@HiltViewModel
class UpdateSectionViewModel @Inject constructor(
    private val updateRepository: UpdateRepository,
    private val updateSettings: UpdateSettings
) : ViewModel() {

    val state: StateFlow<UpdateState> = updateRepository.state
    val autoUpdateEnabled: StateFlow<Boolean> = updateSettings.autoUpdateEnabled
    val lastCheckedAt: StateFlow<Long> = updateSettings.lastCheckedAt

    fun checkNow() {
        viewModelScope.launch { updateRepository.checkForUpdate() }
    }

    fun startDownload() {
        updateRepository.startDownload()
    }

    fun launchInstaller() {
        updateRepository.launchInstaller()
    }

    fun setAutoUpdateEnabled(enabled: Boolean) {
        updateSettings.setAutoUpdateEnabled(enabled)
    }
}
