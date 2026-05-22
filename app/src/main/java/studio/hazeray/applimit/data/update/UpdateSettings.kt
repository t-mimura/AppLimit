package studio.hazeray.applimit.data.update

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class UpdateSettings @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _autoUpdateEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_AUTO_UPDATE, true)
    )
    val autoUpdateEnabled: StateFlow<Boolean> = _autoUpdateEnabled.asStateFlow()

    private val _lastCheckedAt = MutableStateFlow(prefs.getLong(KEY_LAST_CHECK, 0L))
    val lastCheckedAt: StateFlow<Long> = _lastCheckedAt.asStateFlow()

    fun setAutoUpdateEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_UPDATE, enabled).apply()
        _autoUpdateEnabled.value = enabled
    }

    fun recordCheck(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_CHECK, timestamp).apply()
        _lastCheckedAt.value = timestamp
    }

    companion object {
        private const val PREFS_NAME = "update_settings"
        private const val KEY_AUTO_UPDATE = "auto_update_enabled"
        private const val KEY_LAST_CHECK = "last_checked_at"
    }
}
