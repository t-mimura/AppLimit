package com.example.applimit.ui.appselect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.applimit.data.repository.TargetAppRepository
import com.example.applimit.domain.model.TargetApp
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AppSelectViewModel @Inject constructor(
    private val repository: TargetAppRepository,
    private val installedAppLister: InstalledAppLister
) : ViewModel() {

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _appAdded = MutableStateFlow(false)
    val appAdded: StateFlow<Boolean> = _appAdded.asStateFlow()

    val filteredApps: StateFlow<List<InstalledApp>> =
        combine(_installedApps, _searchQuery) { apps, query ->
            if (query.isBlank()) {
                apps
            } else {
                apps.filter {
                    it.appName.contains(query, ignoreCase = true) ||
                        it.packageName.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            _installedApps.value = installedAppLister.getInstalledApps()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectApp(installedApp: InstalledApp) {
        viewModelScope.launch {
            val existing = repository.getTargetAppByPackageName(installedApp.packageName)
            if (existing != null) return@launch

            repository.addTargetApp(
                TargetApp(
                    packageName = installedApp.packageName,
                    appName = installedApp.appName
                )
            )
            _appAdded.value = true
        }
    }

    fun resetAppAdded() {
        _appAdded.value = false
    }
}
