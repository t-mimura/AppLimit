package studio.hazeray.applimit.data.repository

import studio.hazeray.applimit.data.db.TargetAppEntity
import studio.hazeray.applimit.domain.model.TargetApp

fun TargetAppEntity.toDomain(): TargetApp = TargetApp(
    id = id,
    packageName = packageName,
    appName = appName,
    limitMinutes = limitMinutes,
    cooldownMinutes = cooldownMinutes,
    extensionMinutes = extensionMinutes,
    isEnabled = isEnabled
)

fun TargetApp.toEntity(): TargetAppEntity = TargetAppEntity(
    id = id,
    packageName = packageName,
    appName = appName,
    limitMinutes = limitMinutes,
    cooldownMinutes = cooldownMinutes,
    extensionMinutes = extensionMinutes,
    isEnabled = isEnabled
)
