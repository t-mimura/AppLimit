package studio.hazeray.applimit.ui.debug

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import studio.hazeray.applimit.debug.DebugLogStore
import studio.hazeray.applimit.debug.DebugSettings
import studio.hazeray.applimit.debug.DebugTickRecord

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val debugLogStore: DebugLogStore,
    private val debugSettings: DebugSettings
) : ViewModel() {

    val entries: StateFlow<List<DebugTickRecord>> = debugLogStore.entries
    val overlayEnabled: StateFlow<Boolean> = debugSettings.overlayEnabled

    fun setOverlayEnabled(enabled: Boolean) {
        debugSettings.setOverlayEnabled(enabled)
    }

    fun clear() {
        debugLogStore.clear()
    }
}
