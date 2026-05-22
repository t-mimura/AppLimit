package studio.hazeray.applimit.ui.appselect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.hazeray.applimit.data.repository.TargetAppRepository
import studio.hazeray.applimit.domain.model.TargetApp

@HiltViewModel
class AppSelectViewModel @Inject constructor(
    private val repository: TargetAppRepository,
    private val installedAppLister: InstalledAppLister
) : ViewModel() {

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _addedAppId = MutableStateFlow<Long?>(null)
    val addedAppId: StateFlow<Long?> = _addedAppId.asStateFlow()

    private val _duplicateSelected = MutableStateFlow(false)
    val duplicateSelected: StateFlow<Boolean> = _duplicateSelected.asStateFlow()

    val addedPackages: StateFlow<Set<String>> =
        repository.getAllTargetApps()
            .map { apps -> apps.map { it.packageName }.toSet() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

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
            if (existing != null) {
                _duplicateSelected.value = true
                return@launch
            }

            val newId = repository.addTargetApp(
                TargetApp(
                    packageName = installedApp.packageName,
                    appName = installedApp.appName
                )
            )
            _addedAppId.value = newId
        }
    }

    fun resetAddedAppId() {
        _addedAppId.value = null
    }

    fun resetDuplicateSelected() {
        _duplicateSelected.value = false
    }
}
