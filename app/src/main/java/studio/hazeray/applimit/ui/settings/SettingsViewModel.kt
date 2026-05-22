package studio.hazeray.applimit.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.hazeray.applimit.data.repository.TargetAppRepository
import studio.hazeray.applimit.domain.model.TargetApp

@HiltViewModel(assistedFactory = SettingsViewModel.Factory::class)
class SettingsViewModel @AssistedInject constructor(
    private val repository: TargetAppRepository,
    @Assisted private val appId: Long
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(appId: Long): SettingsViewModel
    }

    // Persisted snapshot — only updated after save/delete completes.
    private val _targetApp = MutableStateFlow<TargetApp?>(null)
    val targetApp: StateFlow<TargetApp?> = _targetApp.asStateFlow()

    // Edits land in the draft; nothing is written until save() is called.
    private val _draft = MutableStateFlow<TargetApp?>(null)
    val draft: StateFlow<TargetApp?> = _draft.asStateFlow()

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    init {
        loadApp()
    }

    private fun loadApp() {
        viewModelScope.launch {
            val loaded = repository.getTargetAppById(appId)
            _targetApp.value = loaded
            _draft.value = loaded
        }
    }

    fun updateLimitMinutes(minutes: Int) {
        updateDraft { it.copy(limitMinutes = minutes) }
    }

    fun updateCooldownMinutes(minutes: Int) {
        updateDraft { it.copy(cooldownMinutes = minutes) }
    }

    fun updateExtensionMinutes(minutes: Int) {
        updateDraft { it.copy(extensionMinutes = minutes) }
    }

    fun toggleEnabled() {
        updateDraft { it.copy(isEnabled = !it.isEnabled) }
    }

    fun save() {
        viewModelScope.launch {
            val draftApp = _draft.value ?: return@launch
            repository.updateTargetApp(draftApp)
            _targetApp.value = draftApp
            _saved.value = true
        }
    }

    fun deleteApp() {
        viewModelScope.launch {
            val app = _targetApp.value ?: return@launch
            repository.removeTargetApp(app)
            _deleted.value = true
        }
    }

    private fun updateDraft(transform: (TargetApp) -> TargetApp) {
        val current = _draft.value ?: return
        _draft.value = transform(current)
    }
}
