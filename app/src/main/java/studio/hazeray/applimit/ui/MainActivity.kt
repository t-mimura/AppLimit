package studio.hazeray.applimit.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import studio.hazeray.applimit.service.MonitorService
import studio.hazeray.applimit.service.NotificationHelper
import studio.hazeray.applimit.ui.permission.hasNotificationPermission
import studio.hazeray.applimit.ui.permission.hasUsageStatsPermission

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notificationHelper.createNotificationChannels()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasAllRequiredPermissions()) {
            startMonitorService()
        }
    }

    private fun hasAllRequiredPermissions(): Boolean = hasUsageStatsPermission(this) &&
        Settings.canDrawOverlays(this) &&
        hasNotificationPermission(this)

    private fun startMonitorService() {
        val intent = Intent(this, MonitorService::class.java)
        startForegroundService(intent)
    }
}
