package com.example.applimit.ui.appselect

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface InstalledAppLister {
    fun getInstalledApps(): List<InstalledApp>
}

@Singleton
class InstalledAppProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : InstalledAppLister {

    override fun getInstalledApps(): List<InstalledApp> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        return pm.queryIntentActivities(mainIntent, 0)
            .map { resolveInfo ->
                InstalledApp(
                    packageName = resolveInfo.activityInfo.packageName,
                    appName = resolveInfo.loadLabel(pm).toString(),
                    icon = resolveInfo.loadIcon(pm)
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.appName }
    }
}
