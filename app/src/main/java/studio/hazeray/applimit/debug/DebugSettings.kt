package studio.hazeray.applimit.debug

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class DebugSettings @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val _overlayEnabled = MutableStateFlow(prefs.getBoolean(KEY_OVERLAY, false))
    val overlayEnabled: StateFlow<Boolean> = _overlayEnabled.asStateFlow()

    fun setOverlayEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_OVERLAY, enabled) }
        _overlayEnabled.value = enabled
    }

    companion object {
        private const val PREF_NAME = "debug_settings"
        private const val KEY_OVERLAY = "overlay_enabled"
    }
}
