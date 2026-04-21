package com.example.applimit.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.applimit.data.repository.TargetAppRepository
import com.example.applimit.domain.model.TargetApp
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: TargetAppRepository
) : ViewModel() {

    val targetApps: StateFlow<List<TargetApp>> =
        repository.getAllTargetApps()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun toggleEnabled(appId: Long) {
        viewModelScope.launch {
            val app = repository.getTargetAppById(appId) ?: return@launch
            repository.updateTargetApp(app.copy(isEnabled = !app.isEnabled))
        }
    }

    fun removeApp(appId: Long) {
        viewModelScope.launch {
            val app = repository.getTargetAppById(appId) ?: return@launch
            repository.removeTargetApp(app)
        }
    }
}
