package com.example.applimit.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.applimit.data.repository.TargetAppRepository
import com.example.applimit.domain.model.TargetApp
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = SettingsViewModel.Factory::class)
class SettingsViewModel @AssistedInject constructor(
    private val repository: TargetAppRepository,
    @Assisted private val appId: Long
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(appId: Long): SettingsViewModel
    }

    private val _targetApp = MutableStateFlow<TargetApp?>(null)
    val targetApp: StateFlow<TargetApp?> = _targetApp.asStateFlow()

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    init {
        loadApp()
    }

    private fun loadApp() {
        viewModelScope.launch {
            _targetApp.value = repository.getTargetAppById(appId)
        }
    }

    fun updateLimitMinutes(minutes: Int) {
        updateApp { it.copy(limitMinutes = minutes) }
    }

    fun updateCooldownMinutes(minutes: Int) {
        updateApp { it.copy(cooldownMinutes = minutes) }
    }

    fun updateExtensionMinutes(minutes: Int) {
        updateApp { it.copy(extensionMinutes = minutes) }
    }

    fun toggleEnabled() {
        updateApp { it.copy(isEnabled = !it.isEnabled) }
    }

    fun deleteApp() {
        viewModelScope.launch {
            val app = _targetApp.value ?: return@launch
            repository.removeTargetApp(app)
            _deleted.value = true
        }
    }

    private fun updateApp(transform: (TargetApp) -> TargetApp) {
        viewModelScope.launch {
            val current = _targetApp.value ?: return@launch
            val updated = transform(current)
            repository.updateTargetApp(updated)
            _targetApp.value = updated
        }
    }
}
